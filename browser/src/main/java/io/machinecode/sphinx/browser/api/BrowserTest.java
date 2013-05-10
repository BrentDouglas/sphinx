package io.machinecode.sphinx.browser.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates this test needs a connection to browserstack to run.
 *
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BrowserTest {

    String url();

    Browser[] browsers() default {};
}
