package io.machinecode.sphinx;

import io.machinecode.sphinx.config.SphinxConfig;
import io.machinecode.sphinx.dependency.DependencyDeployer;
import io.machinecode.sphinx.sql.DatabaseDeployer;
import io.machinecode.sphinx.util.ArchiveUtil;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeStop;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class SphinxDeployer  {

    @Inject
    private Instance<SphinxConfig> instance;

    private static final List<Deployer> deployers = Arrays.asList(
            new DatabaseDeployer(),
            new DependencyDeployer()
    );

    private boolean deployed = false;
    private DeploymentException failure;

    public synchronized void deploy(@Observes final BeforeDeploy event, final Container container) throws DeploymentException {
        if (failure != null) {
            throw failure;
        }
        if (deployed) {
            return;
        }

        final SphinxConfig config = instance.get();
        ArchiveUtil.setTempDir(config.getTempDir());

        final List<Deployer> run = new ArrayList<Deployer>(deployers.size());
        try {
            for (final Deployer deployer : deployers) {
                deployer.deploy(event, container, config);
                run.add(deployer);
            }
            deployed = true;
        } catch (final DeploymentException e) {
            failure = e;
            for (final Deployer deployer : run) {
                try {
                    deployer.deploy(event, container, config);
                } catch (final DeploymentException de) {
                    e.addSuppressed(de);
                }
            }
            ArchiveUtil.cleanUp();
            throw failure;
        }
    }

    public synchronized void undeploy(@Observes final BeforeStop event, final Container container) throws DeploymentException {
        final SphinxConfig config = instance.get();

        for (final Deployer deployer : deployers) {
            deployer.undeploy(container, config);
        }

        ArchiveUtil.cleanUp();
    }
}
