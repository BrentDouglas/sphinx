package io.machinecode.spinx;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * @author Stuart Douglas
 */
public class SphinxLoadableExtension implements LoadableExtension {
    @Override
    public void register(ExtensionBuilder extensionBuilder) {
        extensionBuilder.observer(SphinxDeployer.class);
        extensionBuilder.service(ApplicationArchiveProcessor.class, SphinxArchiveProcessor.class);

    }
}
