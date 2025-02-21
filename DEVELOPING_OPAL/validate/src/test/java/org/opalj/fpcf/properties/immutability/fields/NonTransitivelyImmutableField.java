/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.properties.immutability.fields;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.opalj.br.fpcf.FPCFAnalysis;
import org.opalj.fpcf.properties.PropertyValidator;
import org.opalj.tac.fpcf.analyses.L0FieldImmutabilityAnalysis;

/**
 * Annotation to state that the annotated field is non-transitively immutable.
 */
@PropertyValidator(key="FieldImmutability",validator= NonTransitiveImmutableFieldMatcher.class)
@Documented
@Retention(RetentionPolicy.CLASS)
public @interface NonTransitivelyImmutableField {

    /**
     * A short reasoning of this property.
     */
    String value();

    Class<? extends FPCFAnalysis>[] analyses() default { L0FieldImmutabilityAnalysis.class };
}
