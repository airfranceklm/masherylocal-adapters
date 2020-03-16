package com.airfranceklm.amt.sidecar.elements;

import java.util.function.Predicate;

/**
 * Functional interface for the function that converts a string expression into a {@link Predicate} that will be used
 * to test equivalence of a data element to the condition.
 * @param <T>
 *
 */
@FunctionalInterface
public interface ElementFilterCreator<T> {

    default Predicate<T> create(String elementName, String expression) throws IllegalFilterExpressionException {
        return create(new ElementWithParameterSpecImpl(elementName), expression);
    }

    /**
     * Create the predicate from the expression
     * @param expression expression for the predicate
     * @return instnace of the predicate
     * @throws IllegalFilterExpressionException if the expression is not valid for this algorithm.
     */
    Predicate<T> create(ElementSpec attachingTo, String expression) throws IllegalFilterExpressionException;
}
