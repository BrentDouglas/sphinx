package io.machinecode.sphinx.cdi.deployment;

import java.io.Serializable;
import java.util.Set;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class Message implements Serializable {

    private String binding;
    private Set<String> classes;

    public String getBinding() {
        return binding;
    }

    public Message setBinding(final String binding) {
        this.binding = binding;
        return this;
    }

    public Set<String> getClasses() {
        return classes;
    }

    public Message setClasses(final Set<String> classes) {
        this.classes = classes;
        return this;
    }
}
