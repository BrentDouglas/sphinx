package io.machinecode.spinx;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeStop;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Stuart Douglas
 */
public class SphinxDeployer {

    private Set<Archive> archives = new HashSet<Archive>();
    private boolean deployed = false;

    public synchronized void doServiceDeploy(@Observes BeforeDeploy event, Container container) throws DeploymentException {
        if(deployed) {
            return;
        }
        deployed = true;

        String[] deployments = System.getProperty("sphinx.deployments").split(",");
        for(String deployment : deployments) {
            File file = new File(deployment);
            if(!file.exists()) {
                throw new RuntimeException("File " + file + " does not exist");
            }
            Archive<?> archive;
            if(file.getName().endsWith(".ear")) {
                archive = ShrinkWrap.createFromZipFile(EnterpriseArchive.class, file);
            } else if(file.getName().endsWith(".war")) {
                archive = ShrinkWrap.createFromZipFile(WebArchive.class, file);
            } else {
                archive = ShrinkWrap.createFromZipFile(JavaArchive.class, file);
            }

            container.getDeployableContainer().deploy(archive);
            archives.add(archive);
        }
    }

    public synchronized void undeploy(@Observes BeforeStop event, Container container) throws DeploymentException {

        for(Archive archive : archives) {
            DeployableContainer<?> deployableContainer = container.getDeployableContainer();
            deployableContainer.undeploy(archive);
        }
    }
}
