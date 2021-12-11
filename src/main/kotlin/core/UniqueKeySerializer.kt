package core

import core.engine.UniqueKey
import core.engine.transaction.StringUniqueKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object UniqueKeySerializer : KSerializer<UniqueKey> {
    override fun deserialize(decoder: Decoder): UniqueKey {
        return StringUniqueKey(decoder.decodeString())
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("UniqueKey", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UniqueKey){
        return encoder.encodeString(value.toString())
    }

}