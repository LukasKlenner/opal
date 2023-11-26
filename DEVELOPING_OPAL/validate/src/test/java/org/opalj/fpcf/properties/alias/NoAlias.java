package org.opalj.fpcf.properties.alias;

import org.opalj.fpcf.properties.PropertyValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;

@PropertyValidator(key = "AliasProperty", validator = NoAliasMatcher.class)
@Target({ TYPE_USE, PARAMETER })
@Documented
@Retention(RetentionPolicy.CLASS)
public @interface NoAlias {

    /**
     * A short reasoning of this property.
     */
    String value();

    String other();

    String thiz();
}
