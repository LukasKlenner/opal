package org.opalj.fpcf.properties.alias.u_var;

import org.opalj.br.fpcf.FPCFAnalysis;
import org.opalj.fpcf.properties.PropertyValidator;
import org.opalj.fpcf.properties.alias.Alias;
import org.opalj.fpcf.properties.alias.MayAliasMatcher;
import org.opalj.tac.fpcf.analyses.alias.IntraProceduralNoAliasAnalysis;
import org.opalj.tac.fpcf.analyses.alias.pointsto.AllocationSitePointsToBasedAliasAnalysis;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 * Annotation to specify that this element is part of the MayAlias relation with the given ID.
 *
 * @see Alias
 */
@PropertyValidator(key = "AliasProperty", validator = MayAliasMatcher.class)
@Documented
@Target({TYPE_USE, PARAMETER, METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface MayAliasUVars {

    MayAliasUVar[] value();

}
