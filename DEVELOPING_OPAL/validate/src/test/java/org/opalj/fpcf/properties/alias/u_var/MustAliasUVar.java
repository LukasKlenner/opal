/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.properties.alias.u_var;

import org.opalj.br.fpcf.FPCFAnalysis;
import org.opalj.fpcf.properties.PropertyValidator;
import org.opalj.fpcf.properties.alias.MayAliasMatcher;
import org.opalj.fpcf.properties.alias.MustAliasMatcher;
import org.opalj.tac.fpcf.analyses.alias.IntraProceduralNoAliasAnalysis;
import org.opalj.tac.fpcf.analyses.alias.pointsto.AllocationSitePointsToBasedAliasAnalysis;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

@PropertyValidator(key = "AliasProperty", validator = MustAliasMatcher.class)
@Repeatable(MustAliasUVars.class)
@Documented
@Target({TYPE_USE, PARAMETER, METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface MustAliasUVar {

    /**
     * A short reasoning why this relation is a NoAlias relation.
     */
    String reason();

    int lineNumber();

    int secondLineNumber() default -1;

    int parameterIndex() default -1;

    int secondParameterIndex() default -1;

    int methodID();

    int secondMethodID() default -1;

    boolean aliasWithNull() default false;

    Class<?> clazz();

    /**
     * All analyses that should be able to correctly detect this relation.
     * @return All analyses that should be able to correctly detect this relation.
     */
    Class<? extends FPCFAnalysis>[] analyses() default {
            AllocationSitePointsToBasedAliasAnalysis.class,
            IntraProceduralNoAliasAnalysis.class
    };
}
