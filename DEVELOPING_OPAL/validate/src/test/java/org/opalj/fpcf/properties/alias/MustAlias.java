/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.properties.alias;

import org.opalj.br.fpcf.FPCFAnalysis;
import org.opalj.fpcf.properties.PropertyValidator;
import org.opalj.tac.fpcf.analyses.alias.IntraProceduralNoAliasAnalysis;
import org.opalj.tac.fpcf.analyses.alias.pointsto.AllocationSitePointsToBasedAliasAnalysis;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 * Annotation to specify that this element is part of the MustAlias relation with the given ID.
 *
 * @see Alias
 */
@PropertyValidator(key = "AliasProperty", validator = MustAliasMatcher.class)
@Repeatable(MustAliases.class)
@Documented
@Target({TYPE_USE, PARAMETER, METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface MustAlias {

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

    int methodID() default -1;

    /**
     * Indicates whether this element is part of a NoAlias relation with null.
     * @return Whether this element is part of a NoAlias relation with null.
     */
    boolean aliasWithNull() default false;
}
