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

import com.fivemin.core.engine.UniqueKey
import com.fivemin.core.engine.session.BloomFilter
import com.fivemin.core.engine.session.BloomFilterFactory
import com.fivemin.core.engine.session.SerializedBloomFilter
import com.google.common.hash.Funnels
import java.io.InputStream
import java.io.OutputStream


class BloomFilterImpl : BloomFilter {
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
    
    override fun exportTo(): OutputStream {
        return filter.writeTo()
    }
}

class BloomFilterFactoryImpl : BloomFilterFactory {
    override fun createEmpty(): BloomFilter {
        return BloomFilterImpl()
    }
}

class SerializedBloomFilterImpl(private val input: InputStream) : SerializedBloomFilter {
    override fun create(): BloomFilter {
        return BloomFilterImpl(input)
    }
}