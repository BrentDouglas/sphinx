package io.machinecode.sphinx;

import io.machinecode.sphinx.dependency.DependencyArchiveProcessor;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * @author Stuart Douglas
 */
public class SphinxLoadableExtension implements LoadableExtension {
    @Override
    public void register(final ExtensionBuilder builder) {
        builder.observer(SphinxDeployer.class)
                .observer(ConfigurationProducer.class)
                .service(ApplicationArchiveProcessor.class, DependencyArchiveProcessor.class);
    }
}
