package core.initialize.json

import arrow.core.Some
import arrow.core.toOption
import core.engine.*
import core.engine.transaction.serialize.postParser.RequestFactory
import core.initialize.RequesterFactory
import core.parser.HtmlDocumentFactoryImpl
import core.request.*
import core.request.adapter.RequesterAdapterImpl
import core.request.adapter.ResponseAdapterImpl
import core.request.cookie.CookieControllerImpl
import core.request.cookie.CookieResolveTargetFactory
import core.request.cookie.CookieSyncGradiant
import core.request.cookie.CustomCookieJar

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

class JsonRequesterIndex(
    val engine: String,
    val index: Int?
) {
    fun build(): PerformedRequesterInfo {
        return PerformedRequesterInfo(RequesterEngineInfo(engine), RequesterSlotInfo(index!!))
    }
}


class JsonRequesterEngineFormat(
    val requesterEngineName: String,
    val type: String,
    val requesters: Iterable<JsonRequesterFormat>
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