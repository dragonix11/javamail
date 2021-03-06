/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.mail.imap;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * IMAP Server.
 *
 * Inspired by, and derived from, POP3Server by sbo.
 *
 * @author sbo
 * @author Bill Shannon
 */
public final class IMAPServer extends Thread {

    /** Server socket. */
    private ServerSocket serverSocket;

    /** Keep on? */
    private volatile boolean keepOn;

    /** Port to listen. */
    private final int port;

    /** IMAP handler. */
    private final IMAPHandler handler;

    /**
     * IMAP server.
     *
     * @param handler
     *            handler
     * @param port
     *            port to listen
     */
    public IMAPServer(final IMAPHandler handler, final int port) {
        this.handler = handler;
        this.port = port;
    }

    /**
     * Exit IMAP server.
     */
    public void quit() {
        try {
            keepOn = false;
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Open a server socket.
     *
     * @return server socket
     * @throws IOException unable to open socket
     */
    private ServerSocket openServerSocket() throws IOException {
        return new ServerSocket(port, 0, null);
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        try {
            keepOn = true;

            try {
                serverSocket = openServerSocket();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }

            while (keepOn) {
                try {
                    final Socket clientSocket = serverSocket.accept();
                    final IMAPHandler imapHandler =
			(IMAPHandler)handler.clone();
                    imapHandler.setClientSocket(clientSocket);
                    imapHandler.start();
                } catch (final IOException e) {
                    //e.printStackTrace();
                }
            }
        } finally {
            quit();
        }
    }
}
