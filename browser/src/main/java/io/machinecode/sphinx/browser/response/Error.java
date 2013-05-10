package io.machinecode.sphinx.browser.response;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class Error extends Base {
    private String field;
    private String code;

    public String getField() {
        return field;
    }

    public void setField(final String field) {
        this.field = field;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }
}
