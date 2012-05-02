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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * helper for executing shell commands
 * 
 * @author ywu
 * 
 */
public final class Shell {
    private static final Logger LOG = LoggerFactory.getLogger("xink.Shell");

    private Shell() {
        // no instance
    }

    /**
     * execute cmd in a shell
     * 
     * @param cmd command line to execute
     * @param blocking block to read outputs
     */
    public static void exec(final String cmd, final boolean blocking) {
        try {
            exec("sh", cmd, blocking);
        } catch (IOException e) {
            throw new SysException("failed to exec: " + cmd, e);
        }
    }

    /**
     * execute cmd in a shell, as root
     * 
     * @param cmd command line to execute
     * @param blocking block to read outputs
     */
    public static void sudo(final String cmd, final boolean blocking) {
        try {
            exec("su", cmd, blocking);
        } catch (IOException e) {
            throw new SysException("failed to sudo: " + cmd, e);
        }
    }

    /**
     * execute cmd in the specified shell program
     * 
     * @param shell the shell program
     * @param cmd command line to execute
     * @param blocking block to read outputs
     * @throws IOException whenever failure
     */
    private static void exec(final String shell, final String cmd, final boolean blocking) throws IOException {
        Process process = null;

        try {
            process = new ProcessBuilder(shell).redirectErrorStream(true).start();

            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();

            if (!blocking || process.getInputStream().available() == 0)
                return;

            dumpCmdPrompt(process);
        } finally {
            if (process != null) {
                // process.destroy();
            }
        }
    }

    private static void dumpCmdPrompt(final Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String ln;

        while ((ln = reader.readLine()) != null) {
            LOG.debug(ln);
        }
    }

}
