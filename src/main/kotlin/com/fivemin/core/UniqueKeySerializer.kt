package com.fivemin.core

import com.fivemin.core.engine.UniqueKey
import com.fivemin.core.engine.transaction.StringUniqueKey
import com.fivemin.core.engine.transaction.UriUniqueKey
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.builtins.LongAsStringSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.net.URI

object UniqueKeySerializer : KSerializer<UniqueKey> {
    private val STRING_UNIQUE = "String"
    private val URI_UNIQUE = "Uri"
    
    @OptIn(ExperimentalSerializationApi::class)
    private val delegateSerializer = ArraySerializer(String.Companion.serializer())
    
    override fun deserialize(decoder: Decoder): UniqueKey {
        var type = delegateSerializer.deserialize(decoder)
        
        return if (type.first() == STRING_UNIQUE) {
            StringUniqueKey(type[1])
        } else if (type.first() == URI_UNIQUE) {
            UriUniqueKey(URI(type[1]))
        } else {
            throw IllegalArgumentException("unexpected uniquekey type while deserializing")
        }
    }
    
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = SerialDescriptor("UniqueKey", delegateSerializer.descriptor)
    
    override fun serialize(encoder: Encoder, value: UniqueKey) {
        var type = if (value is StringUniqueKey) {
            arrayOf(STRING_UNIQUE, value.src)
        } else if (value is UriUniqueKey) {
            arrayOf(URI_UNIQUE, value.uri.toString())
        } else {
            throw IllegalArgumentException("unexpected uniquekey type while serializing")
        }
        
        return delegateSerializer.serialize(encoder, type)
    }
}