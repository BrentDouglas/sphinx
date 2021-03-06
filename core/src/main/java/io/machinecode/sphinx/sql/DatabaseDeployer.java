package io.machinecode.sphinx.sql;

import io.machinecode.sphinx.Deployer;
import io.machinecode.sphinx.config.DatabaseConfig;
import io.machinecode.sphinx.config.SphinxConfig;
import io.machinecode.sphinx.util.ManifestUtil;
import io.machinecode.sphinx.util.PropertyUtil;
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
import java.io.Reader;
import java.sql.Driver;
import java.util.Arrays;
import java.util.HashMap;
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
                final String pre = databaseConfig.getPreDeployment();
                if (pre != null) {
                    try {
                        final Reader reader = new FileReader(new File(pre));
                        DatabaseImporter.runScript(databaseConfig, reader, new DriverProducer() {
                            @Override
                            public Driver produce() throws Exception {
                                return DelegateDriver.from(databaseConfig.getDriver());
                            }
                        });
                    } catch (Exception e) {
                        final DeploymentException exception = new DeploymentException("Failed running pre deployment sql task " + pre, e);
                        try {
                            runPostScripts(config);
                        } catch (final DeploymentException de) {
                            exception.addSuppressed(de);
                        }
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
                    .addClass(PropertyUtil.class)
                    .addClass(ConfigurationException.class)
                    .addAsManifestResource(
                            ManifestUtil.getAsset(ManifestUtil.get(Arrays.asList("javaee.api"))),
                            ArchivePaths.create(JarFile.MANIFEST_NAME)
                    );

            for (final DatabaseConfig databaseConfig : config.getDatabases()) {
                final String pre = databaseConfig.getPreDeployment();
                final String post = databaseConfig.getPostDeployment();
                final String id = databaseConfig.getId();
                if (pre != null) {
                    archive.addAsResource(
                        new File(pre),
                        "/" + id + "/" + PRE_DEPLOY_SQL
                    );
                }
                if (post != null) {
                    archive.addAsResource(
                        new File(post),
                        "/" + id + "/" + POST_DEPLOY_SQL
                    );
                }
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
        final Map<String, Exception> failures = new HashMap<>();
        for (final DatabaseConfig databaseConfig : config.getDatabases()) {
            final String post = databaseConfig.getPostDeployment();
            if (post != null) {
                try {
                    final Reader reader = new FileReader(new File(post));
                    DatabaseImporter.runScript(databaseConfig, reader, new DriverProducer() {
                        @Override
                        public Driver produce() throws Exception {
                            return DelegateDriver.from(databaseConfig.getDriver());
                        }
                    });
                } catch (final Exception e) {
                    failures.put(post, e);
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
