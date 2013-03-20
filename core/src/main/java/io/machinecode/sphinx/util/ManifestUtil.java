package io.machinecode.sphinx.util;

import org.jboss.shrinkwrap.api.asset.Asset;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class ManifestUtil {

    public static Manifest get(final List<String> dependencies) {
        return get(new Manifest(), dependencies);
    }

    public static Manifest get(final Manifest existing, final List<String> dependencies) {
        final Manifest manifest = new Manifest(existing);
        final Attributes attributes = manifest.getMainAttributes();
        if (attributes.getValue(Attributes.Name.MANIFEST_VERSION.toString()) == null) {
            attributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
        }
        final String value = attributes.getValue("Dependencies");
        final StringBuilder moduleDeps = new StringBuilder(value != null && value.trim().length() > 0
                ? value
                : "org.jboss.modules"
        );
        for (final String dep : dependencies) {
            if (dep != null && moduleDeps.indexOf(dep) < 0) {
                moduleDeps.append(",").append(dep);
            }
        }

        attributes.putValue("Dependencies", moduleDeps.toString());
        return manifest;
    }

    public static Asset getAsset(final Manifest manifest) {
        return new Asset() {
            public InputStream openStream() {
                try {
                    final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    manifest.write(stream);
                    return new ByteArrayInputStream(stream.toByteArray());
                } catch (IOException ex) {
                    throw new IllegalStateException("Cannot write manifest", ex);
                }
            }
        };
    }

}
