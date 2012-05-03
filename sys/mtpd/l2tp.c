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

/* A simple implementation of L2TP Access Concentrator (RFC 2661) which only
 * creates a single session. The following code only handles control packets.
 * Data packets are handled by PPPoLAC driver which can be found in Android
 * kernel tree. */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <arpa/inet.h>
#include <linux/netdevice.h>
#include <linux/if_pppox.h>
#include <openssl/md5.h>

#include "mtpd.h"

/* To avoid unnecessary endianness conversions, tunnels, sessions, attributes,
 * and values are all accessed in network order. */

/* 0 is reserved. We put ACK here just for convenience. */
enum l2tp_message {
    ACK = 0,
    SCCRQ = 1,
    SCCRP = 2,
    SCCCN = 3,
    STOPCCN = 4,
    HELLO = 6,
    OCRQ = 7,
    OCRP = 8,
    OCCN = 9,
    ICRQ = 10,
    ICRP = 11,
    ICCN = 12,
    CDN = 14,
    WEN = 15,
    SLI = 16,
    MESSAGE_MAX = 16,
};

static char *messages[] = {
    "ACK", "SCCRQ", "SCCRP", "SCCCN", "STOPCCN", NULL, "HELLO", "OCRQ",
    "OCRP", "OCCN", "ICRQ", "ICRP", "ICCN", NULL, "CDN", "WEN", "SLI",
};

/* This is incomplete. Only those we used are listed here. */
#define RESULT_CODE             htons(1)
#define PROTOCOL_VERSION        htons(2)
#define FRAMING_CAPABILITIES    htons(3)
#define HOST_NAME               htons(7)
#define ASSIGNED_TUNNEL         htons(9)
#define WINDOW_SIZE             htons(10)
#define CHALLENGE               htons(11)
#define CHALLENGE_RESPONSE      htons(13)
#define ASSIGNED_SESSION        htons(14)
#define CALL_SERIAL_NUMBER      htons(15)
#define FRAMING_TYPE            htons(19)
#define CONNECT_SPEED           htons(24)
#define RANDOM_VECTOR           htons(36)

#define MESSAGE_FLAG            0xC802
#define MESSAGE_MASK            0xCB0F
#define ATTRIBUTE_FLAG(length)  (0x8006 + (length))
#define ATTRIBUTE_LENGTH(flag)  (0x03FF & (flag))
#define ATTRIBUTE_HIDDEN(flag)  (0x4000 & (flag))

#define ACK_SIZE                12
#define MESSAGE_HEADER_SIZE     20
#define ATTRIBUTE_HEADER_SIZE   6
#define MAX_ATTRIBUTE_SIZE      1024

static uint16_t local_tunnel;
static uint16_t local_session;
static uint16_t local_sequence;
static uint16_t remote_tunnel;
static uint16_t remote_session;
static uint16_t remote_sequence;

static uint16_t state;
static int acknowledged;

#define RANDOM_DEVICE   "/dev/urandom"
#define CHALLENGE_SIZE  32

static char *secret;
static int secret_length;
static uint8_t challenge[CHALLENGE_SIZE];

/* According to RFC 2661 page 46, an exponential backoff strategy is required
 * for retransmission. However, it might waste too much time waiting for IPsec
 * negotiation. Here we use the same interval to keep things simple. */
#define TIMEOUT_INTERVAL 2000

#define MAX_PACKET_LENGTH 2048

static struct packet {
    int message;
    int length;
    uint8_t buffer[MAX_PACKET_LENGTH] __attribute__((aligned(4)));
} incoming, outgoing;

struct attribute {
    uint16_t flag;
    uint16_t vendor;
    uint16_t type;
    uint8_t value[1];
} __attribute__((packed));

static void set_message(uint16_t session, uint16_t message)
{
    uint16_t *p = (uint16_t *)outgoing.buffer;
    p[0] = htons(MESSAGE_FLAG);
    /* p[1] will be filled in send_packet(). */
    p[2] = remote_tunnel;
    p[3] = session;
    p[4] = htons(local_sequence);
    p[5] = htons(remote_sequence);
    p[6] = htons(ATTRIBUTE_FLAG(2));
    p[7] = 0;
    p[8] = 0;
    p[9] = htons(message);
    outgoing.message = message;
    outgoing.length = MESSAGE_HEADER_SIZE;
    ++local_sequence;
}

static void add_attribute_raw(uint16_t type, void *value, int size)
{
    struct attribute *p = (struct attribute *)&outgoing.buffer[outgoing.length];
    p->flag = htons(ATTRIBUTE_FLAG(size));
    p->vendor = 0;
    p->type = type;
    memcpy(&p->value, value, size);
    outgoing.length += ATTRIBUTE_HEADER_SIZE + size;
}

static void add_attribute_u16(uint16_t attribute, uint16_t value)
{
    add_attribute_raw(attribute, &value, sizeof(uint16_t));
}

static void add_attribute_u32(uint16_t attribute, uint32_t value)
{
    add_attribute_raw(attribute, &value, sizeof(uint32_t));
}

static void send_packet()
{
    uint16_t *p = (uint16_t *)outgoing.buffer;
    p[1] = htons(outgoing.length);
    send(the_socket, outgoing.buffer, outgoing.length, 0);
    acknowledged = 0;
}

static void send_ack()
{
    uint16_t buffer[6] = {
        htons(MESSAGE_FLAG), htons(ACK_SIZE), remote_tunnel, 0,
        htons(local_sequence), htons(remote_sequence),
    };
    send(the_socket, buffer, ACK_SIZE, 0);
}

static int recv_packet(uint16_t *session)
{
    uint16_t *p = (uint16_t *)incoming.buffer;

    incoming.length = recv(the_socket, incoming.buffer, MAX_PACKET_LENGTH, 0);
    if (incoming.length == -1) {
        if (errno == EINTR) {
            return 0;
        }
        log_print(FATAL, "Recv() %s", strerror(errno));
        exit(NETWORK_ERROR);
    }

    /* We only handle packets in our tunnel. */
    if ((incoming.length != ACK_SIZE && incoming.length < MESSAGE_HEADER_SIZE)
            || (p[0] & htons(MESSAGE_MASK)) != htons(MESSAGE_FLAG) ||
            ntohs(p[1]) != incoming.length || p[2] != local_tunnel) {
        return 0;
    }

    if (incoming.length == ACK_SIZE) {
        incoming.message = ACK;
    } else if (p[6] == htons(ATTRIBUTE_FLAG(2)) && !p[7] && !p[8]) {
        incoming.message = ntohs(p[9]);
    } else {
        return 0;
    }

    /* Check if the packet is duplicated and send ACK if necessary. */
    if ((uint16_t)(ntohs(p[4]) - remote_sequence) > 32767) {
        if (incoming.message != ACK) {
            send_ack();
        }
        return 0;
    }

    if (ntohs(p[5]) == local_sequence) {
        acknowledged = 1;
    }

    /* Our sending and receiving window sizes are both 1. Thus we only handle
     * this packet if it is their next one and they received our last one. */
    if (ntohs(p[4]) != remote_sequence || !acknowledged) {
        return 0;
    }
    *session = p[3];
    if (incoming.message != ACK) {
        ++remote_sequence;
    }
    return 1;
}

static int get_attribute_raw(uint16_t type, void *value, int size)
{
    int offset = MESSAGE_HEADER_SIZE;
    uint8_t *vector = NULL;
    int vector_length = 0;

    while (incoming.length >= offset + ATTRIBUTE_HEADER_SIZE) {
        struct attribute *p = (struct attribute *)&incoming.buffer[offset];
        uint16_t flag = ntohs(p->flag);
        int length = ATTRIBUTE_LENGTH(flag);

        offset += length;
        length -= ATTRIBUTE_HEADER_SIZE;
        if (length < 0 || offset > incoming.length) {
            break;
        }
        if (p->vendor) {
            continue;
        }
        if (p->type != type) {
            if (p->type == RANDOM_VECTOR && !ATTRIBUTE_HIDDEN(flag)) {
                vector = p->value;
                vector_length = length;
            }
            continue;
        }

        if (!ATTRIBUTE_HIDDEN(flag)) {
            if (size > length) {
                size = length;
            }
            memcpy(value, p->value, size);
            return size;
        }

        if (!secret || !vector || length < 2) {
            return 0;
        } else {
            uint8_t buffer[MAX_ATTRIBUTE_SIZE];
            uint8_t hash[MD5_DIGEST_LENGTH];
            MD5_CTX ctx;
            int i;

            MD5_Init(&ctx);
            MD5_Update(&ctx, &type, sizeof(uint16_t));
            MD5_Update(&ctx, secret, secret_length);
            MD5_Update(&ctx, vector, vector_length);
            MD5_Final(hash, &ctx);

            for (i = 0; i < length; ++i) {
                int j = i % MD5_DIGEST_LENGTH;
                if (i && !j) {
                    MD5_Init(&ctx);
                    MD5_Update(&ctx, secret, secret_length);
                    MD5_Update(&ctx, &p->value[i - MD5_DIGEST_LENGTH],
                        MD5_DIGEST_LENGTH);
                    MD5_Final(hash, &ctx);
                }
                buffer[i] = p->value[i] ^ hash[j];
            }

            length = buffer[0] << 8 | buffer[1];
            if (length > i - 2) {
                return 0;
            }
            if (size > length) {
                size = length;
            }
            memcpy(value, &buffer[2], size);
            return size;
        }
    }
    return 0;
}

static int get_attribute_u16(uint16_t type, uint16_t *value)
{
    return get_attribute_raw(type, value, sizeof(uint16_t)) == sizeof(uint16_t);
}

static int l2tp_connect(char **arguments)
{
    create_socket(AF_INET, SOCK_DGRAM, arguments[0], arguments[1]);

    while (!local_tunnel) {
        local_tunnel = random();
    }

    log_print(DEBUG, "Sending SCCRQ (local_tunnel = %d)", local_tunnel);
    state = SCCRQ;
    set_message(0, SCCRQ);
    add_attribute_u16(PROTOCOL_VERSION, htons(0x0100));
    add_attribute_raw(HOST_NAME, "anonymous", 9);
    add_attribute_u32(FRAMING_CAPABILITIES, htonl(3));
    add_attribute_u16(ASSIGNED_TUNNEL, local_tunnel);
    add_attribute_u16(WINDOW_SIZE, htons(1));

    if (arguments[2][0]) {
        int fd = open(RANDOM_DEVICE, O_RDONLY);
        if (fd == -1 || read(fd, challenge, CHALLENGE_SIZE) != CHALLENGE_SIZE) {
            log_print(FATAL, "Cannot read %s", RANDOM_DEVICE);
            exit(SYSTEM_ERROR);
        }
        close(fd);

        add_attribute_raw(CHALLENGE, challenge, CHALLENGE_SIZE);
        secret = arguments[2];
        secret_length = strlen(arguments[2]);
    }

    send_packet();
    return TIMEOUT_INTERVAL;
}

static int create_pppox()
{
    int pppox = socket(AF_PPPOX, SOCK_DGRAM, PX_PROTO_OLAC);
    log_print(INFO, "Creating PPPoX socket");

    if (pppox == -1) {
        log_print(FATAL, "Socket() %s", strerror(errno));
        exit(SYSTEM_ERROR);
    } else {
        struct sockaddr_pppolac address = {
            .sa_family = AF_PPPOX,
            .sa_protocol = PX_PROTO_OLAC,
            .udp_socket = the_socket,
            .local = {.tunnel = local_tunnel, .session = local_session},
            .remote = {.tunnel = remote_tunnel, .session = remote_session},
        };
        if (connect(pppox, (struct sockaddr *)&address, sizeof(address))) {
            log_print(FATAL, "Connect() %s", strerror(errno));
            exit(SYSTEM_ERROR);
        }
    }
    return pppox;
}

static uint8_t *compute_response(uint8_t type, void *challenge, int size)
{
    static uint8_t response[MD5_DIGEST_LENGTH];
    MD5_CTX ctx;
    MD5_Init(&ctx);
    MD5_Update(&ctx, &type, sizeof(uint8_t));
    MD5_Update(&ctx, secret, secret_length);
    MD5_Update(&ctx, challenge, size);
    MD5_Final(response, &ctx);
    return response;
}

static int verify_challenge()
{
    if (secret) {
        uint8_t response[MD5_DIGEST_LENGTH];
        if (get_attribute_raw(CHALLENGE_RESPONSE, response, MD5_DIGEST_LENGTH)
                != MD5_DIGEST_LENGTH) {
            return 0;
        }
        return !memcmp(compute_response(SCCRP, challenge, CHALLENGE_SIZE),
                response, MD5_DIGEST_LENGTH);
    }
    return 1;
}

static void answer_challenge()
{
    if (secret) {
        uint8_t challenge[MAX_ATTRIBUTE_SIZE];
        int size = get_attribute_raw(CHALLENGE, challenge, MAX_ATTRIBUTE_SIZE);
        if (size > 0) {
            uint8_t *response = compute_response(SCCCN, challenge, size);
            add_attribute_raw(CHALLENGE_RESPONSE, response, MD5_DIGEST_LENGTH);
        }
    }
}

static int l2tp_process()
{
    uint16_t sequence = local_sequence;
    uint16_t tunnel = 0;
    uint16_t session = 0;

    if (!recv_packet(&session)) {
        return acknowledged ? 0 : TIMEOUT_INTERVAL;
    }

    /* Here is the fun part. We always try to protect our tunnel and session
     * from being closed even if we received unexpected messages. */
    switch(incoming.message) {
        case SCCRP:
            if (state == SCCRQ) {
                if (get_attribute_u16(ASSIGNED_TUNNEL, &tunnel) && tunnel &&
                        verify_challenge()) {
                    remote_tunnel = tunnel;
                    log_print(DEBUG, "Received SCCRP (remote_tunnel = %d) -> "
                            "Sending SCCCN", remote_tunnel);
                    state = SCCCN;
                    answer_challenge();
                    set_message(0, SCCCN);
                    break;
                }
                log_print(DEBUG, "Received SCCRP without %s", tunnel ?
                        "valid challenge response" : "assigned tunnel");
                log_print(ERROR, "Protocol error");
                return tunnel ? -CHALLENGE_FAILED : -PROTOCOL_ERROR;
            }
            break;

        case ICRP:
            if (state == ICRQ && session == local_session) {
                if (get_attribute_u16(ASSIGNED_SESSION, &session) && session) {
                    remote_session = session;
                    log_print(DEBUG, "Received ICRP (remote_session = %d) -> "
                            "Sending ICCN", remote_session);
                    state = ICCN;
                    set_message(remote_session, ICCN);
                    add_attribute_u32(CONNECT_SPEED, htonl(100000000));
                    add_attribute_u32(FRAMING_TYPE, htonl(3));
                    break;
                }
                log_print(DEBUG, "Received ICRP without assigned session");
                log_print(ERROR, "Protocol error");
                return -PROTOCOL_ERROR;
            }
            break;

        case STOPCCN:
            log_print(DEBUG, "Received STOPCCN");
            log_print(INFO, "Remote server hung up");
            state = STOPCCN;
            return -REMOTE_REQUESTED;

        case CDN:
            if (session && session == local_session) {
                log_print(DEBUG, "Received CDN (local_session = %d)",
                        local_session);
                log_print(INFO, "Remote server hung up");
                return -REMOTE_REQUESTED;
            }
            break;

        case ACK:
        case HELLO:
        case WEN:
        case SLI:
            /* These are harmless, so we just treat them in the same way. */
            if (state == SCCCN) {
                while (!local_session) {
                    local_session = random();
                }
                log_print(DEBUG, "Received %s -> Sending ICRQ (local_session = "
                        "%d)", messages[incoming.message], local_session);
                log_print(INFO, "Tunnel established");
                state = ICRQ;
                set_message(0, ICRQ);
                add_attribute_u16(ASSIGNED_SESSION, local_session);
                add_attribute_u32(CALL_SERIAL_NUMBER, random());
                break;
            }

            if (incoming.message == ACK) {
                log_print(DEBUG, "Received ACK");
            } else {
                log_print(DEBUG, "Received %s -> Sending ACK",
                          messages[incoming.message]);
                send_ack();
            }

            if (state == ICCN) {
                log_print(INFO, "Session established");
                state = ACK;
                start_pppd(create_pppox());
            }
            return 0;

        case ICRQ:
        case OCRQ:
            /* Since we run pppd as a client, it does not makes sense to
             * accept ICRQ or OCRQ. Always send CDN with a proper error. */
            if (get_attribute_u16(ASSIGNED_SESSION, &session) && session) {
                log_print(DEBUG, "Received %s (remote_session = %d) -> "
                        "Sending CDN", messages[incoming.message], session);
                set_message(session, CDN);
                add_attribute_u32(RESULT_CODE, htonl(0x00020006));
                add_attribute_u16(ASSIGNED_SESSION, 0);
            }
            break;
    }

    if (sequence != local_sequence) {
        send_packet();
        return TIMEOUT_INTERVAL;
    }

    /* We reach here if we got an unexpected message. Log it and send ACK. */
    if (incoming.message > MESSAGE_MAX || !messages[incoming.message]) {
        log_print(DEBUG, "Received UNKNOWN %d -> Sending ACK anyway",
                incoming.message);
    } else {
        log_print(DEBUG, "Received UNEXPECTED %s -> Sending ACK anyway",
                messages[incoming.message]);
    }
    send_ack();
    return 0;
}

static int l2tp_timeout()
{
    if (acknowledged) {
        return 0;
    }
    log_print(DEBUG, "Timeout -> Sending %s", messages[outgoing.message]);
    send(the_socket, outgoing.buffer, outgoing.length, 0);
    return TIMEOUT_INTERVAL;
}

static void l2tp_shutdown()
{
    if (state != STOPCCN) {
        log_print(DEBUG, "Sending STOPCCN");
        set_message(0, STOPCCN);
        add_attribute_u16(ASSIGNED_TUNNEL, local_tunnel);
        add_attribute_u16(RESULT_CODE, htons(6));
        send_packet();
    }
}

struct protocol l2tp = {
    .name = "l2tp",
    .arguments = 3,
    .usage = "<server> <port> <secret>",
    .connect = l2tp_connect,
    .process = l2tp_process,
    .timeout = l2tp_timeout,
    .shutdown = l2tp_shutdown,
};
