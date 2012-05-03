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

#ifndef __MTPD_H__
#define __MTPD_H__

/* The socket to the server. */
extern int the_socket;

enum exit_code {
    SYSTEM_ERROR = 1,
    NETWORK_ERROR = 2,
    PROTOCOL_ERROR = 3,
    CHALLENGE_FAILED = 4,
    USER_REQUESTED = 5,
    REMOTE_REQUESTED = 6,
    PPPD_EXITED = 32,
};

enum log_level {
    DEBUG = 0,
    INFO = 1,
    WARNING = 2,
    ERROR = 3,
    FATAL = 4,
    LOG_MAX = 4,
};

void log_print(int level, char *format, ...);
void create_socket(int family, int type, char *server, char *port);
void start_pppd(int pppox);

/* Each protocol must implement everything defined in this structure. Note that
 * timeout intervals are in milliseconds, where zero means forever. To indicate
 * an error, one should use a negative exit code such as -REMOTE_REQUESTED. */
struct protocol {
    /* The name of this protocol. */
    char *name;
    /* The number of arguments. */
    int arguments;
    /* The usage of the arguments. */
    char *usage;
    /* Connect to the server and return the next timeout interval. */
    int (*connect)(char **arguments);
    /* Process the incoming packet and return the next timeout interval. */
    int (*process)();
    /* Handle the timeout event and return the next timeout interval. */
    int (*timeout)();
    /* Handle the shutdown event. */
    void (*shutdown)();
};

#endif /* __MTPD_H__ */
