/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.properties.alias.u_var;

import org.opalj.fpcf.properties.PropertyValidator;
import org.opalj.fpcf.properties.alias.MayAliasMatcher;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
