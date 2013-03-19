package io.machinecode.sphinx.cdi;

import io.machinecode.sphinx.cdi.deployment.CdiDeployment;
import io.machinecode.sphinx.cdi.deployment.CdiExtension;
import io.machinecode.sphinx.cdi.deployment.Message;
import io.machinecode.sphinx.config.CdiConfig;
import io.machinecode.sphinx.util.ArchiveUtil;
import io.machinecode.sphinx.util.ManifestUtil;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarFile;

import static io.machinecode.sphinx.cdi.deployment.CdiDeployment.RESOURCE;
import static io.machinecode.sphinx.cdi.deployment.CdiDeployment.SOCKET;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class CdiArchiveProcessor {

    private static final Logger log = Logger.getLogger(CdiArchiveProcessor.class);

    private static final String BINDING = "java:/SphinxBeanManager";
    private static final String META_INF_SERVICES_EXTENSION = "META-INF/services/javax.enterprise.inject.spi.Extension";

    private static final String META_INF = "META-INF/";
    private static final String WEB_INF = "WEB-INF/";

    private static final String EJB_JAR_XML = "ejb-jar.xml";
    private static final String META_INF_EJB_JAR = META_INF + EJB_JAR_XML;
    private static final String WEB_INF_EJB_JAR = WEB_INF + EJB_JAR_XML;

    private static final String BEANS_XML = "beans.xml";
    private static final String META_INF_BEANS_XML = META_INF + BEANS_XML;
    private static final String WEB_INF_BEANS_XML = WEB_INF + BEANS_XML;

    public static final Set<String> BEAN_MANAGER_BINDINGS = Collections.synchronizedSet(new HashSet<String>());

    private static volatile int id = 0;

    public static void process(final Archive<?> archive, final CdiConfig config) {
        final String address = config.getBindAddress() + ":" + config.getPort();
        try {
            if (archive instanceof JavaArchive) {
                if (hasBeansXml(archive)) {
                    addToArchive(JavaArchive.class.cast(archive), address);
                }
            } else if (archive instanceof WebArchive) {
                if (hasBeansXml(archive)) {
                    addToArchive(WebArchive.class.cast(archive), address);
                }
            } else if (archive instanceof EnterpriseArchive) {
                final Map<ArchivePath, Node> jars = archive.getContent(Filters.include("/.*\\.jar"));
                for (final Entry<ArchivePath, Node> entry : jars.entrySet()) {
                    final ArchivePath path = entry.getKey();
                    final JavaArchive javaArchive = ArchiveUtil.getReplacementArchive(JavaArchive.class, entry.getValue());
                    if (hasBeansXml(javaArchive)) {
                        archive.delete(path);
                        addToArchive(javaArchive, address);
                        archive.merge(javaArchive, path);
                    }
                }
                final Map<ArchivePath, Node> wars = archive.getContent(Filters.include("/.*\\.war"));
                for (final Entry<ArchivePath, Node> entry : wars.entrySet()) {
                    final ArchivePath path = entry.getKey();
                    final WebArchive webArchive = ArchiveUtil.getReplacementArchive(WebArchive.class, entry.getValue());
                    if (hasBeansXml(webArchive)) {
                        archive.delete(path);
                        addToArchive(webArchive, address);
                        archive.merge(webArchive, path);
                    }
                }
            }
        } catch (final Exception e) {
            log.error("Failed installing CdiExtension", e);
        }
    }

    private static void addToArchive(final JavaArchive archive, final String address) throws IOException {
        try {
            archive.addClass(CdiExtension.class);
            archive.addClass(CdiDeployment.class);
            archive.addClass(Message.class);

            archive.addAsResource(getAsset(), RESOURCE);
            archive.add(new StringAsset(address), SOCKET);
            final Node node = archive.get(META_INF_SERVICES_EXTENSION);

            final StringBuilder builder = new StringBuilder();
            readAsset(node, builder);
            archive.delete(META_INF_SERVICES_EXTENSION);
            archive.add(new StringAsset(builder.toString()), META_INF_SERVICES_EXTENSION);

            if (archive.get(META_INF_EJB_JAR) == null) {
                archive.addAsResource(CdiArchiveProcessor.class.getClassLoader().getResource(EJB_JAR_XML), META_INF_EJB_JAR);
            }
            archive.addAsManifestResource(
                    ManifestUtil.getAsset(ManifestUtil.get(Arrays.asList("javaee.api"))),
                    ArchivePaths.create(JarFile.MANIFEST_NAME)
            );
            log.info("Added CdiExtension to " + archive.getName());
        } catch (final IOException e) {
            log.error("Failed adding CdiExtension from archive", e);
        }
    }

    private static void addToArchive(final WebArchive archive, final String address) throws IOException {
        try {
            archive.addClass(CdiExtension.class);
            archive.addClass(CdiDeployment.class);
            archive.addClass(Message.class);

            archive.addAsResource(getAsset(), RESOURCE);
            archive.add(new StringAsset(address), SOCKET);
            final Node node = archive.get(META_INF_SERVICES_EXTENSION);

            final StringBuilder builder = new StringBuilder();
            readAsset(node, builder);
            archive.delete(META_INF_SERVICES_EXTENSION);
            archive.add(new StringAsset(builder.toString()), META_INF_SERVICES_EXTENSION);
            if (archive.get(WEB_INF_EJB_JAR) == null) {
                archive.addAsResource(CdiArchiveProcessor.class.getClassLoader().getResource(EJB_JAR_XML), WEB_INF_EJB_JAR);
            }
            archive.addAsManifestResource(
                    ManifestUtil.getAsset(ManifestUtil.get(Arrays.asList("javaee.api"))), //They will already have one for cdi
                    ArchivePaths.create(JarFile.MANIFEST_NAME)
            );
            log.info("Added CdiExtension to " + archive.getName());
        } catch (final IOException e) {
            log.error("Failed adding CdiExtension from archive", e);
        }
    }

    private static void readAsset(final Node node, final StringBuilder builder) throws IOException {
        builder.append(CdiExtension.class.getCanonicalName()).append('\n');
        if (node != null) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(node.getAsset().openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        }
    }

    private static StringAsset getAsset() {
        final String binding = BINDING + ++id;
        BEAN_MANAGER_BINDINGS.add(binding);
        return new StringAsset(binding);
    }

    public static Set<String> getBeanManagerBindings() {
        return new HashSet<String>(BEAN_MANAGER_BINDINGS);
    }

    private static boolean hasBeansXml(final Archive<?> archive) {
        return !archive.getContent(Filters.include(".*/beans\\.xml")).isEmpty();
    }

    public static void ensureBeansXml(final Archive<?> archive) {
        if (archive instanceof JavaArchive && archive.get(META_INF_BEANS_XML) == null) {
            JavaArchive.class.cast(archive).addAsResource(CdiArchiveProcessor.class.getClassLoader().getResource(BEANS_XML), META_INF_BEANS_XML);
        } else if (archive instanceof WebArchive && archive.get(WEB_INF_BEANS_XML) == null) {
            JavaArchive.class.cast(archive).addAsResource(CdiArchiveProcessor.class.getClassLoader().getResource(BEANS_XML), WEB_INF_BEANS_XML);
        }
    }
}
