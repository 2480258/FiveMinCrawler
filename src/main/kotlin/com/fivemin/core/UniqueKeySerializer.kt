/*
 *
 *     FiveMinCrawler
 *     Copyright (C) 2022  2480258
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.fivemin.core

import com.fivemin.core.engine.UniqueKey
import com.fivemin.core.engine.transaction.StringUniqueKey
import com.fivemin.core.engine.transaction.UriUniqueKey
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.builtins.serializer
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
