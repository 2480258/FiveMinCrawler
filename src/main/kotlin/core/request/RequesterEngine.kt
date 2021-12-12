package core.request

import core.engine.RequesterSlotInfo
import core.engine.ResponseData

interface RequesterEngine<out Resp : ResponseData> {
    val info: RequesterEngineConfig
    val count: Int

    fun get(info: RequesterSlotInfo): RequesterCore<Resp>
}

class RequesterEngineImpl<out Resp : ResponseData>(
    override val info: RequesterEngineConfig,
    private val requesterDic: Map<RequesterSlotInfo, RequesterCore<Resp>>
) :
    RequesterEngine<Resp> {

    override val count : Int
    get() {
        return requesterDic.size
    }

    override fun get(info: RequesterSlotInfo): RequesterCore<Resp> {
        return requesterDic[info]!!
    }
}

class RequesterEngineConfig {

}