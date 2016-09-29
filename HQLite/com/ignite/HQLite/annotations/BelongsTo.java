package com.ignite.HQLite.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Mansour on 12/05/2016.
 */

/**
 * Defines a "belongs to" relationship where the class specified by belongsTo assumes ownership of the relationship. This has the effect of controlling how saves and deletes cascade.
 * You must use the <code>mappedBy</code> element of the <code>BelongsTo</code> annotation to specify the relationship field or property of the owning side.
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BelongsTo {
    //Class targetEntity() default void.class;

    /** The field name that owns the relationship. */
    String mappedBy();
}
