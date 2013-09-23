package io.machinecode.sphinx.dependency;

import io.machinecode.sphinx.Deployer;
import io.machinecode.sphinx.config.ArchiveConfig;
import io.machinecode.sphinx.config.ReplacementConfig;
import io.machinecode.sphinx.config.SphinxConfig;
import io.machinecode.sphinx.util.ArchiveUtil;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stuart Douglas
 */
public class DependencyDeployer implements Deployer {

    private static final Logger log = Logger.getLogger(DependencyDeployer.class);

    private List<Archive> archives = new ArrayList<Archive>();

    @Override
    public void deploy(final Container container, final SphinxConfig config) throws DeploymentException {
        for (final ArchiveConfig dependency : config.getArchives()) {
            final String deployment = dependency.getPathToArchive();
            final File file = new File(deployment.trim());
            if (!file.exists()) {
                throw new DeploymentException("File " + file + " does not exist");
            }
            final Archive<?> archive;
            if (file.getName().endsWith(".ear")) {
                archive = ShrinkWrap.createFromZipFile(EnterpriseArchive.class, file);
            } else if (file.getName().endsWith(".war")) {
                archive = ShrinkWrap.createFromZipFile(WebArchive.class, file);
            } else {
                archive = ShrinkWrap.createFromZipFile(JavaArchive.class, file);
            }

            for (final ReplacementConfig replacement : dependency.getReplacements()) {
                try {
                    ArchiveUtil.replace(archive, replacement.getExisting(), new File(replacement.getReplacement()), replacement.isSubstituteProperties());
                } catch (IOException e) {
                    throw new DeploymentException("Failed replacing file " + replacement.getExisting() + " with " + replacement.getReplacement(), e);
                }
            }

            archives.add(archive);
            log.info("Deploying " + archive.getName());
            container.getDeployableContainer().deploy(archive);
        }
    }

    @Override
    public void undeploy(final Container container, final SphinxConfig config) throws DeploymentException {
        final DeployableContainer<?> deployableContainer = container.getDeployableContainer();
        for (final Archive archive : archives) {
            log.info("Undeploying " + archive.getName());
            deployableContainer.undeploy(archive);
        }
    }
}
