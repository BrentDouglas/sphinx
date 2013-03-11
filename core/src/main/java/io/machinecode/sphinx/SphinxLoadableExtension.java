package io.machinecode.sphinx;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * @author Stuart Douglas
 */
public class SphinxLoadableExtension implements LoadableExtension {
    @Override
    public void register(final ExtensionBuilder builder) {
        builder.observer(ArchiveDeployer.class)
                .observer(ConfigurationProducer.class)
                .service(ApplicationArchiveProcessor.class, ArchiveProcessor.class);
    }
}
