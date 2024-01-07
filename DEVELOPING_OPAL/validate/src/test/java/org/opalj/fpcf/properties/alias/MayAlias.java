package org.opalj.fpcf.properties.alias;

import org.opalj.br.fpcf.FPCFAnalysis;
import org.opalj.fpcf.properties.PropertyValidator;
import org.opalj.tac.fpcf.analyses.alias.IntraProceduralNoAliasAnalysis;
import org.opalj.tac.fpcf.analyses.alias.pointsto.AbstractPointsToBasedAliasAnalysis;

import java.lang.annotation.*;

@PropertyValidator(key = "AliasProperty", validator = MayAliasMatcher.class)
@Documented
@Target({})
@Retention(RetentionPolicy.CLASS)
public @interface MayAlias {

    /**
     * A short reasoning of this property.
     */
    String reason();

    Class<?> testClass();

    String id();

    Class<? extends FPCFAnalysis>[] analyses() default { IntraProceduralNoAliasAnalysis.class,
            AbstractPointsToBasedAliasAnalysis.class };

    boolean aliasWithNull() default false;
}
