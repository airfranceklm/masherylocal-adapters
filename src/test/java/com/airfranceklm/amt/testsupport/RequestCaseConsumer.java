package com.airfranceklm.amt.testsupport;

@FunctionalInterface
public interface RequestCaseConsumer<T extends RequestCase> {

    void accept(T reqCase);

}
