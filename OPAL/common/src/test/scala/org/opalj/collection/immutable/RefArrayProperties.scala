/* BSD 2-Clause License - seeOPAL/LICENSE for details. */
package org.opalj.collection
package immutable

import org.junit.runner.RunWith

import org.scalatest.junit.JUnitRunner
import org.scalatest.Matchers
import org.scalatest.FunSpec

/**
 * Tests `RefArray`.
 *
 * @author Michael Eichberg
 */
@RunWith(classOf[JUnitRunner])
class RefArrayTest extends FunSpec with Matchers {

    describe("RefArrays") {

        it("filterNonNull should only return the first values if the null value is at the end") {
            assert(
                RefArray.from[String](Array[AnyRef]("a", "b", null)).filterNonNull ==
                    RefArray("a", "b")
            )
        }

        it("filterNonNull should only return the tail values if the null value is at the beginning") {
            assert(
                RefArray.from[String](Array[AnyRef](null, "a", "b")).filterNonNull ==
                    RefArray("a", "b")
            )
        }

        it("filterNonNull should only return the values at the beginning and end if we have null values in the middle") {
            assert(
                RefArray.from[String](Array[AnyRef]("a", null, null, "b")).filterNonNull ==
                    RefArray("a", "b")
            )
        }

        it("filterNonNull should only return the non-null values if the null values are everywhere") {
            assert(
                RefArray.from[String](Array[AnyRef](null, "a", null, null, "b", null, null)).filterNonNull ==
                    RefArray("a", "b")
            )
        }
    }
}
