package io.machinecode.sphinx.config;

import io.machinecode.sphinx.util.PropertyUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static io.machinecode.sphinx.config.SphinxConfig.NAMESPACE;
import static javax.xml.bind.annotation.XmlAccessType.FIELD;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@XmlRootElement(namespace = NAMESPACE, name = "sphinx")
@XmlAccessorType(FIELD)
public class SphinxConfig {
    public static final String EXTENSION_NAME = "sphinx";
    public static final String CONFIG_FILE = "config-file";

    public static final String NAMESPACE = "http://machinecode.io/schema/sphinx:0.1.1";

    private static SphinxConfig instance = null;

    @XmlAttribute(name = "temp-dir", required = true)
    private String tempDir;

    @XmlElement(name = "archive", namespace = NAMESPACE, required = false)
    private List<ArchiveConfig> archives = new ArrayList<>();

    @XmlElement(name = "database", namespace = NAMESPACE, required = false)
    private List<DatabaseConfig> databases = new ArrayList<>();

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(final String tempDir) {
        this.tempDir = tempDir;
    }

    public List<ArchiveConfig> getArchives() {
        return archives;
    }

    public void setArchives(final List<ArchiveConfig> archives) {
        this.archives = archives;
    }

    public List<DatabaseConfig> getDatabases() {
        return databases;
    }

    public void setDatabases(final List<DatabaseConfig> databases) {
        this.databases = databases;
    }

    public void validate() {
        tempDir = PropertyUtil.resolve(tempDir);
        for (final ArchiveConfig archive : archives) {
            archive.validate();
        }
        for (final DatabaseConfig database : databases) {
            database.validate();
        }
    }

    public static synchronized SphinxConfig getConfig(final String filename) {
        try {
            return getConfig(new FileInputStream(filename));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Sphinx configuration not found at location " + filename);
        }
    }

    public static synchronized SphinxConfig getConfig(final InputStream stream) {
        if (instance != null) {
            return instance;
        }
        try {
            final JAXBContext context = JAXBContext.newInstance(SphinxConfig.class);
            instance = (SphinxConfig) context.createUnmarshaller().unmarshal(stream);
            instance.validate();
            return instance;
        } catch (final JAXBException e) {
            throw new IllegalStateException("Failed loading Sphinx config", e);
        }
    }

    public void write(final OutputStream stream) {
        try {
            final JAXBContext context = JAXBContext.newInstance(SphinxConfig.class);
            context.createMarshaller().marshal(this, stream);
        } catch (final JAXBException e) {
            throw new IllegalStateException("Failed writing Sphinx config", e);
        }

    }
}
