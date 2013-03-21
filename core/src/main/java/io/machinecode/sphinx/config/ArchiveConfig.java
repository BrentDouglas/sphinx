package io.machinecode.sphinx.config;

import io.machinecode.sphinx.util.PathUtil;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

import static io.machinecode.sphinx.config.SphinxConfig.*;
import static javax.xml.bind.annotation.XmlAccessType.FIELD;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@XmlAccessorType(FIELD)
public class ArchiveConfig {

    @XmlElement(name = "path-to-archive", namespace = NAMESPACE, required = true)
    private String pathToArchive;

    @XmlElement(name = "manifest-entry", namespace = NAMESPACE, required = false)
    private List<String> dependencies = new ArrayList<String>();

    @XmlElement(name = "replace-file", namespace = NAMESPACE, required = false)
    private List<ReplacementConfig> replacements = new ArrayList<ReplacementConfig>();

    public void validate() {
        pathToArchive = PathUtil.resolve(pathToArchive);
        for (final ReplacementConfig replacement : replacements) {
            replacement.validate();
        }
    }

    public String getPathToArchive() {
        return pathToArchive;
    }

    public void setPathToArchive(final String pathToArchive) {
        this.pathToArchive = pathToArchive;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(final List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public List<ReplacementConfig> getReplacements() {
        return replacements;
    }

    public void setReplacements(final List<ReplacementConfig> replacements) {
        this.replacements = replacements;
    }
}
