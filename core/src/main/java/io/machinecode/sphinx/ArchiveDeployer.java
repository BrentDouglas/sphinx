package io.machinecode.sphinx;

import io.machinecode.sphinx.config.ArchiveConfig;
import io.machinecode.sphinx.config.ReplacementConfig;
import io.machinecode.sphinx.config.SphinxConfig;
import io.machinecode.sphinx.util.ArchiveUtil;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeStop;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Stuart Douglas
 */
public class ArchiveDeployer {

    @Inject
    private Instance<SphinxConfig> instance;

    private List<Archive> archives = new ArrayList<Archive>();
    private boolean deployed = false;
    private Set<ArchiveUtil> cleanup = new HashSet<ArchiveUtil>(); //TODO

    public synchronized void deploy(@Observes final BeforeDeploy event, final Container container) throws DeploymentException {
        if (deployed) {
            return;
        }
        deployed = true;

        final SphinxConfig config = instance.get();

        ArchiveUtil.setTempDir(config.getTempDir());

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

            for (final ReplacementConfig replacement: dependency.getReplacements()) {
                try {
                    cleanup.add(ArchiveUtil.replace(archive, replacement.getExisting(), new File(replacement.getReplacement())));
                } catch (IOException e) {
                    cleanUp();
                    throw new DeploymentException("Failed replacing file " + replacement.getExisting() + " with " + replacement.getReplacement(), e);
                }
            }

            archives.add(archive);
            container.getDeployableContainer().deploy(archive);
        }
    }

    public synchronized void undeploy(@Observes final BeforeStop event, final Container container) throws DeploymentException {
        for (final Archive archive : archives) {
            final DeployableContainer<?> deployableContainer = container.getDeployableContainer();
            deployableContainer.undeploy(archive);
        }
        cleanUp();
    }

    private void cleanUp() {
        for (final ArchiveUtil util : cleanup) {
            util.cleanUp();
        }
    }
}
