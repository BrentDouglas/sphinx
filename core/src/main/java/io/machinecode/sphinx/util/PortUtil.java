package io.machinecode.sphinx.util;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class PortUtil {

    public static int firstAvailableTcp() {
        int port = 1024;
        while (port < 65535) {
            ServerSocket socket = null;
            try {
                socket = new ServerSocket(port);
                socket.setReuseAddress(true);
                return port;
            } catch (final IOException e) {
                //
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (final IOException e) {
                        //
                    }
                }
            }
            ++port;
        }
        throw new IllegalArgumentException("Invalid start port: " + port);
    }
}
