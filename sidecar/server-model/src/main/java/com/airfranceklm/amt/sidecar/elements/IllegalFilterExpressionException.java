package com.airfranceklm.amt.sidecar.elements;

public class IllegalFilterExpressionException extends DataElementException {
    private String expression;

    public IllegalFilterExpressionException(ElementSpec elemName, String message, Throwable cause, String expression) {
        this(elemName != null ? elemName.getElementName() : "<null>", message, cause, expression);
    }

    public IllegalFilterExpressionException(String elemName, String message, Throwable cause, String expression) {
        super(elemName, message, cause);
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }
}
