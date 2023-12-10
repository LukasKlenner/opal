package org.opalj.fpcf.properties.alias;

import org.opalj.br.fpcf.FPCFAnalysis;
import org.opalj.fpcf.properties.PropertyValidator;
import org.opalj.tac.fpcf.analyses.alias.IntraProceduralNoAliasAnalysis;
import org.opalj.tac.fpcf.analyses.alias.PointsToBasedAliasAnalysis;

import java.lang.annotation.*;

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
    String reason() default "No reason Provided";

    Class<?> testClass();

    String id();

    Class<? extends FPCFAnalysis>[] analyses() default { IntraProceduralNoAliasAnalysis.class,
            PointsToBasedAliasAnalysis.class };

    // see DirectCall Annotation[] a();
}
