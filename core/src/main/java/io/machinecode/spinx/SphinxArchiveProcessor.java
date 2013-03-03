package io.machinecode.spinx;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author Stuart Douglas
 */
public class SphinxArchiveProcessor implements ApplicationArchiveProcessor {

    public static final List<String> DEPENDENCIES;

    static {
        final List<String> deps = new ArrayList<String>();
        deps.addAll(Arrays.asList(System.getProperty("sphix.dependencies").split(",")));
        DEPENDENCIES = Collections.unmodifiableList(deps);
    }

    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        addModulesManifestDependencies(archive);
    }

    private void addModulesManifestDependencies(Archive<?> appArchive) {
        if (appArchive instanceof ManifestContainer<?> == false)
            throw new IllegalArgumentException("ManifestContainer expected " + appArchive);

        final Manifest manifest = getOrCreateManifest(appArchive);

        Attributes attributes = manifest.getMainAttributes();
        if (attributes.getValue(Attributes.Name.MANIFEST_VERSION.toString()) == null) {
            attributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
        }
        String value = attributes.getValue("Dependencies");
        StringBuffer moduleDeps = new StringBuffer(value != null && value.trim().length() > 0 ? value : "org.jboss.modules");
        for (String dep : DEPENDENCIES) {
            if (moduleDeps.indexOf(dep) < 0)
                moduleDeps.append("," + dep);
        }

        attributes.putValue("Dependencies", moduleDeps.toString());

        // Add the manifest to the archive
        ArchivePath manifestPath = ArchivePaths.create(JarFile.MANIFEST_NAME);
        appArchive.delete(manifestPath);
        appArchive.add(new Asset() {
            public InputStream openStream() {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    manifest.write(baos);
                    return new ByteArrayInputStream(baos.toByteArray());
                } catch (IOException ex) {
                    throw new IllegalStateException("Cannot write manifest", ex);
                }
            }
        }, manifestPath);
    }


    public static Manifest getOrCreateManifest(Archive<?> archive) {
        Manifest manifest;
        try {
            Node node = archive.get(JarFile.MANIFEST_NAME);
            if (node == null) {
                manifest = new Manifest();
                Attributes attributes = manifest.getMainAttributes();
                attributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
            } else {
                manifest = new Manifest(node.getAsset().openStream());
            }
            return manifest;
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot obtain manifest", ex);
        }
    }
}
