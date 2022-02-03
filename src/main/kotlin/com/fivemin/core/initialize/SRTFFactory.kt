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

import com.fivemin.core.request.srtf.SRTFExportSubPolicy
import com.fivemin.core.request.srtf.SRTFFinalizeSubPolicy
import com.fivemin.core.request.srtf.SRTFPrepareSubPolicy
import com.fivemin.core.request.srtf.SRTFScheduler

class SRTFOption(val scheduler: SRTFScheduler, val prepare: SRTFPrepareSubPolicy, val finalize: SRTFFinalizeSubPolicy, val export: SRTFExportSubPolicy) {
    val policies: SubPolicyCollection

    init {
        policies = SubPolicyCollection(listOf(prepare), listOf(finalize), listOf(), listOf(export))
    }
}

class SRTFFactory {
    fun create(): SRTFOption {
        var sc = SRTFScheduler()

        return SRTFOption(sc, SRTFPrepareSubPolicy(sc), SRTFFinalizeSubPolicy(sc), SRTFExportSubPolicy(sc))
    }
}
