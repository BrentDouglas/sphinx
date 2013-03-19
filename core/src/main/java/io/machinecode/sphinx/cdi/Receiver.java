package io.machinecode.sphinx.cdi;

import io.machinecode.sphinx.cdi.deployment.Message;
import io.machinecode.sphinx.config.CdiConfig;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class Receiver implements Closeable {

    final ServerSocketChannel serverChannel;

    public Receiver(final CdiConfig config) throws DeploymentException {
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(true);
            serverChannel.socket().bind(new InetSocketAddress(InetAddress.getByName(config.getBindAddress()), config.getPort()));
        } catch (final IOException e) {
            throw new DeploymentException("Failed to open channel", e);
        }
    }

    public Message receive() throws Exception {
        SocketChannel channel = null;
        try {
            channel = serverChannel.accept();

            ObjectInputStream stream = null;
            try {
                stream = new ObjectInputStream(channel.socket().getInputStream());
                return (Message) stream.readObject();
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (final IOException e) {
                    //
                }
            }
        } finally {
            try {
                if (channel != null) {
                    channel.close();
                }
            } catch (final IOException e) {
                //
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            serverChannel.close();
        } catch (IOException e) {
            //
        }
    }
}
