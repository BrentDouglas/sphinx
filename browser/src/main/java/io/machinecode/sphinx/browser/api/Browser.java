package io.machinecode.sphinx.browser.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A browser that this test will be run in.
 *
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Browser {
    String device();
    String osVersion();
    String os();
    String browserVersion();
    String browser();
}
