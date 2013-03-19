package io.machinecode.sphinx.cdi;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import java.util.Set;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public abstract class CdiProducer {

    protected <T> T get(final String className) {
        try {
            final BeanManager beanManager = InitialContext.doLookup(getBinding());
            final Class<?> clazz = CdiProducer.class.getClassLoader().loadClass(className);
            final Set<Bean<?>> beans = beanManager.getBeans(clazz);
            final Bean<?> bean = beanManager.resolve(beans);
            final CreationalContext<?> ctx = beanManager.createCreationalContext(bean);
            return (T) beanManager.getReference(bean, clazz, ctx);
        } catch (Exception e) {
            return null;
        }
    }

    protected abstract String getBinding();
}
