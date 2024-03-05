/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.properties.alias;

import org.opalj.br.fpcf.FPCFAnalysis;
import org.opalj.fpcf.properties.PropertyValidator;
import org.opalj.tac.fpcf.analyses.alias.IntraProceduralNoAliasAnalysis;
import org.opalj.tac.fpcf.analyses.alias.pointsto.AllocationSitePointsToBasedAliasAnalysis;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

@PropertyValidator(key = "AliasProperty", validator = MayAliasMatcher.class)
@Repeatable(MayAliases.class)
@Documented
@Target({TYPE_USE, PARAMETER, METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface MayAlias {

    /**
     * A short reasoning why this relation is a NoAlias relation.
     */
    String reason();

    /**
     * The id of this NoAlias relation.
     * It is used to associate this element with the other element that is part of this relation.
     * @return The id of this NoAlias relation.
     */
    int id();

    Class<?> clazz();

    /**
     * All analyses that should be able to correctly detect this relation.
     * @return All analyses that should be able to correctly detect this relation.
     */
    Class<? extends FPCFAnalysis>[] analyses() default {
            AllocationSitePointsToBasedAliasAnalysis.class,
            IntraProceduralNoAliasAnalysis.class
    };

    /**
     * Indicates whether this element is part of a NoAlias relation with null.
     * @return Whether this element is part of a NoAlias relation with null.
     */
    boolean aliasWithNull() default false;

}
