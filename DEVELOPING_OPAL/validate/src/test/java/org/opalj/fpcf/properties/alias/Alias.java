package org.opalj.fpcf.properties.alias;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;

@Target({ TYPE_USE, PARAMETER })
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface Alias {

    MayAlias[] mayAlias() default {};

    NoAlias[] noAlias() default {};

    MustAlias[] mustAlias() default {};
}
