package com.airfranceklm.amt.yaml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the binding from the YAML key to the property setter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface YamlBinding {
    String value();
    String context() default "";
    Class collectionType() default Object.class;
}
