/* BSD 2-Clause License:
 * Copyright (c) 2009 - 2017
 * Software Technology Group
 * Department of Computer Science
 * Technische Universität Darmstadt
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.opalj
package ai

import org.opalj.collection.immutable.UIDSet
import org.opalj.br.{BaseType, ReferenceType}
import org.opalj.br.{BooleanType, ByteType, CharType, ShortType, IntegerType, LongType}
import org.opalj.br.{FloatType, DoubleType}

/**
 * Encapsulates the available type information about a `DomainValue`.
 *
 * @author Michael Eichberg
 */
sealed trait TypeInformation { // TODO Rename to ValueInformation

    /**
     * Returns `true` if no type information is available.
     */
    def isUnknownValue: Boolean

    /** True if the value has a reference type; undefined if the type is unknown. */
    def isReferenceValue: Boolean

    /** True in case of a value with primitive type; undefined if the type is unknown. */
    def isPrimitiveValue: Boolean

}

/**
 * Specifies that no type information is available.
 *
 * @note Recall that the computational type of a value always has to
 *      be available, but that a
 *      `ValuesDomain.typeOfValue(...)` query does not need to take the computational type
 *      into account. (Whenever the core framework requires the computational type of a
 *      value it uses the respective method.) However, in case that the
 *      underlying value may be an array or exception
 *      value the reported type must not be `TypeUnknown`.
 *
 * @author Michael Eichberg
 */
case object UnknownType extends TypeInformation { // TODO Rename to UnknownValue

    override def isUnknownValue: Boolean = true

    override def isPrimitiveValue: Boolean = throw DomainException("the type is unknown")

    override def isReferenceValue: Boolean = throw DomainException("the type is unknown")

}

trait KnownType extends TypeInformation { // TODO Rename to KnownValue

    final override def isUnknownValue: Boolean = false

}

/**
 * The value has the primitive type.
 */
sealed trait IsPrimitiveValue[T <: BaseType, DomainPrimitiveValue <: AnyRef] extends KnownType {
    this: DomainPrimitiveValue ⇒

    final def isReferenceValue: Boolean = false

    final def isPrimitiveValue: Boolean = true

    def primitiveType: T

    def valueType: Option[T] = Some(primitiveType)

}

object IsPrimitiveValue {

    def unapply[T <: BaseType, V <: AnyRef](answer: IsPrimitiveValue[T, V]): Option[T] = {
        Some(answer.primitiveType)
    }

}

trait IsBooleanValue[DomainBooleanValue <: AnyRef]
        extends IsPrimitiveValue[BooleanType, DomainBooleanValue] {
    this: DomainBooleanValue ⇒

    final def primitiveType: BooleanType = BooleanType
}

trait IsByteValue[DomainByteValue <: AnyRef]
        extends IsPrimitiveValue[ByteType, DomainByteValue] {
    this: DomainByteValue ⇒

    final def primitiveType: ByteType = ByteType
}

trait IsCharValue[DomainCharValue <: AnyRef]
        extends IsPrimitiveValue[CharType, DomainCharValue] {
    this: DomainCharValue ⇒

    final def primitiveType: CharType = CharType
}

trait IsShortValue[DomainShortValue <: AnyRef]
        extends IsPrimitiveValue[ShortType, DomainShortValue] {
    this: DomainShortValue ⇒

    final def primitiveType: ShortType = ShortType
}

trait IsIntegerValue[DomainIntegerValue <: AnyRef]
        extends IsPrimitiveValue[IntegerType, DomainIntegerValue] {
    this: DomainIntegerValue ⇒

    final def primitiveType: IntegerType = IntegerType
}

trait IsFloatValue[DomainFloatValue <: AnyRef]
        extends IsPrimitiveValue[FloatType, DomainFloatValue] {
    this: DomainFloatValue ⇒

    final def primitiveType: FloatType = FloatType
}

trait IsLongValue[DomainLongValue <: AnyRef]
        extends IsPrimitiveValue[LongType, DomainLongValue] {
    this: DomainLongValue ⇒

    final def primitiveType: LongType = LongType
}

trait IsDoubleValue[DomainDoubleValue <: AnyRef]
        extends IsPrimitiveValue[DoubleType, DomainDoubleValue] {
    this: DomainDoubleValue ⇒

    final def primitiveType: DoubleType = DoubleType
}

/**
 * Characterizes a reference value. Captures the information about the values
 * a domain value may refer to. For example, in the following:
 * {{{
 * val o = If(...) new Object() else "STRING"
 * }}}
 * o is a reference value (`IsReferenceValue`) that (may) refers to two "simple" base values:
 * `new Object()` and `"STRING"`; however, it is a decision of the the underlying domain whether
 * the information about the base values is made available or not. Furthermore, if the base values
 * are actually used, the constraints in effect for the overall abstraction should be considered
 * to get the most precise result.
 *
 * @author Michael Eichberg
 */
trait IsReferenceValue[DomainReferenceValue <: AnyRef] extends KnownType {
    this: DomainReferenceValue ⇒

    final override def isReferenceValue: Boolean = true

    final override def isPrimitiveValue: Boolean = false

    /**
     * The upper bound of the value's type. The upper bound is empty if this
     * value is `null` (i.e., `isNull == Yes`). The upper bound is only guaranteed to contain
     * exactly one type if the type is precise. (i.e., `isPrecise == true`). Otherwise,
     * the upper type bound may contain one or more types that are not known to be
     * in an inheritance relation, but which will ''correctly'' approximate the runtime
     * type.
     *
     * @note If only a part of a project is analyzed, the class hierarchy may be
     *      fragmented and it may happen that two classes that are indeed in an
     *      inheritance relation – if we would analyze the complete project – are part
     *      of the upper type bound.
     */
    def upperTypeBound: UIDSet[_ <: ReferenceType]

    /**
     * If `Yes` the value is known to always be `null` at runtime. In this
     * case the upper bound  is (has to be) empty. If the answer is `Unknown` then the
     * analysis was not able to statically determine whether the value is `null` or
     * is not `null`. In this case the upper bound is expected to be non-empty.
     * If the answer is `No` then the value is statically known not to be `null`. In this
     * case, the upper bound may precisely identify the runtime type or still just identify
     * an upper bound.
     *
     * This default implementation always returns `Unknown`; this is a sound
     * over-approximation.
     *
     * @note '''This method is expected to be overridden by subtypes.'''
     *
     * @return `Unknown`.
     */
    def isNull: Answer = Unknown

    /**
     * Returns `true` if the type information is precise. I.e., the type returned by
     * `upperTypeBound` precisely models the runtime type of the value.
     *  If, `isPrecise` returns true, the type of this value can
     * generally be assumed to represent a class type (not an interface type) or
     * an array type. However, this domain also supports the case that `isPrecise`
     * returns `true` even though the associated type identifies an interface type
     * or an abstract class type. The later case may be interesting in the context
     * of classes that are generated at run time.
     *
     * This default implementation always returns `false`.
     *
     * @note `isPrecise` is always `true` if this value is known to be `null`.
     *
     * @note '''This method is expected to be overridden by subtypes.'''
     *
     * @return `false`
     */
    def isPrecise: Boolean = false

    /**
     * Tests if the type of this value is potentially a subtype of the specified
     * reference type under the assumption that this value is not `null`.
     * This test takes the precision of the type information into account.
     * That is, if the currently available type information is not precise and
     * the given type has a subtype that is always a subtype of the current
     * upper type bound, then `Unknown` is returned. Given that it may be
     * computationally intensive to determine whether two types have a common subtype
     * it may be better to just return `Unknown` in case that this type and the
     * given type are not in a direct inheritance relationship.
     *
     *
     * Basically, this method implements the same semantics as the `ClassHierarchy`'s
     * `isSubtypeOf` method, but it additionally checks if the type of this value
     * ''could be a subtype'' of the given supertype. I.e., if this value's type
     * identifies a supertype of the given `supertype` and that type is not known
     * to be precise, the answer is `Unknown`.
     *
     * For example, assume that the type of this reference value is
     * `java.util.Collection` and we know/have to assume that this is only an
     * upper bound. In this case an answer is `No` if and only if it is impossible
     * that the runtime type is a subtype of the given supertype. This
     * condition holds, for example, for `java.io.File` which is not a subclass
     * of `java.util.Collection` and which does not have any further subclasses (in
     * the JDK). I.e., the classes `java.io.File` and `java.util.Collection` are
     * not in an inheritance relationship. However, if the specified supertype would
     * be `java.util.List` the answer would be unknown.
     *
     * @note The function `isValueSubtypeOf` is not defined if `isNull` returns `Yes`;
     *      if `isNull` is `Unknown` then the result is given under the
     *      assumption that the value is not `null` at runtime.
     *      In other words, if this value represents `null` this method is not supported.
     * @note This method is expected to be overridden by subtypes.
     *
     * @return This default implementation always returns `Unknown`.
     */
    def isValueSubtypeOf(referenceType: ReferenceType): Answer = Unknown

    /**
     * In general an `IsReferenceValue` abstracts over all potential values and this information is
     * sufficient for subsequent analyses; but in some cases, analyzing the set of underlying values
     * may increase the overall precision and this set is returned by this function. In other
     * words: if `baseValues` is nonEmpty, then the properties returned by `this` value are derived
     * from the base values, but still maybe more specific. For example,
     * {{{
     *     Object o = _;
     *     if(...) o = f() else o = g();
     *     // when we reach this point, we generally don't know if the values returned by f and g
     *     // are non-null; hence, o is potentially null.
     *     if(o != null)
     *      // Now, we know that o is not null, but we still don't know if the values returned
     *      // by f OR g were null and we cannot establish that when we don't know to which value
     *      // o is actually referring to.
     *      u(o);
     * }}}
     *
     * @note A reference value which belongs to the base values by some other reference value
     *       '''never''' has itself as a '''direct''' base value.
     *
     * @return The set of values this reference value abstracts over. The set is empty if this
     *         value is already a base value.
     */
    def baseValues: Traversable[DomainReferenceValue]

    /**
     * The set of base values this values abstracts over. This set is never empty and contains
     * this value if this value does not (further) abstract over other reference values.
     *
     * @note Primarily defined as a convenience interface.
     */
    final def allValues: Traversable[DomainReferenceValue] = {
        val baseValues = this.baseValues
        if (baseValues.isEmpty) Traversable(this) else baseValues
    }

}

/**
 * Extractor for reference values.
 *
 * @author Michael Eichberg
 */
object TypeOfReferenceValue {

    def unapply[V <: AnyRef](rv: IsReferenceValue[V]): Some[UIDSet[_ <: ReferenceType]] = {
        Some(rv.upperTypeBound)
    }
}

/**
 * Defines an extractor method for instances of `IsReferenceValue` objects.
 *
 * @note To ensure that the generic type can be matched, it may be necessary to first cast
 *       a ''generic'' [[org.opalj.ai.ValuesDomain.DomainValue]] to a
 *       [[org.opalj.ai.ValuesDomain.DomainReferenceValue]].
 *       {{{
 *           val d : Domain = ...
 *           val d.DomainReferenceValue(v) = /*some domain value; e.g., operands.head*/
 *           val BaseReferenceValues(values) = v
 *           values...
 *       }}}
 *
 * @author Michael Eichberg
 */
object BaseReferenceValues {

    def unapply[V <: IsReferenceValue[V]](rv: IsReferenceValue[V]): Some[Traversable[V]] = {
        Some(rv.allValues)
    }
}

/**
 * Defines and extractor for the null-property of reference values.
 *
 * @author Michael Eichberg
 */
object IsNullValue {

    def unapply[V <: AnyRef](rv: IsReferenceValue[V]): Boolean = rv.isNull == Yes

}
