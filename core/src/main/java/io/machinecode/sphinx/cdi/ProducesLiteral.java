package io.machinecode.sphinx.cdi;

import javax.enterprise.inject.Produces;
import javax.enterprise.util.AnnotationLiteral;

/**
 * Annotation literal for {@link javax.enterprise.inject.Produces}
 *
 * @author Brent Douglas
 */
class ProducesLiteral extends AnnotationLiteral<Produces> implements Produces {

    public static final Produces INSTANCE = new ProducesLiteral();

    ProducesLiteral() {}
}
