package com.ignite.HQLite.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Mansour on 12/05/2016.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Constraints {
    boolean nullable() default true;
    boolean unique() default false;
    boolean email() default false;
    int maxSize() default 256;
    int minSize() default 0;
}