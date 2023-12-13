package org.opalj.fpcf.properties.alias;

import org.opalj.br.fpcf.FPCFAnalysis;
import org.opalj.fpcf.properties.PropertyValidator;
import org.opalj.tac.fpcf.analyses.alias.IntraProceduralNoAliasAnalysis;
import org.opalj.tac.fpcf.analyses.alias.PointsToBasedAliasAnalysis;

import java.lang.annotation.*;

@PropertyValidator(key = "AliasProperty", validator = NoAliasMatcher.class)
@Documented
@Target({})
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

    boolean aliasWithNull() default false;
}
