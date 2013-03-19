package io.machinecode.sphinx.cdi.deployment;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CdiExtension implements Extension {

    private static final Set<String> CLASSES = new HashSet<String>();

    private static volatile BeanManager beanManager = null;
    private static volatile boolean done = false;

    public void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery beforeBeanDiscovery, final BeanManager beanManager) throws Exception {
        CdiExtension.beanManager = beanManager;
    }

    public void processAnnotatedType(@Observes final ProcessAnnotatedType processAnnotatedType) {
        final Class clazz = processAnnotatedType.getAnnotatedType().getJavaClass();
        CLASSES.add(clazz.getCanonicalName());
    }

    public void afterDeploymentValidation(@Observes final AfterDeploymentValidation afterDeploymentValidation) {
        done = true;
    }

    public void shutdown(@Observes BeforeShutdown event) {
        beanManager = null;
        CLASSES.clear();
    }

    public BeanManager getBeanManager() {
        return beanManager;
    }

    public Set<String> getClasses() {
        return done
                ? Collections.unmodifiableSet(CLASSES)
                : null;
    }
}
