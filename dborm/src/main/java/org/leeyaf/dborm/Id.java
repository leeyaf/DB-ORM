package org.leeyaf.dborm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * You should annotate module`s id field use this annotation.
 * It distinguish PK field to others.
 * 
 * @author leeyaf
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @ interface Id {

}
