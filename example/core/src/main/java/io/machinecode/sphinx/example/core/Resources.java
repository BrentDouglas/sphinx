package io.machinecode.sphinx.example.core;

import org.jboss.logging.Logger;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public final class Resources {

    @Produces
    @PersistenceContext(unitName = "ExamplePU")
    private EntityManager entityManager;

    @Produces
    public final Logger produceLog(final InjectionPoint injectionPoint) {
        return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
    }
}
