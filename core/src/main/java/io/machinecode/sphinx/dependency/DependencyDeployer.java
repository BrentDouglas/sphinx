package io.machinecode.sphinx.dependency;

import io.machinecode.sphinx.Deployer;
import io.machinecode.sphinx.cdi.CdiArchiveProcessor;
import io.machinecode.sphinx.cdi.CdiProducerProducer;
import io.machinecode.sphinx.cdi.Receiver;
import io.machinecode.sphinx.cdi.deployment.Message;
import io.machinecode.sphinx.config.ArchiveConfig;
import io.machinecode.sphinx.config.ReplacementConfig;
import io.machinecode.sphinx.config.SphinxConfig;
import io.machinecode.sphinx.util.ArchiveUtil;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
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
import java.util.Set;

/**
 * @author Stuart Douglas
 */
public class DependencyDeployer implements Deployer {

    private static final Logger log = Logger.getLogger(DependencyDeployer.class);

    private final List<Archive> archives = new ArrayList<Archive>();

    @Override
    public void deploy(final BeforeDeploy event, final Container container, final SphinxConfig config) throws DeploymentException {
        final Receiver receiver = config.isCdiConfigured()
                ? new Receiver(config.getCdi())
                : null;

        for (final ArchiveConfig dependency : config.getArchives()) {
            final String deployment = dependency.getPathToArchive();
            final File file = new File(deployment.trim());
            if (!file.exists()) {
                undeploy(container, config);
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
                    ArchiveUtil.replace(archive, replacement.getExisting(), new File(replacement.getReplacement()));
                } catch (IOException e) {
                    undeploy(container, config);
                    throw new DeploymentException("Failed replacing file " + replacement.getExisting() + " with " + replacement.getReplacement(), e);
                }
            }

            if (config.isCdiConfigured()) {
                CdiArchiveProcessor.process(archive, config.getCdi());
            }

            archives.add(archive);
            log.info("Deploying " + archive.getName());
            container.getDeployableContainer().deploy(archive);
        }

        if (config.isCdiConfigured()) {
            final Archive<?> archive = event.getDeployment().getArchive();
            final Set<String> bindings = CdiArchiveProcessor.getBeanManagerBindings();

            while (!bindings.isEmpty()) {
                final Message message;
                try {
                    message = receiver.receive();
                } catch (final Exception e) {
                    try {
                        receiver.close();
                    } catch (IOException _) {
                        //
                    }
                    undeploy(container, config);
                    throw new DeploymentException("Failed receiving deployment message", e);
                }
                CdiProducerProducer.createProducer(archive, message);
                bindings.remove(message.getBinding());
            }
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
