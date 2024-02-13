package org.opalj.fpcf.properties.alias;

import org.opalj.fpcf.properties.PropertyValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@PropertyValidator(key = "AliasProperty", validator = NoAliasMatcher.class)
@Documented
@Target({TYPE_USE, PARAMETER, METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface NoAliases {

    NoAlias[] value();

}
