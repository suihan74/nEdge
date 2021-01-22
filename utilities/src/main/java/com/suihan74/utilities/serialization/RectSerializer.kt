package com.suihan74.utilities.serialization

import android.graphics.Rect
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*

class RectSerializer : KSerializer<Rect> {
    private enum class RectElement {
        LEFT,
        TOP,
        RIGHT,
        BOTTOM
    }

    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor(RectSerializer::class.qualifiedName!!) {
            element<Int>(RectElement.LEFT.name)
            element<Int>(RectElement.TOP.name)
            element<Int>(RectElement.RIGHT.name)
            element<Int>(RectElement.BOTTOM.name)
        }

    override fun serialize(encoder: Encoder, value: Rect) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, RectElement.LEFT.ordinal, value.left)
            encodeIntElement(descriptor, RectElement.TOP.ordinal, value.top)
            encodeIntElement(descriptor, RectElement.RIGHT.ordinal, value.right)
            encodeIntElement(descriptor, RectElement.BOTTOM.ordinal, value.bottom)
        }
    }

    override fun deserialize(decoder: Decoder): Rect {
        return decoder.decodeStructure(descriptor) {
            var left = 0
            var top = 0
            var right = 0
            var bottom = 0
            while (true) {
                when (val idx = decodeElementIndex(descriptor)) {
                    RectElement.LEFT.ordinal -> left = decodeIntElement(descriptor, idx)
                    RectElement.TOP.ordinal -> top = decodeIntElement(descriptor, idx)
                    RectElement.RIGHT.ordinal -> right = decodeIntElement(descriptor, idx)
                    RectElement.BOTTOM.ordinal -> bottom = decodeIntElement(descriptor, idx)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $idx")
                }
            }
            Rect(left, top, right, bottom)
        }
    }
}
