package org.opalj.fpcf.properties.alias.u_var;

import org.opalj.br.fpcf.FPCFAnalysis;
import org.opalj.fpcf.properties.PropertyValidator;
import org.opalj.fpcf.properties.alias.MayAliasMatcher;
import org.opalj.tac.fpcf.analyses.alias.IntraProceduralNoAliasAnalysis;
import org.opalj.tac.fpcf.analyses.alias.pointsto.AllocationSitePointsToBasedAliasAnalysis;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

@PropertyValidator(key = "AliasProperty", validator = MayAliasMatcher.class)
@Repeatable(MayAliasUVars.class)
@Documented
@Target({TYPE_USE, PARAMETER, METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface MayAliasUVar {

    /**
     * A short reasoning why this relation is a NoAlias relation.
     */
    String reason() default "No reason Provided";

    /**
     * All analyses that should be able to correctly detect this relation.
     * @return All analyses that should be able to correctly detect this relation.
     */
    Class<? extends FPCFAnalysis>[] analyses() default {
            AllocationSitePointsToBasedAliasAnalysis.class,
            IntraProceduralNoAliasAnalysis.class
    };

    int lineNumber();
}
