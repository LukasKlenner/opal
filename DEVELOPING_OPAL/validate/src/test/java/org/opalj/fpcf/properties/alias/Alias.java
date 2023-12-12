package org.opalj.fpcf.properties.alias;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

@Target({ TYPE_USE, PARAMETER, METHOD })
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface Alias {

    MayAlias[] mayAlias() default {};

    NoAlias[] noAlias() default {};

    MustAlias[] mustAlias() default {};
}
