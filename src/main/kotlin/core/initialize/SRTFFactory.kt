package core.initialize

import core.request.srtf.SRTFExportSubPolicy
import core.request.srtf.SRTFFinalizeSubPolicy
import core.request.srtf.SRTFPrepareSubPolicy
import core.request.srtf.SRTFScheduler

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