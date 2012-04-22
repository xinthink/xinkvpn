/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/poll.h>
#include <sys/wait.h>
#include <netdb.h>
#include <signal.h>
#include <unistd.h>
#include <fcntl.h>
#include <time.h>

#ifdef ANDROID_CHANGES
#include <android/log.h>
#include <cutils/sockets.h>
#include "keystore_get.h"
#endif

#include "mtpd.h"

int the_socket = -1;

extern struct protocol l2tp;
extern struct protocol pptp;
static struct protocol *protocols[] = {&l2tp, &pptp, NULL};
static struct protocol *the_protocol;

static int pppd_argc;
static char **pppd_argv;
static pid_t pppd_pid;

/* We redirect signals to a pipe in order to prevent race conditions. */
static int signals[2];

static void interrupt(int signal)
{
    write(signals[1], &signal, sizeof(int));
}

static int initialize(int argc, char **argv)
{
    int timeout = 0;
    int i;

    for (i = 2; i < argc; ++i) {
        if (!argv[i][0]) {
            pppd_argc = argc - i - 1;
            pppd_argv = &argv[i + 1];
            argc = i;
            break;
        }
    }

    if (argc >= 2) {
        for (i = 0; protocols[i]; ++i) {
            if (!strcmp(argv[1], protocols[i]->name)) {
                log_print(INFO, "Using protocol %s", protocols[i]->name);
                the_protocol = protocols[i];
                timeout = the_protocol->connect(argc - 2, &argv[2]);
                break;
            }
        }
    }

    if (!the_protocol || timeout == -USAGE_ERROR) {
        printf("Usage: %s <protocol-args> '' <pppd-args>, "
               "where protocol-args are one of:\n", argv[0]);
        for (i = 0; protocols[i]; ++i) {
            printf("       %s %s\n", protocols[i]->name, protocols[i]->usage);
        }
        exit(USAGE_ERROR);
    }
    return timeout;
}

static void stop_pppd()
{
    if (pppd_pid) {
        log_print(INFO, "Sending signal to pppd (pid = %d)", pppd_pid);
        kill(pppd_pid, SIGTERM);
        sleep(5);
        pppd_pid = 0;
    }
}

#ifdef ANDROID_CHANGES

static int get_control_and_arguments(int *argc, char ***argv)
{
    static char *args[256];
    int control;
    int i;

    if ((i = android_get_control_socket("mtpd")) == -1) {
        return -1;
    }
    log_print(DEBUG, "Waiting for control socket");
    if (listen(i, 1) == -1 || (control = accept(i, NULL, 0)) == -1) {
        log_print(FATAL, "Cannot get control socket");
        exit(SYSTEM_ERROR);
    }
    close(i);
    fcntl(control, F_SETFD, FD_CLOEXEC);

    args[0] = (*argv)[0];
    for (i = 1; i < 256; ++i) {
        unsigned char length;
        if (recv(control, &length, 1, 0) != 1) {
            log_print(FATAL, "Cannot get argument length");
            exit(SYSTEM_ERROR);
        }
        if (length == 0xFF) {
            break;
        } else {
            int offset = 0;
            args[i] = malloc(length + 1);
            while (offset < length) {
                int n = recv(control, &args[i][offset], length - offset, 0);
                if (n > 0) {
                    offset += n;
                } else {
                    log_print(FATAL, "Cannot get argument value");
                    exit(SYSTEM_ERROR);
                }
            }
            args[i][length] = 0;
        }
    }
    log_print(DEBUG, "Received %d arguments", i - 1);

    /* L2TP secret is the only thing stored in keystore. We do the query here
     * so other files are clean and free from android specific code. */
    if (i > 4 && !strcmp("l2tp", args[1]) && args[4][0]) {
        char value[KEYSTORE_MESSAGE_SIZE];
        int length = keystore_get(args[4], strlen(args[4]), value);
        if (length == -1) {
            log_print(FATAL, "Cannot get L2TP secret from keystore");
            exit(SYSTEM_ERROR);
        }
        free(args[4]);
        args[4] = malloc(length + 1);
        memcpy(args[4], value, length);
        args[4][length] = 0;
    }

    *argc = i;
    *argv = args;
    return control;
}

#endif

int main(int argc, char **argv)
{
    struct pollfd pollfds[2];
    int timeout;
    int status;
#ifdef ANDROID_CHANGES
    int control = get_control_and_arguments(&argc, &argv);
    unsigned char code = argc - 1;
    send(control, &code, 1, 0);
#endif

    srandom(time(NULL));

    if (pipe(signals) == -1) {
        log_print(FATAL, "Pipe() %s", strerror(errno));
        exit(SYSTEM_ERROR);
    }
    fcntl(signals[0], F_SETFD, FD_CLOEXEC);
    fcntl(signals[1], F_SETFD, FD_CLOEXEC);

    timeout = initialize(argc, argv);

    signal(SIGHUP, interrupt);
    signal(SIGINT, interrupt);
    signal(SIGTERM, interrupt);
    signal(SIGCHLD, interrupt);
    signal(SIGPIPE, SIG_IGN);
    atexit(stop_pppd);

    pollfds[0].fd = signals[0];
    pollfds[0].events = POLLIN;
    pollfds[1].fd = the_socket;
    pollfds[1].events = POLLIN;

    while (timeout >= 0) {
        if (poll(pollfds, 2, timeout ? timeout : -1) == -1 && errno != EINTR) {
            log_print(FATAL, "Poll() %s", strerror(errno));
            exit(SYSTEM_ERROR);
        }
        if (pollfds[0].revents) {
            break;
        }
        timeout = pollfds[1].revents ?
            the_protocol->process() : the_protocol->timeout();
    }

    if (timeout < 0) {
        status = -timeout;
    } else {
        int signal;
        read(signals[0], &signal, sizeof(int));
        log_print(INFO, "Received signal %d", signal);
        if (signal == SIGCHLD && waitpid(pppd_pid, &status, WNOHANG) == pppd_pid
            && WIFEXITED(status)) {
            status = WEXITSTATUS(status);
            log_print(INFO, "Pppd is terminated (status = %d)", status);
            status += PPPD_EXITED;
            pppd_pid = 0;
        } else {
            status = USER_REQUESTED;
        }
    }

    stop_pppd();
    the_protocol->shutdown();

#ifdef ANDROID_CHANGES
    code = status;
    send(control, &code, 1, 0);
#endif
    log_print(INFO, "Mtpd is terminated (status = %d)", status);
    return status;
}

void log_print(int level, char *format, ...)
{
    if (level >= 0 && level <= LOG_MAX) {
#ifdef ANDROID_CHANGES
        static int levels[5] = {
            ANDROID_LOG_DEBUG, ANDROID_LOG_INFO, ANDROID_LOG_WARN,
            ANDROID_LOG_ERROR, ANDROID_LOG_FATAL
        };
        va_list ap;
        va_start(ap, format);
        __android_log_vprint(levels[level], "mtpd", format, ap);
        va_end(ap);
#else
        static char *levels = "DIWEF";
        va_list ap;
        fprintf(stderr, "%c: ", levels[level]);
        va_start(ap, format);
        vfprintf(stderr, format, ap);
        va_end(ap);
        fputc('\n', stderr);
#endif
    }
}

void create_socket(int family, int type, char *server, char *port)
{
    struct addrinfo hints = {
        .ai_flags = AI_NUMERICSERV,
        .ai_family = family,
        .ai_socktype = type,
    };
    struct addrinfo *records;
    struct addrinfo *r;
    int error;

    log_print(INFO, "Connecting to %s port %s", server, port);

    error = getaddrinfo(server, port, &hints, &records);
    if (error) {
        log_print(FATAL, "Getaddrinfo() %s", (error == EAI_SYSTEM) ?
                  strerror(errno) : gai_strerror(error));
        exit(NETWORK_ERROR);
    }

    for (r = records; r; r = r->ai_next) {
        the_socket = socket(r->ai_family, r->ai_socktype, r->ai_protocol);
        if (the_socket != -1) {
            if (connect(the_socket, r->ai_addr, r->ai_addrlen) == 0) {
                break;
            }
            close(the_socket);
            the_socket = -1;
        }
    }

    freeaddrinfo(records);

    if (the_socket == -1) {
        log_print(FATAL, "Connect() %s", strerror(errno));
        exit(NETWORK_ERROR);
    }

    fcntl(the_socket, F_SETFD, FD_CLOEXEC);
    log_print(INFO, "Connection established (socket = %d)", the_socket);
}

void start_pppd(int pppox)
{
    if (pppd_pid) {
        log_print(WARNING, "Pppd is already started (pid = %d)", pppd_pid);
        close(pppox);
        return;
    }

    log_print(INFO, "Starting pppd (pppox = %d)", pppox);

    pppd_pid = fork();
    if (pppd_pid < 0) {
        log_print(FATAL, "Fork() %s", strerror(errno));
        exit(SYSTEM_ERROR);
    }

    if (!pppd_pid) {
        char *args[pppd_argc + 5];
        char number[12];

        sprintf(number, "%d", pppox);
        args[0] = "pppd";
        args[1] = "nodetach";
        args[2] = "pppox";
        args[3] = number;
        memcpy(&args[4], pppd_argv, sizeof(char *) * pppd_argc);
        args[4 + pppd_argc] = NULL;

#ifdef ANDROID_CHANGES
        {
            char envargs[65536];
            char *tail = envargs;
            int i;
            /* Hex encode the arguments using [A-P] instead of [0-9A-F]. */
            for (i = 0; args[i]; ++i) {
                char *p = args[i];
                do {
                    *tail++ = 'A' + ((*p >> 4) & 0x0F);
                    *tail++ = 'A' + (*p & 0x0F);
                } while (*p++);
            }
            *tail = 0;
            setenv("envargs", envargs, 1);
            args[1] = NULL;
        }
#endif
        execvp("pppd", args);
        log_print(FATAL, "Exec() %s", strerror(errno));
        exit(1); /* Pretending a fatal error in pppd. */
    }

    log_print(INFO, "Pppd started (pid = %d)", pppd_pid);
    close(pppox);
}
