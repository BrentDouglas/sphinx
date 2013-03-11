package io.machinecode.sphinx.config;

import io.machinecode.sphinx.util.PathUtil;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@XmlAccessorType(FIELD)
public class ReplacementConfig {

    @XmlElement(name = "existing", namespace = SphinxConfig.NAMESPACE, required = true)
    private String existing;

    @XmlElement(name = "replacement", namespace = SphinxConfig.NAMESPACE, required = true)
    private String replacement;

    public void validate() {
        replacement = PathUtil.resolve(replacement);
    }

    public String getExisting() {
        return existing;
    }

    public void setExisting(final String existing) {
        this.existing = existing;
    }

    public String getReplacement() {
        return replacement;
    }

    public void setReplacement(final String replacement) {
        this.replacement = replacement;
    }
}
