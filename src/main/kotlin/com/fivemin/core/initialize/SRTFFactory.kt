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

package com.fivemin.core.initialize

import com.fivemin.core.request.queue.srtfQueue.*

class SRTFOption(val deq: SRTFOptimizationPolicy, val keyEx: SRTFKeyExtractor, val descriptFac: SRTFPageDescriptorFactory, val timingRepo: SRTFTimingRepository) {
    val policies: SubPolicyCollection
    
    init {
        policies = SubPolicyCollection(listOf(), listOf(SRTFLogSubPolicy(timingRepo, descriptFac, deq)), listOf(), listOf())
    }
}

class SRTFFactory {
    fun create(): SRTFOption {
        val timing = SRTFTimingRepositoryImpl()
        val opt = SRTFOptimizationPolicyImpl(timing)
        val keyEx = opt
        val descript = SRTFPageDescriptorFactoryImpl()
        
        return SRTFOption(opt, keyEx, descript, timing)
    }
}
