package io.machinecode.sphinx;

import io.machinecode.sphinx.config.ArchiveConfig;
import io.machinecode.sphinx.config.SphinxConfig;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.container.ManifestContainer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author Stuart Douglas
 */
public class ArchiveProcessor implements ApplicationArchiveProcessor {

    @Inject
    private Instance<SphinxConfig> instance;

    @Override
    public void process(final Archive<?> archive, final TestClass testClass) {
        final SphinxConfig config = instance.get();

        if (!(archive instanceof ManifestContainer<?>)) {
            throw new IllegalArgumentException("ManifestContainer expected " + archive);
        }
        final Manifest manifest;
        final Attributes attributes;
        try {
            final Node node = archive.get(JarFile.MANIFEST_NAME);
            if (node == null) {
                manifest = new Manifest();
                attributes = manifest.getMainAttributes();
                attributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
            } else {
                manifest = new Manifest(node.getAsset().openStream());
                attributes = manifest.getMainAttributes();
                if (attributes.getValue(Attributes.Name.MANIFEST_VERSION.toString()) == null) {
                    attributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
                }
            }
        } catch (final Exception e) {
            throw new IllegalStateException("Cannot obtain manifest", e);
        }

        final String value = attributes.getValue("Dependencies");
        final StringBuilder moduleDeps = new StringBuilder(value != null && value.trim().length() > 0
                ? value
                : "org.jboss.modules"
        );
        for (final ArchiveConfig dependency : config.getArchives()) {
            for (final String dep : dependency.getDependencies()) {
                if (dep != null && moduleDeps.indexOf(dep) < 0) {
                    moduleDeps.append(",").append(dep);
                }
            }
        }

        attributes.putValue("Dependencies", moduleDeps.toString());

        // Add the manifest to the archive
        final ArchivePath manifestPath = ArchivePaths.create(JarFile.MANIFEST_NAME);
        archive.delete(manifestPath);
        archive.add(new Asset() {
            public InputStream openStream() {
                try {
                    final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    manifest.write(stream);
                    return new ByteArrayInputStream(stream.toByteArray());
                } catch (IOException ex) {
                    throw new IllegalStateException("Cannot write manifest", ex);
                }
            }
        }, manifestPath);
    }
}
