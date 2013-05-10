package io.machinecode.sphinx.browser.response;

import java.util.List;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class Failure extends Base {

    private String field;
    private List<Error> errors;

    public String getField() {
        return field;
    }

    public void setField(final String field) {
        this.field = field;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(final List<Error> errors) {
        this.errors = errors;
    }
}
