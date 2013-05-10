package io.machinecode.sphinx.browser;

import io.machinecode.sphinx.browser.api.BrowserTest;
import io.machinecode.sphinx.config.BrowserStackConfig;
import io.machinecode.sphinx.config.BrowserStackHostConfig;
import io.machinecode.sphinx.config.SphinxConfig;
import org.jboss.arquillian.container.spi.event.container.BeforeStop;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;

/**
 * This class is responsible for starting the browserstack tunnel when it encounters
 * the first test to use it.
 *
 * It is also responsible for shutting down the tunnel after the tests have run.
 *
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class BrowserstackArchiveProcessor implements ApplicationArchiveProcessor {

    @Inject
    private Instance<SphinxConfig> instance;

    private static volatile Thread thread;

    @Override
    public void process(final Archive<?> archive, final TestClass testClass) {
        final BrowserStackConfig config = instance.get().getBrowserStack();
        if (config == null || thread != null) {
            return;
        }

        if (testClass.getMethods(BrowserTest.class).length == 0) {
            return;
        }

        final StringBuilder builder = new StringBuilder();
        for (final BrowserStackHostConfig hostConfig : config.getHosts()) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(hostConfig.getHostname())
                    .append(',')
                    .append(hostConfig.getPort())
                    .append(',')
                    .append(hostConfig.isSsl() ? 1 : 0);
        }

        (thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Class.forName("CommandLine")
                            .getMethod("main", String[].class)
                            .invoke(null, new Object[]{new String[]{config.getTunnelKey(), builder.toString()}});
                } catch (final Exception e) {
                    throw new IllegalStateException("Could not start browserstack tunnel.", e);
                }
            }
        })).start();
    }

    public void undeploy(@Observes final BeforeStop event) throws Exception {
        if (instance.get().getBrowserStack() == null || thread == null) {
            return;
        }
        thread.stop();
    }
}
