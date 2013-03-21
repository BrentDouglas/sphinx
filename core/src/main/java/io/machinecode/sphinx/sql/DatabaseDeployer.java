package io.machinecode.sphinx.sql;

import io.machinecode.sphinx.Deployer;
import io.machinecode.sphinx.config.DatabaseConfig;
import io.machinecode.sphinx.config.SphinxConfig;
import io.machinecode.sphinx.util.ManifestUtil;
import io.machinecode.sphinx.util.PathUtil;
import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarFile;

/**
 * TODO: This currently only provides the filesystem ref of the
 * schema file to deployer and will not work on remote containers.
 *
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class DatabaseDeployer implements Deployer {

    private static final Logger log = Logger.getLogger(DatabaseDeployer.class);

    private JavaArchive archive;

    private final List<String> tasksRun = new ArrayList<String>();

        @Override
    public void deploy(final Container container, final SphinxConfig config) throws DeploymentException {
        if (config.getDatabases() == null) {
            return;
        }

        boolean deployArchive = false;
        for (final DatabaseConfig databaseConfig : config.getDatabases() ) {
            if (databaseConfig.isRunInContainer()) {
                deployArchive = true;
            } else {
                final String file = databaseConfig.getPreDeployment();
                try {
                    tasksRun.add(file); //Well want to try and clean this one up even if it fails
                    DatabaseImporter.runScript(databaseConfig, new FileReader(new File(file)));
                } catch (Exception e) {
                    final DeploymentException exception = new DeploymentException("Failed running pre deployment sql task " + file, e);
                    try {
                        runPostScripts(config);
                    } catch (final DeploymentException de) {
                        exception.addSuppressed(de);
                    } finally {
                        throw exception;
                    }
                }
            }
        }

        if (deployArchive) {
            this.archive = ShrinkWrap.create(JavaArchive.class, "sphinx-database-import.jar")
                    .addAsResource(new Asset() {
                        @Override
                        public InputStream openStream() {
                            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            config.write(stream);
                            return new ByteArrayInputStream(stream.toByteArray());
                        }
                    }, DatabaseImporter.SPHINX_XML)
                    .addAsResource(getClass().getClassLoader().getResource("ejb-jar.xml"), "META-INF/ejb-jar.xml")
                    .addPackage(SphinxConfig.class.getPackage())
                    .addClass(DatabaseImporter.class)
                    .addClass(DelegateDriver.class)
                    .addClass(PathUtil.class)
                    .addClass(ConfigurationException.class)
                    .addAsManifestResource(
                            ManifestUtil.getAsset(ManifestUtil.get(Arrays.asList("javaee.api"))),
                            ArchivePaths.create(JarFile.MANIFEST_NAME)
                    );

            for (final DatabaseConfig databaseConfig : config.getDatabases()) {
                archive.addAsResource(new File(databaseConfig.getPreDeployment()), databaseConfig.getId() + File.separator + databaseConfig.getPreDeployment())
                    .addAsResource(new File(databaseConfig.getPostDeployment()), databaseConfig.getId() + File.separator + databaseConfig.getPostDeployment());
            }

            log.info("Deploying " + archive.getName());
            container.getDeployableContainer().deploy(archive);
        }
    }

    @Override
    public void undeploy(final Container container, final SphinxConfig config) throws DeploymentException {
        DeploymentException failure = null;
        try {
            runPostScripts(config);
        } finally {
            if (archive != null) {
                log.info("Undeploying " + archive.getName());
                container.getDeployableContainer().undeploy(this.archive);
            }
        }
    }

    private void runPostScripts(final SphinxConfig config) throws DeploymentException {
        final Map<String, Exception> failures = new HashMap<String, Exception>();
        for (final DatabaseConfig databaseConfig : config.getDatabases()) {
            final String file = databaseConfig.getPostDeployment();
            if (tasksRun.contains(databaseConfig.getPreDeployment())) {
                try {
                    DatabaseImporter.runScript(databaseConfig, new FileReader(new File(file)));
                } catch (Exception e) {
                    failures.put(file, e);
                }
            }
        }
        if (!failures.isEmpty()) {
            final DeploymentException exception = new DeploymentException("Failed running post deployment sql tasks");
            for (final Entry<String, Exception> entry : failures.entrySet()) {
                exception.addSuppressed(new DeploymentException("Failed running post deployment sql task " + entry.getKey(), entry.getValue()));
            }
            throw exception;
        }
    }
}
