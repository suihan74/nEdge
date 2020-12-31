package com.suihan74.utilities.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.threeten.bp.LocalTime

class LocalTimeSerializer : KSerializer<LocalTime> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(LocalTimeSerializer::class.qualifiedName!!, PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: LocalTime) {
        encoder.encodeLong(value.toSecondOfDay().toLong())
    }

    override fun deserialize(decoder: Decoder): LocalTime {
        return LocalTime.ofSecondOfDay(decoder.decodeLong())
    }
}
