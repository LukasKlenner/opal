package org.opalj.fpcf.properties.alias;

import org.opalj.br.fpcf.FPCFAnalysis;
import org.opalj.fpcf.properties.PropertyValidator;
import org.opalj.tac.fpcf.analyses.alias.IntraProceduralNoAliasAnalysis;
import org.opalj.tac.fpcf.analyses.alias.PointsToBasedAliasAnalysis;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;

@PropertyValidator(key = "AliasProperty", validator = MustAliasMatcher.class)
@Target({ TYPE_USE, PARAMETER })
@Documented
@Retention(RetentionPolicy.CLASS)
public @interface MustAlias {

    /**
     * A short reasoning of this property.
     */
    String reason();

    Class<?> testClass();

    String id();

    Class<? extends FPCFAnalysis>[] analyses() default { IntraProceduralNoAliasAnalysis.class,
            PointsToBasedAliasAnalysis.class };
}
