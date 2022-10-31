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

package com.fivemin.core.request

import arrow.core.Some
import arrow.core.none
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsRequestDoc
import com.fivemin.core.engine.*
import com.fivemin.core.parser.HtmlDocumentFactoryImpl
import com.fivemin.core.request.adapter.RequesterAdapterImpl
import com.fivemin.core.request.adapter.ResponseAdapterImpl
import com.fivemin.core.request.cookie.CookieControllerImpl
import com.fivemin.core.request.cookie.CookieSyncGradiant
import com.fivemin.core.request.cookie.CustomCookieJar
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.testng.Assert.fail
import org.testng.annotations.Test
import java.net.URI

class DefaultRequesterCoreTest {
    
    val performedReqA = PerformedRequesterInfo(RequesterEngineInfo("Default"), RequesterSlotInfo(0))
    val performedReqB = PerformedRequesterInfo(RequesterEngineInfo("Default"), RequesterSlotInfo(1))
    val performedReqC = PerformedRequesterInfo(RequesterEngineInfo("Default"), RequesterSlotInfo(2))
    
    val factory = CookieControllerImpl(listOf(CookieSyncGradiant(performedReqA, listOf(performedReqB))))
    
    fun generateCore(info: PerformedRequesterInfo, userAgent: String): DefaultRequesterCore {
        val io: DirectIO = mockk()
        
        every {
            io.getToken(any())
        } returns (mockk())
        
        return DefaultRequesterCore(
            RequesterExtraImpl(), info, RequesterConfig(factory), RequesterAdapterImpl(
                CustomCookieJar(),
                ResponseAdapterImpl(info, MemoryFilterFactoryImpl(io, HtmlDocumentFactoryImpl())),
                RequestHeaderProfile(userAgent = Some(userAgent))
            )
        )
    }
    
    @Test
    fun testRequestWith_UserAgent() {
        val coreA = generateCore(performedReqA, "abc")
        
        val cookieMakeDoc = DocumentMockFactory.getHttpRequest(
            URI("http://127.0.0.1:3000/headerReflect"), RequestType.LINK)
            .upgrade().upgradeAsDocument("a").upgradeAsRequestDoc().upgrade()
        
        val ret = runBlocking {
            coreA.request(DequeuedRequest(cookieMakeDoc, DequeuedRequestInfo())).await()
        }
        
        ret.fold({ fail() }, {
            it.responseBody.ifHttpSucc({
                it.body.ifString({
                    it.openStreamAsStringAndDispose {
                        val r = it.readText().lowercase()
                        if (!(r.contains("useragent: abc"))
                        ) else {
                            fail()
                        }
                    }
                }, {
                    fail()
                })
            }, {
                fail()
            })
        })
    }
    
    @Test
    fun testRequestWith_UserAgentOverride() {
        val coreA = generateCore(performedReqA, "abc")
    
        val cookieMakeDoc = DocumentMockFactory.getHttpRequest(
            URI("http://127.0.0.1:3000/headerReflect"), RequestType.LINK, profile = PerRequestHeaderProfile(
                Some(
                    RequestHeaderProfile(
                        userAgent = Some("abc123")
                    )
                ),
                none(),
                none(),
                URI("http://127.0.0.1:3000/headerReflect")
            )
        ).upgrade().upgradeAsDocument("a").upgradeAsRequestDoc().upgrade()
    
        val ret = runBlocking {
            coreA.request(DequeuedRequest(cookieMakeDoc, DequeuedRequestInfo())).await()
        }
    
        ret.fold({ fail() }, {
            it.responseBody.ifHttpSucc({
                it.body.ifString({
                    it.openStreamAsStringAndDispose {
                        val r = it.readText().lowercase()
                        if (!(r.contains("useragent: abc123"))
                        ) else {
                            fail()
                        }
                    }
                }, {
                    fail()
                })
            }, {
                fail()
            })
        })
    }
    
    @Test
    fun testRequestWith_Header() {
        val coreA = generateCore(performedReqA, "abc")
        
        val cookieMakeDoc = DocumentMockFactory.getHttpRequest(
            URI("http://127.0.0.1:3000/headerReflect"), RequestType.LINK, profile = PerRequestHeaderProfile(
                Some(
                    RequestHeaderProfile(
                        acceptEncoding = Some("gzip"),
                        acceptLanguage = Some("1234"),
                        userAgent = Some("abc")
                    )
                ),
                Some("unsafe_url"),
                Some(URI("https://aaa.com")),
                URI("http://127.0.0.1:3000/headerReflect")
            )
        ).upgrade().upgradeAsDocument("a").upgradeAsRequestDoc().upgrade()
        
        val ret = runBlocking {
            coreA.request(DequeuedRequest(cookieMakeDoc, DequeuedRequestInfo())).await()
        }
        
        ret.fold({ fail() }, {
            it.responseBody.ifHttpSucc({
                it.body.ifString({
                    it.openStreamAsStringAndDispose {
                        val r = it.readText().lowercase()
                        if (!(r.contains("encoding: gzip")
                                    and r.contains("language: 1234")
                                    and r.contains("connection: close")
                                    and r.contains("te: trailers")
                                    and r.contains("useragent: abc")
                                    and r.contains("referrer: https://aaa.com"))
                        ) else {
                            fail()
                        }
                    }
                }, {
                    fail()
                })
            }, {
                fail()
            })
        })
    }
    
    @Test
    fun testRequestWith_CookieIgnored() {
        val coreA = generateCore(performedReqA, "abc")
        val coreC = generateCore(performedReqC, "abc")
        
        val cookieMakeDoc =
            DocumentMockFactory.getHttpRequest(URI("http://127.0.0.1:3000/cookieMake"), RequestType.LINK).upgrade()
                .upgradeAsDocument("a").upgradeAsRequestDoc().upgrade()
        
        val cookieReflectDoc =
            DocumentMockFactory.getHttpRequest(URI("http://127.0.0.1:3000/cookieReflect"), RequestType.LINK).upgrade()
                .upgradeAsDocument("a").upgradeAsRequestDoc().upgrade()
        
        val ret = runBlocking {
            coreA.request(DequeuedRequest(cookieMakeDoc, DequeuedRequestInfo())).await()
            coreC.request(DequeuedRequest(cookieReflectDoc, DequeuedRequestInfo())).await()
        }
        
        ret.fold({ fail() }, {
            it.responseBody.ifHttpSucc({
                it.body.ifString({
                    it.openStreamAsStringAndDispose {
                        val r = it.readText()
                        if (r.contentEquals("null")) {
                        
                        } else {
                            fail()
                        }
                    }
                }, {
                    fail()
                })
            }, {
                fail()
            })
        })
    }
    
    @Test
    fun testRequestWith_Cookie() {
        val coreA = generateCore(performedReqA, "abc")
        
        val cookieMakeDoc =
            DocumentMockFactory.getHttpRequest(URI("http://127.0.0.1:3000/cookieMake"), RequestType.LINK).upgrade()
                .upgradeAsDocument("a").upgradeAsRequestDoc().upgrade()
        
        val cookieReflectDoc =
            DocumentMockFactory.getHttpRequest(URI("http://127.0.0.1:3000/cookieReflect"), RequestType.LINK).upgrade()
                .upgradeAsDocument("a").upgradeAsRequestDoc().upgrade()
        
        val ret = runBlocking {
            coreA.request(DequeuedRequest(cookieMakeDoc, DequeuedRequestInfo())).await()
            coreA.request(DequeuedRequest(cookieReflectDoc, DequeuedRequestInfo())).await()
        }
        
        ret.fold({ fail() }, {
            it.responseBody.ifHttpSucc({
                it.body.ifString({
                    it.openStreamAsStringAndDispose {
                        val r = it.readText()
                        if (r.contains("CookieMake1") and r.contains("CookieMake2")) {
                        
                        } else {
                            fail()
                        }
                    }
                }, {
                    fail()
                })
            }, {
                fail()
            })
        })
    }
    
    @Test
    fun testRequestWith_CookiePropagatedBetweenRequesters() {
        val coreA = generateCore(performedReqA, "abc")
        val coreB = generateCore(performedReqB, "abc")
        
        val cookieMakeDoc =
            DocumentMockFactory.getHttpRequest(URI("http://127.0.0.1:3000/cookieMake"), RequestType.LINK).upgrade()
                .upgradeAsDocument("a").upgradeAsRequestDoc().upgrade()
        
        val cookieReflectDoc =
            DocumentMockFactory.getHttpRequest(URI("http://127.0.0.1:3000/cookieReflect"), RequestType.LINK).upgrade()
                .upgradeAsDocument("a").upgradeAsRequestDoc().upgrade()
        
        val ret = runBlocking {
            coreA.request(DequeuedRequest(cookieMakeDoc, DequeuedRequestInfo())).await()
            coreB.request(DequeuedRequest(cookieReflectDoc, DequeuedRequestInfo())).await()
        }
        
        ret.fold({ fail() }, {
            it.responseBody.ifHttpSucc({
                it.body.ifString({
                    it.openStreamAsStringAndDispose {
                        val r = it.readText()
                        if (r.contains("CookieMake1") and r.contains("CookieMake2")) {
                        
                        } else {
                            fail()
                        }
                    }
                }, {
                    fail()
                })
            }, {
                fail()
            })
        })
    }
}