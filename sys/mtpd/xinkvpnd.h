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

#ifndef __XINKVPND_H__
#define __XINKVPND_H__

#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <stdarg.h>
#include <string.h>
#include <errno.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <fcntl.h>
#include <android/log.h>
#include <cutils/properties.h>

#define SOCK_PATH   "/dev/socket/xinkvpnd"
#define LOG_TAG     "xinkvpnd"
#define MAX_CONN    3
#define MAX_ARGS    256

#define CMD_ALIVE   0x00
#define CMD_START   0x01
#define CMD_STOP    0x02

int handle_conn(const int conn);
int do_start(const int argc, const char **argv);

// -----------------------------------------------------------------------
// system daemons
//
#define MTPD        "mtpd"
#define MTPD_SOCK   "/dev/socket/mtpd"

struct daemon {
    const char *name;
    const char *sock;
} mtpd = { MTPD, MTPD_SOCK };

#define SVC_START_CMD           "ctl.start"
#define SVC_STOP_CMD            "ctl.stop"
#define SVC_STATE_RUNNING       "running"
#define SVC_STATE_STOPPED       "stopped"
#define SVC_STATE_CMD_PREFIX    "init.svc."

#define SVC_RETRY               5
#define SVC_RETRY_INTV          1
#define SVC_ARGS_END            0xFF

int start_daemon(const struct daemon *d);
int get_svc_sock(const struct daemon *d);
int stop_daemon(const struct daemon *d);
int wait_svc_state(const char *daemon, const char *expectedState);

// -----------------------------------------------------------------------
// log utils
//
void log_d(const char *format, ...);
void log_i(const char *format, ...);
void log_e(const char *format, ...);

#endif /* __XINKVPND_H__ */
