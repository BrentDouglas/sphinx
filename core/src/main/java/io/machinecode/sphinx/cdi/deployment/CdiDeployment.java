package io.machinecode.sphinx.cdi.deployment;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.naming.InitialContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@Singleton
@Startup
public class CdiDeployment {

    private static final Logger log = Logger.getLogger(CdiDeployment.class.getCanonicalName());

    public static final String RESOURCE = "sphinx-bean-manager-binding";
    public static final String SOCKET = "sphinx-socket-binding";

    @Inject
    private CdiExtension extension;

    private volatile boolean running;

    private volatile int port;
    private volatile InetAddress address;

    private volatile String binding;

    @PostConstruct
    private void create() throws Exception {
        try {
            binding = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(RESOURCE))).readLine();
        } catch (final Exception e) {
            throw new Exception("Could not resolve BeanManager global binding", e);
        }

        try {
            final String[] parts = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(SOCKET))).readLine().split(":");
            address = InetAddress.getByName(parts[0]);
            port = Integer.parseInt(parts[1]);
        } catch (final Exception e) {
            throw new Exception("Could not resolve socket binding", e);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                BeanManager beanManager;
                while (running) {
                    try {
                        beanManager = extension.getBeanManager();
                        if (beanManager == null) {
                            Thread.sleep(100);
                            continue;
                        }
                        try {
                            new InitialContext().bind(binding, beanManager);
                            break;
                        } catch (final Exception e) {
                            throw new Exception("Failed to bind BeanManager to " + binding, e);
                        }
                    } catch (final Exception e) {
                        //
                    }
                }

                while (running) {
                    try {
                        final Set<String> classes = extension.getClasses();
                        if (classes == null) {
                            Thread.sleep(100);
                            continue;
                        }
                        final Message message = new Message()
                                .setBinding(binding)
                                .setClasses(classes);

                        SocketChannel channel = null;
                        try {
                            channel = SocketChannel.open();
                            channel.configureBlocking(true);

                            if (!channel.connect(new InetSocketAddress(address, port))) {
                                log.info("Failed to connect to address: " + address.getCanonicalHostName() + ":" + port);
                            }

                            ObjectOutputStream stream = null;
                            try {
                                stream = new ObjectOutputStream(channel.socket().getOutputStream());
                                stream.writeObject(message);
                                stream.close();
                                running = false;
                            } finally {
                                try {
                                    if (stream != null) {
                                        stream.close();
                                    }
                                } catch (final IOException ioe) {
                                    //
                                }
                            }
                        } finally {
                            try {
                                if (channel != null) {
                                    channel.close();
                                }
                            } catch (final IOException ioe) {
                                //
                            }
                        }
                    } catch (final Exception e) {
                        //
                    }
                }
            }
        }).start();
    }

    @PreDestroy
    private void destroy() {
        running = false;
    }
}
