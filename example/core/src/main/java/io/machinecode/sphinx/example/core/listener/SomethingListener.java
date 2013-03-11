package io.machinecode.sphinx.example.core.listener;

import javax.persistence.PostLoad;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class SomethingListener {

    @PostLoad
    public void postLoad(final Object object) {

    }
}
