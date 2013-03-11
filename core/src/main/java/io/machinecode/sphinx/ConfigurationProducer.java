package io.machinecode.sphinx;

import io.machinecode.sphinx.config.SphinxConfig;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class ConfigurationProducer {

    @Inject
    @ApplicationScoped
    private InstanceProducer<SphinxConfig> producer;

    public void configure(@Observes ArquillianDescriptor descriptor) {
        for (final ExtensionDef extension : descriptor.getExtensions()) {
            if (SphinxConfig.EXTENSION_NAME.equals(extension.getExtensionName())) {
                final String filename = extension.getExtensionProperties().get(SphinxConfig.CONFIG_FILE);
                producer.set(SphinxConfig.getConfig(filename));
                return;
            }
        }
        throw new IllegalStateException("Sphinx config file name not set in arquillian.xml");
    }
}
