package io.machinecode.sphinx;

import io.machinecode.sphinx.config.SphinxConfig;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface Deployer {

    void deploy(final Container container, final SphinxConfig config) throws DeploymentException;

    void undeploy(final Container container, final SphinxConfig config) throws DeploymentException;
}
