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

package com.fivemin.core.request.srtf

import arrow.core.Either
import com.fivemin.core.engine.PageName

class SRTFPageBlock private constructor(private val name: Either<PageName, String>) {
    companion object {
        private var blocks: MutableMap<Either<PageName, String>, SRTFPageBlock> = mutableMapOf()

        fun create(name: Either<PageName, String>): SRTFPageBlock {
            if (!blocks.containsKey(name)) {
                blocks.put(name, SRTFPageBlock(name))
            }

            return blocks[name]!!
        }

        fun reset() {
            blocks = mutableMapOf()
        }
    }

    private val average: Average
    private val propagation: MutableMap<SRTFPageBlock, Counter>

    init {
        average = Average()
        propagation = mutableMapOf()
    }

    fun getEndpointTime(): Double {
        return average.value + propagation.map {
            it.key.average.value * it.value.count
        }.fold(0.0) { x, y ->
            x + y
        }
    }

    fun addTimeSample(time: Double) {
        average.addSample(time)
    }

    fun addSample(name: SRTFPageBlock) {
        if (!propagation.containsKey(name)) {
            propagation[name] = Counter()
        }

        propagation[name]!!.increase()
    }
}
