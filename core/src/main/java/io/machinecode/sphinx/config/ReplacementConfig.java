package io.machinecode.sphinx.config;

import io.machinecode.sphinx.util.PropertyUtil;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import static io.machinecode.sphinx.config.SphinxConfig.NAMESPACE;
import static javax.xml.bind.annotation.XmlAccessType.FIELD;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@XmlAccessorType(FIELD)
public class ReplacementConfig {

    @XmlAttribute(name = "substitute-properties")
    private boolean substituteProperties = false;

    @XmlElement(name = "existing", namespace = NAMESPACE, required = true)
    private String existing;

    @XmlElement(name = "replacement", namespace = NAMESPACE, required = true)
    private String replacement;

    public void validate() {
        existing = PropertyUtil.resolve(existing);
        replacement = PropertyUtil.resolve(replacement);
    }

    public boolean isSubstituteProperties() {
        return substituteProperties;
    }

    public void setSubstituteProperties(final boolean substituteProperties) {
        this.substituteProperties = substituteProperties;
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
