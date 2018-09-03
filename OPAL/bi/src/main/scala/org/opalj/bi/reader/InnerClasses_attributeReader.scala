/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj
package bi
package reader

import java.io.DataInputStream

import org.opalj.control.fillRefArray
import org.opalj.collection.immutable.RefArray

/**
 * Generic parser for the ''inner classes'' attribute.
 */
trait InnerClasses_attributeReader extends AttributeReader {

    //
    // TYPE DEFINITIONS AND FACTORY METHODS
    //

    type InnerClassesEntry <: AnyRef
    type InnerClasses = RefArray[InnerClassesEntry]

    type InnerClasses_attribute >: Null <: Attribute

    def InnerClasses_attribute(
        constant_pool:        Constant_Pool,
        attribute_name_index: Constant_Pool_Index,
        inner_classes:        InnerClasses,
        // The scope in which the attribute is defined
        as_name_index:       Constant_Pool_Index,
        as_descriptor_index: Constant_Pool_Index
    ): InnerClasses_attribute

    def InnerClassesEntry(
        constant_pool:            Constant_Pool,
        inner_class_info_index:   Constant_Pool_Index,
        outer_class_info_index:   Constant_Pool_Index,
        inner_name_index:         Constant_Pool_Index,
        inner_class_access_flags: Int
    ): InnerClassesEntry

    //
    // IMPLEMENTATION
    //

    /**
     * <pre>
     * InnerClasses_attribute {
     * u2 attribute_name_index;
     * u4 attribute_length;
     * u2 number_of_classes; // => Seq[InnerClasses_attribute.Class]
     *  {   u2 inner_class_info_index;
     *      u2 outer_class_info_index;
     *      u2 inner_name_index;
     *      u2 inner_class_access_flags;
     *  } classes[number_of_classes];
     * }
     * </pre>
     */
    private[this] def parserFactory() = (
        ap: AttributeParent,
        as_name_index: Constant_Pool_Index,
        as_descriptor_index: Constant_Pool_Index,
        cp: Constant_Pool,
        attribute_name_index: Constant_Pool_Index,
        in: DataInputStream
    ) ⇒ {
        /*val attribute_length =*/ in.readInt()
        val number_of_classes = in.readUnsignedShort
        if (number_of_classes > 0 || reifyEmptyAttributes) {
            InnerClasses_attribute(
                cp,
                attribute_name_index,
                fillRefArray(number_of_classes) {
                    InnerClassesEntry(
                        cp,
                        in.readUnsignedShort, in.readUnsignedShort,
                        in.readUnsignedShort, in.readUnsignedShort
                    )
                },
                as_name_index,
                as_descriptor_index
            )
        } else {
            null
        }
    }

    registerAttributeReader(InnerClassesAttribute.Name → parserFactory())
}
