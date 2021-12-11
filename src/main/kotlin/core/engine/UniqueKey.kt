package core.engine

import core.UniqueKeySerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = UniqueKeySerializer::class)
abstract class UniqueKey
{
    protected abstract fun eq(key : UniqueKey) : Boolean

    protected abstract fun hash() : Int

    protected abstract fun toStr() : String

    override fun toString() : String{
        return toStr()
    }

    override fun equals(other: Any?): Boolean {
        if(other != null && other is UniqueKey) {
            return eq(other as UniqueKey)
        }

        return false
    }

    override fun hashCode(): Int {
        return hash()
    }
}