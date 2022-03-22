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

package com.fivemin.core.engine.session.bFilter

import arrow.core.Either
import com.fivemin.core.engine.FileIOToken
import com.fivemin.core.engine.SerializableAMQ
import com.fivemin.core.engine.UniqueKey
import com.fivemin.core.engine.session.BloomFilterFactory
import com.fivemin.core.engine.session.SerializedBloomFilterFactory
import com.google.common.hash.Funnels
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import java.io.DataOutput
import java.io.InputStream
import java.io.OutputStream


class BloomFilterImpl : SerializableAMQ {
    constructor(input: InputStream) {
        filter = com.google.common.hash.BloomFilter.readFrom(input, Funnels.stringFunnel(Charsets.UTF_8))
    }
    
    constructor(expectedInsertion: Int = 100000000, falsePositives: Double = 0.000001) {
        filter = com.google.common.hash.BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), expectedInsertion, falsePositives) //It will take 342MB
    }
    
    val filter : com.google.common.hash.BloomFilter<String>
    //Since URL is almost english, UTF8 will perform better than UTF16(or 32)
    
    override fun mightContains(element: UniqueKey) : Boolean {
        return filter.mightContain(element.toString())
    }
    
    override fun put(element: UniqueKey): Boolean {
        return filter.put(element.toString())
    }
    
    override fun exportTo(output: OutputStream) {
        return filter.writeTo(output)
    }
}

class BloomFilterFactoryImpl : BloomFilterFactory {
    override fun createEmpty(): SerializableAMQ {
        return BloomFilterImpl()
    }
}

class SerializedBloomFilterFactoryImpl : SerializedBloomFilterFactory {
    override fun createWithInput(inputStream: InputStream): SerializableAMQ {
        return BloomFilterImpl(inputStream)
    }
    
}