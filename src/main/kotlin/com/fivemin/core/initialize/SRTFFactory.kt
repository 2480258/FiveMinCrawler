package com.fivemin.core.initialize

import com.fivemin.core.request.srtf.SRTFExportSubPolicy
import com.fivemin.core.request.srtf.SRTFFinalizeSubPolicy
import com.fivemin.core.request.srtf.SRTFPrepareSubPolicy
import com.fivemin.core.request.srtf.SRTFScheduler

class SRTFOption(val scheduler : SRTFScheduler, val prepare : SRTFPrepareSubPolicy, val finalize : SRTFFinalizeSubPolicy, val export : SRTFExportSubPolicy){
    val policies : SubPolicyCollection

    init {
        policies = SubPolicyCollection(listOf(prepare), listOf(finalize), listOf(), listOf(export))
    }
}

class SRTFFactory {
    fun create() : SRTFOption {
        var sc = SRTFScheduler()

        return SRTFOption(sc, SRTFPrepareSubPolicy(sc), SRTFFinalizeSubPolicy(sc), SRTFExportSubPolicy(sc))
    }
}