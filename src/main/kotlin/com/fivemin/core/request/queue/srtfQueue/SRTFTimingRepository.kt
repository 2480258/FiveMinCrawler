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

package com.fivemin.core.request.queue.srtfQueue

import arrow.core.Either
import arrow.core.toOption
import com.fivemin.core.engine.PageName
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

interface SRTFTimingRepository {
    fun getTiming(descriptor: SRTFPageDescriptor): Duration
    
    fun reportTiming(descriptor: SRTFPageDescriptor, duration: Duration)
}

class SRTFTimingRepositoryImpl : SRTFTimingRepository {
    private val map = ConcurrentHashMap<SRTFPageDescriptor, Average>()
    
    /**
     * Returns average timing of given SRTFPageDescriptor. If descriptor not found, returns zero.
     */
    @OptIn(ExperimentalTime::class)
    override fun getTiming(descriptor: SRTFPageDescriptor): Duration {
        return map[descriptor].toOption().fold({
            Duration.ZERO
        }, {
            it.value.milliseconds
        })
    }
    
    @OptIn(ExperimentalTime::class)
    override fun reportTiming(descriptor: SRTFPageDescriptor, duration: Duration) {
        map.getOrPut(descriptor, { Average() }).addSample(duration.toDouble(DurationUnit.MILLISECONDS))
    }
}

class SRTFPageDescriptor {
    private val pageName: String?
    private val extension: String?
    
    constructor(page: PageName) {
        pageName = page.toString()
        extension = null
    }
    
    constructor(ext: String) {
        pageName = null
        extension = ext
    }
    
    override fun equals(other: Any?): Boolean {
        return if (other != null && other is SRTFPageDescriptor) {
            pageName == other.pageName && extension == other.extension
        } else {
            false
        }
    }
    
    override fun hashCode(): Int {
        val pn = pageName.orEmpty().hashCode()
        val ext = extension.orEmpty().hashCode()
        
        return (pn * 13) xor ext
    }
}


class Average {
    var value: Double = 0.0
    var count: Int = 0
    
    fun addSample(sample: Double) {
        value = (value * count + sample) / (count + 1)
        count++
    }
}
