package io.machinecode.sphinx.sql;

import io.machinecode.sphinx.Deployer;
import io.machinecode.sphinx.config.DatabaseConfig;
import io.machinecode.sphinx.config.SphinxConfig;
import io.machinecode.sphinx.util.ManifestUtil;
import io.machinecode.sphinx.util.PathUtil;
import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
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
import java.io.Reader;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarFile;

import static io.machinecode.sphinx.sql.DatabaseImporter.DRIVER_JAR;
import static io.machinecode.sphinx.sql.DatabaseImporter.POST_DEPLOY_SQL;
import static io.machinecode.sphinx.sql.DatabaseImporter.PRE_DEPLOY_SQL;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class DatabaseDeployer implements Deployer {

    private static final Logger log = Logger.getLogger(DatabaseDeployer.class);

    private JavaArchive archive;

    private final List<String> tasksRun = new ArrayList<String>();

    @Override
    public void deploy(final BeforeDeploy event, final Container container, final SphinxConfig config) throws DeploymentException {
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
                    tasksRun.add(file); //We'll want to try and clean this one up even if it fails half way through
                    final Reader reader = new FileReader(new File(file));
                    DatabaseImporter.runScript(databaseConfig, reader, new DriverProducer() {
                        @Override
                        public Driver produce() throws Exception {
                            return DelegateDriver.from(databaseConfig.getDriver());
                        }
                    });
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
                    .addClass(DriverProducer.class)
                    .addClass(DelegateDriver.class)
                    .addClass(PathUtil.class)
                    .addClass(ConfigurationException.class)
                    .addAsManifestResource(
                            ManifestUtil.getAsset(ManifestUtil.get(Arrays.asList("javaee.api"))),
                            ArchivePaths.create(JarFile.MANIFEST_NAME)
                    );

            for (final DatabaseConfig databaseConfig : config.getDatabases()) {
                final String id = databaseConfig.getId();
                archive.addAsResource(
                        new File(databaseConfig.getPreDeployment()),
                        "/" + id + "/" + PRE_DEPLOY_SQL
                    ).addAsResource(
                            new File(databaseConfig.getPostDeployment()),
                            "/" + id + "/" + POST_DEPLOY_SQL
                    );
                if (databaseConfig.getDriver() != null) {
                    archive.addAsResource(
                            new File(databaseConfig.getDriver().getPathToDriverJar()),
                            "/" + id + "/" + DRIVER_JAR
                    );
                }
            }

            log.info("Deploying " + archive.getName());
            container.getDeployableContainer().deploy(archive);
        }
    }

    @Override
    public void undeploy(final Container container, final SphinxConfig config) throws DeploymentException {
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
                    final Reader reader = new FileReader(new File(file));
                    DatabaseImporter.runScript(databaseConfig, reader, new DriverProducer() {
                        @Override
                        public Driver produce() throws Exception {
                            return DelegateDriver.from(databaseConfig.getDriver());
                        }
                    });
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
