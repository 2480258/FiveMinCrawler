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

package com.fivemin.core.initialize.json

import arrow.core.Some
import arrow.core.identity
import arrow.core.toOption
import com.fivemin.core.engine.*
import com.fivemin.core.initialize.RequesterFactory
import com.fivemin.core.parser.HtmlDocumentFactoryImpl
import com.fivemin.core.request.*
import com.fivemin.core.request.adapter.RequesterAdapterImpl
import com.fivemin.core.request.adapter.ResponseAdapterImpl
import com.fivemin.core.request.cookie.CookieControllerImpl
import com.fivemin.core.request.cookie.CookieResolveTargetFactory
import com.fivemin.core.request.cookie.CookieSyncGradiant
import com.fivemin.core.request.cookie.CustomCookieJar
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

@kotlinx.serialization.Serializable
data class JsonRequesterCompFormat(
    val engines: List<JsonRequesterEngineFormat>,
    val cookiePolicies: List<JsonRequesterCookieSyncFormat>
) {
    fun build(factories: Iterable<RequesterFactory>, io: DirectIO): RequesterSelector {
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
                (
                        0 until engine.single {
                            it.requesterEngineName == syncDest.engine
                        }.requesters.count()
                        ).map {
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
    fun build(
        factories: Iterable<RequesterFactory>,
        io: DirectIO,
        factory: CookieResolveTargetFactory
    ): RequesterEngine<ResponseData> {
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
    ): RequesterCore<ResponseData> {
        if (type == "Default") {
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
    val key: String = "Default",
    val cookiePathFromExeLoc: String? = null
) {
    fun build(info: PerformedRequesterInfo, io: DirectIO, factory: CookieResolveTargetFactory): DefaultRequesterCore {
        
        val cookies = cookiePathFromExeLoc.toOption().map {
            val file = File(it).readText()
            
            val jsonReader = Json { ignoreUnknownKeys = true }
            
            jsonReader.decodeFromString<JsonCookieImportFormat>(file).build()
        }.fold({ listOf() }, ::identity)
        
        return DefaultRequesterCore(
            RequesterExtraImpl(),
            info,
            RequesterConfig(factory),
            RequesterAdapterImpl(
                CustomCookieJar(cookies),
                ResponseAdapterImpl(info, MemoryFilterFactoryImpl(io, HtmlDocumentFactoryImpl())),
                RequestHeaderProfile(userAgent = Some(userAgent))
            )
        )
    }
}
