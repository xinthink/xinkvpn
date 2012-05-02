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
package xink.sys;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xink.vpn.AppException;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;

/**
 * system mtpd service wrapper
 * 
 * @author ywu
 * 
 */
public final class Mtpd {

    private static final Logger LOG = LoggerFactory.getLogger("xink.mtpd");

    private static final String DAEMON = "xinkvpnd";
    private static final String SOCK_PATH = "/dev/socket/xinkvpnd";

    private static final int MAX_ARG_BYTES = 0xFFFF;
    private static final short ARG_END = 0xFF;
    private static final int CONN_RETRIES = 3;
    private static final int SOCK_RETRY_INTERV = 200;

    private static final short RET_OK = 0;
    private static final short CMD_ALIVE = 0x00;
    private static final short CMD_START = 0x01;
    private static final short CMD_STOP = 0x02;

    private Mtpd() {

    }

    /**
     * Start PPTP service.
     */
    public static void startPptp(final String server, final String user, final String passwd, final boolean encrypted) {
        String[] args = { "pptp", server, "1723", "", "linkname", "vpn", "name", user, "password", passwd,
                "refuse-eap", "nodefaultroute", "usepeerdns", "idle", "1800", "mtu", "1400", "mru", "1400",
                encrypted ? "+mppe" : "nomppe" };
        start(args);
    }

    public static void start(final String[] args) {
        startDaemon();

        try {
            sendCmd(CMD_START, args);
        } catch (IOException e) {
            throw new AppException("failed to start mtpd", e);
        }
    }

    /*
     * Establish control socket
     */
    private static LocalSocket getCtrlSock() throws IOException {
        LocalSocket socket = new LocalSocket();
        LocalSocketAddress addr = new LocalSocketAddress(SOCK_PATH, LocalSocketAddress.Namespace.FILESYSTEM);

        IOException ex = null;
        for (int i = 0; i < CONN_RETRIES; i++) {
            try {
                socket.connect(addr);
                return socket;
            } catch (IOException e) {
                ex = e;
                sleep(SOCK_RETRY_INTERV);
            }
        }

        throw ex;
    }

    private static void sendCmd(final short cmd, final String... args) throws IOException {
        LocalSocket so = getCtrlSock();

        try {
            sendCmd(so, cmd, args);
        } finally {
            so.close();
        }
    }

    private static void sendCmd(final LocalSocket so, final short cmd, final String... args) throws IOException {
        OutputStream out = so.getOutputStream();
        out.write(cmd);
        out.flush();

        for (String argument : args) {
            byte[] bytes = argument.getBytes();
            if (bytes.length >= MAX_ARG_BYTES)
                throw new IllegalArgumentException("Argument is too large");
            out.write(bytes.length);
            out.write(bytes);
            out.flush();
        }
        out.write(ARG_END);
        out.flush();

        // read result
        InputStream in = so.getInputStream();
        int ret = in.read();

        if (ret != RET_OK)
            throw new IOException("socket return error: " + ret);
    }

    /*
     * ensure the daemon is running
     */
    private static void startDaemon() {
        boolean alive = false;
        try {
            alive = checkAlive();
        } catch (IOException e) {
            // igonre
        }

        if (!alive) {
            LOG.info("xinkvpn daemon is not running, start it");
            Shell.sudo(DAEMON, false);
        }
    }
    
    private static boolean checkAlive() throws IOException {
        sendCmd(CMD_ALIVE);
        return true;
    }

    public static void stop() {
        try {
            sendCmd(CMD_STOP);
        } catch (IOException e) {
            throw new AppException("failed to stop daemon", e);
        }
    }

    private static void sleep(final int msec) {
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
