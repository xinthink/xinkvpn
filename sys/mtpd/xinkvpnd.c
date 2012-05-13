/*
 * Copyright 2011 yingxinwu.g@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
#include "xinkvpnd.h"

void interrupt(int signal)
{
    log_d("interrupt, signal: %d", signal);
    unlink(SOCK_PATH);
}

void on_exit()
{
    log_i("shut down xinkvpn daemon");
    unlink(SOCK_PATH);
}

int get_control_socket()
{
    int sock;
    
    if ((sock = socket(PF_LOCAL, SOCK_STREAM, 0)) < 0) {
        log_e("error create unix domain socket. %s\n", strerror(errno));
        return -1;
    }

    struct sockaddr_un sock_addr;
    memset(&sock_addr, 0, sizeof(sock_addr));
    sock_addr.sun_family = PF_LOCAL;
    strcpy(sock_addr.sun_path, SOCK_PATH);
    int sock_path_offset = offsetof(struct sockaddr_un, sun_path) + strlen(sock_addr.sun_path) + 1;

    unlink(SOCK_PATH);
    if (bind(sock, (struct sockaddr *) &sock_addr, sock_path_offset)) {
        log_e("error bind to %s. %s\n", sock_addr.sun_path, strerror(errno));
        return -1;
    }
    
    if (listen(sock, MAX_CONN) < 0) {
        log_e("error listen to %s. %s\n", sock_addr.sun_path, strerror(errno));
        return -1;
    }
    log_i("listening to %s\n", sock_addr.sun_path);
    return sock;
}

void accept_conns(const int sock)
{
    int conn;
    while (1) {
        if((conn = accept(sock, NULL, 0)) < 0) {
            log_e("error accepting socket conn. %s\n", strerror(errno));
            continue;
        }
        
        unsigned char ret = 0;
        if ((ret = handle_conn(conn)) < 0) {
            log_e("error handle socket conn. %s\n", strerror(errno));
        }
        send(conn, &ret, 1, 0);
    }
}

void read_args(const int conn, int *argc, char ***argv)
{
    char *args[MAX_ARGS];
    int i;
    for (i = 0;; ++i) {
        unsigned char len;
        if (recv(conn, &len, 1, 0) != 1 || 0xFF == len) break; // TODO allow max len 0xFFFF
        
        char *buf = malloc(len + 1);
        int offset = 0;
        
        while (offset < len) {
            int n = recv(conn, &buf[offset], len - offset, 0);
            if (n <= 0) break;

            offset += n;
        }

        buf[len] = 0;
        args[i] = buf;
        log_d(buf);
    }
    log_d("--- end ---");

    *argc = i;
    *argv = args;
}

int do_start(const int argc, const char **argv)
{
    log_i("start %s on %s ...", argv[1], argv[0]);

    int svc;
    if ((svc = start_daemon(&mtpd)) < 0) {
        log_e("start daemon mtpd failed");
        return -1;
    }

    // send args to daemon
    int i;
    for (i = 0; i < argc; ++i) {
        log_d("send arg #%d '%s'", i, argv[i]);

        int len = strlen(argv[i]);
        send_int16(svc, len);
        send(svc, argv[i], len, 0);
    }
    send_int16(svc, SVC_ARGS_END);

    return 0;
}

int do_stop(const int argc, const char **argv)
{
    log_i("stop mtpd ...");
    return stop_daemon(&mtpd);
}

int do_alive(const int argc, const char **argv)
{
    log_d("check alive ...");
    return 0;
}

int handle_conn(const int conn)
{
    log_d("handling request...");
    unsigned char cmd;
    
    if (recv(conn, &cmd, 1, 0) <= 0) {
        log_e("recv cmd failed");
        return -1;
    }
    log_i("recv command: %d", cmd);
    
    // read cmd args
    int argc;
    char **argv;
    read_args(conn, &argc, &argv);

    // handle command
    switch (cmd) {
        case CMD_START:
            return do_start(argc, argv);
        case CMD_STOP:
            return do_stop(argc, argv);
        case CMD_ALIVE:
            return do_alive(argc, argv);
        default:
            log_e("unknowd cmd %d", cmd);
            break;
    }
    
    return 0;
}

int main(int argc, char **argv)
{
    signal(SIGHUP, interrupt);
    signal(SIGINT, interrupt);
    signal(SIGTERM, interrupt);
    signal(SIGCHLD, interrupt);
    signal(SIGPIPE, SIG_IGN);
    atexit(on_exit);

    int control = get_control_socket();
    accept_conns(control);
    return 0;
}

// -----------------------------------------------------------------------
// daemon mgmt
//
int get_svc_state(const char *daemon, char **state)
{
    char key[PROPERTY_KEY_MAX];
    strlcpy(key, SVC_STATE_CMD_PREFIX, PROPERTY_KEY_MAX);
    strlcat(key, daemon, PROPERTY_KEY_MAX);

    *state = malloc(PROPERTY_VALUE_MAX);
    return property_get(key, *state, "");
}

int wait_svc_state(const char *daemon, const char *expectedState)
{
    char key[PROPERTY_KEY_MAX];
    strlcpy(key, SVC_STATE_CMD_PREFIX, PROPERTY_KEY_MAX);
    strlcat(key, daemon, PROPERTY_KEY_MAX);

    char val[PROPERTY_VALUE_MAX];
    int result = 0;

    int i;
    for (i = 0; i < SVC_RETRY; ++i) {
        if (property_get(key, val, "") > 0) {
            if (strcmp(expectedState, val) == 0) {
                result = 1;
                break;
            }
        }

        sleep(SVC_RETRY_INTV);
    }

    return result;
}

int start_daemon(const struct daemon *d)
{
    // check state of the mtpd service
    char *state;
    if (get_svc_state(d->name, &state) > 0) {
        if (strcmp(SVC_STATE_RUNNING, state) == 0) {
            log_i("deamn %s is running, stop it first\n", d->name);
            if (stop_daemon(d) < 0) return -1;
        }
    }

    // start it
    if (property_set(SVC_START_CMD, d->name) < 0) return -1;
    if (!wait_svc_state(d->name, SVC_STATE_RUNNING)) return -1;
    sleep(SVC_RETRY_INTV * 2); // wait daemon start up completely

    // open control socket
    int svc = get_svc_sock(d);
    if (svc < 0) return -1;

    return svc;
}

int get_svc_sock(const struct daemon *d)
{
    int svc;
    
    if ((svc = socket(PF_LOCAL, SOCK_STREAM, 0)) < 0) {
        log_e("error create unix domain socket. %s\n", strerror(errno));
        return -1;
    }

    struct sockaddr_un addr;
    memset(&addr, 0, sizeof(addr));
    addr.sun_family = PF_LOCAL;
    strcpy(addr.sun_path, d->sock);
    int sock_path_offset = offsetof(struct sockaddr_un, sun_path) + strlen(addr.sun_path) + 1;

    if (connect(svc, (struct sockaddr *) &addr, sock_path_offset)) {
        log_e("error connect to %s. %s\n", addr.sun_path, strerror(errno));
        return -1;
    }

    return svc;
}

int stop_daemon(const struct daemon *d)
{
    if (property_set(SVC_STOP_CMD, d->name) < 0) return -1;
    if (!wait_svc_state(d->name, SVC_STATE_STOPPED)) return -1;
    return 0;
}

// -----------------------------------------------------------------------
// log utils
//

void log_d(const char *format, ...)
{
#ifdef DEBUG
    va_list ap;
    va_start(ap, format);
    __android_log_vprint(ANDROID_LOG_DEBUG, LOG_TAG, format, ap);
    va_end(ap);
#endif
}

void log_i(const char *format, ...)
{
    va_list ap;
    va_start(ap, format);
    __android_log_vprint(ANDROID_LOG_INFO, LOG_TAG, format, ap);
    va_end(ap);
}

void log_e(const char *format, ...)
{
    va_list ap;
    va_start(ap, format);
    __android_log_vprint(ANDROID_LOG_ERROR, LOG_TAG, format, ap);
    va_end(ap);
}
