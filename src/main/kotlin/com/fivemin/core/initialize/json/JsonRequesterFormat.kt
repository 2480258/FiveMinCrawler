package com.fivemin.core.initialize.json

import arrow.core.Some
import arrow.core.toOption
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.serialize.postParser.RequestFactory
import com.fivemin.core.initialize.RequesterFactory
import com.fivemin.core.parser.HtmlDocumentFactoryImpl
import com.fivemin.core.request.*
import com.fivemin.core.request.adapter.RequesterAdapterImpl
import com.fivemin.core.request.adapter.ResponseAdapterImpl
import com.fivemin.core.request.cookie.CookieControllerImpl
import com.fivemin.core.request.cookie.CookieResolveTargetFactory
import com.fivemin.core.request.cookie.CookieSyncGradiant
import com.fivemin.core.request.cookie.CustomCookieJar

@kotlinx.serialization.Serializable
data class JsonRequesterCompFormat(
    val engines: List<JsonRequesterEngineFormat>,
    val cookiePolicies: List<JsonRequesterCookieSyncFormat>
) {
    fun build(factories: Iterable<RequesterFactory>, io : DirectIO): RequesterSelector {
        val cookies = cookiePolicies.map {
            it.build(engines)
        }

        var targetFac = CookieControllerImpl(cookies)
        var eng = engines.map {
            Pair(it.requesterEngineName, it.build(factories, io, targetFac))
        }

        var dic = eng.associate {
            Pair(RequesterEngineInfo(it.first), it.second)
        }

        return RequesterSelectorImpl(dic)
    }
}

@kotlinx.serialization.Serializable
class JsonRequesterCookieSyncFormat(
    val syncDest: JsonRequesterIndex,
    val syncSrc: JsonRequesterIndex
) {
    fun build(engine: List<JsonRequesterEngineFormat>): CookieSyncGradiant {
        if (syncSrc.index == null) {
            throw IllegalArgumentException()
        }

        return if (syncDest.index == null) {
            CookieSyncGradiant(
                syncSrc.build(),
                (0 until engine.single {
                    it.requesterEngineName == syncDest.engine
                }.requesters.count()).map {
                    PerformedRequesterInfo(RequesterEngineInfo(syncDest.engine), RequesterSlotInfo(it))
                }
            )
        } else {
            CookieSyncGradiant(syncSrc.build(), listOf(syncDest.build()))
        }
    }
}

@kotlinx.serialization.Serializable
class JsonRequesterIndex(
    val engine: String,
    val index: Int?
) {
    fun build(): PerformedRequesterInfo {
        return PerformedRequesterInfo(RequesterEngineInfo(engine), RequesterSlotInfo(index!!))
    }
}

@kotlinx.serialization.Serializable
class JsonRequesterEngineFormat(
    val requesterEngineName: String,
    val type: String,
    val requesters: List<JsonRequesterFormat>
) {
    fun build(factories: Iterable<RequesterFactory>, io: DirectIO, factory: CookieResolveTargetFactory) : RequesterEngine<ResponseData>{
        var dic = (0 until requesters.count()).associate {
            var info = buildInfo(it)
            var req = requesters.elementAt(it)

            Pair(info.slot, buildReq(factories, req, info, io, factory))
        }

        return RequesterEngineImpl<ResponseData>(RequesterEngineConfig(), dic)
    }

    private fun buildReq(
        factories: Iterable<RequesterFactory>,
        fmt: JsonRequesterFormat,
        info: PerformedRequesterInfo,
        io: DirectIO,
        factory: CookieResolveTargetFactory
    ) : RequesterCore<ResponseData>{
        if(type == "Default"){
            return fmt.build(info, io, factory)
        }

        var ret = factories.single {
            it.key == type
        }.build(info, io, factory)

        return ret
    }

    private fun buildInfo(idx: Int): PerformedRequesterInfo {
        return PerformedRequesterInfo(RequesterEngineInfo(requesterEngineName), RequesterSlotInfo(idx))
    }
}

@kotlinx.serialization.Serializable
class JsonRequesterFormat(
    val userAgent: String,
    val key: String = "Default"
) {
    fun build(info: PerformedRequesterInfo, io: DirectIO, factory: CookieResolveTargetFactory): DefaultRequesterCore {

        return DefaultRequesterCore(
            RequesterExtraImpl(),
            info,
            HttpRequesterConfig(RequesterConfig(factory), RequestHeaderProfile(userAgent = Some(userAgent))),
            RequesterAdapterImpl(
                CustomCookieJar(),
                ResponseAdapterImpl(info, MemoryFilterFactoryImpl(io, HtmlDocumentFactoryImpl()))
            )
        )
    }
}