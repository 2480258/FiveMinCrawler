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

package com.fivemin.core.engine.transaction.serialize.postParser

import arrow.core.*
import com.fivemin.core.ElemIterator
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.*
import com.fivemin.core.export.ConfigControllerImpl
import com.fivemin.core.parser.HtmlDocumentFactoryImpl
import com.fivemin.core.request.HttpResponseMessage
import com.fivemin.core.request.MemoryFilterFactoryImpl
import com.fivemin.core.request.RequestHeaderProfile
import com.fivemin.core.request.adapter.RequesterAdapterImpl
import com.fivemin.core.request.adapter.ResponseAdapterImpl
import com.fivemin.core.request.cookie.CustomCookieJar
import kotlinx.coroutines.runBlocking
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.net.URI

class ReferrerExtractorStreamTest {

    lateinit var adapter: RequesterAdapterImpl
    val uriIt = ElemIterator(UriIterator())
    val fac = HtmlDocumentFactoryImpl()

    @BeforeMethod
    fun setUp() {
        adapter = RequesterAdapterImpl(
            CustomCookieJar(),
            ResponseAdapterImpl(
                PerformedRequesterInfo(RequesterEngineInfo("A"), RequesterSlotInfo(0)),
                MemoryFilterFactoryImpl(DirectIOImpl(ConfigControllerImpl(""), none()), HtmlDocumentFactoryImpl())
            ),
            RequestHeaderProfile()
        )
    }

    @Test
    fun referrerLinkMetaTest() {
        runBlocking {
            var req = HttpRequestImpl(
                none(), URI("http://127.0.0.1:3000/referrertest"), RequestType.LINK,
                PerRequestHeaderProfile(
                    RequestHeaderProfile().toOption(),
                    none(),
                    URI("https://localhost:12345").toOption(),
                    URI("https://localhost:44376/home/referrertest")
                ),
                TagRepositoryImpl()
            )
            var ret = adapter.requestAsync(req).await()

            var c = ret.map {
                var msg =
                    HttpResponseMessage(it, PerformedRequesterInfo(RequesterEngineInfo("a"), RequesterSlotInfo(0)))
                val ref = ReferrerExtractorStream(msg)

                var ret = it.ifSuccAsync({
                    var ret = it.body.ifHtmlAsync({
                        var ret = it.parseAsHtmlDocumentAsync {
                            ref.extract(ParsedLink(uriIt.gen(), ReferrerInfo(Some("no-referrer-when-downgrade"), none())))
                        }

                        ret
                    }, {
                        fail()

                        IllegalArgumentException().left()
                    })

                    ret
                }, {
                    fail()

                    IllegalArgumentException().left()
                }).toOption()

                ret
            }.orNull().toOption().flatten()

            c.fold({ fail() }, {
                it.fold({ fail() }) {
                    assertEquals(it, "no-referrer-when-downgrade")
                }
            })
        }
    }

    @Test
    fun referrerGlobalMetaTest() {
        runBlocking {
            var req = HttpRequestImpl(
                none(), URI("http://127.0.0.1:3000/referrermetatest"), RequestType.LINK,
                PerRequestHeaderProfile(
                    RequestHeaderProfile().toOption(),
                    none(),
                    URI("https://localhost:12345").toOption(),
                    URI("https://localhost:44376/home/referrertest")
                ),
                TagRepositoryImpl()
            )
            var ret = adapter.requestAsync(req).await()

            var c = ret.map {
                var msg =
                    HttpResponseMessage(it, PerformedRequesterInfo(RequesterEngineInfo("a"), RequesterSlotInfo(0)))
                val ref = ReferrerExtractorStream(msg)

                var ret = it.ifSuccAsync({
                    var ret = it.body.ifHtmlAsync({
                        var ret = it.parseAsHtmlDocumentAsync {
                            ref.extract(ParsedLink(uriIt.gen(), ReferrerInfo(none(), none())))
                        }

                        ret
                    }, {
                        fail()

                        IllegalArgumentException().left()
                    })

                    ret
                }, {
                    fail()

                    IllegalArgumentException().left()
                }).toOption()

                ret
            }.orNull().toOption().flatten()

            c.fold({
                fail()
            }, {
                it.fold({ fail() }) {
                    assertEquals(it, "origin")
                }
            })
        }
    }

    @Test
    fun referrerHeaderTest() {
        runBlocking {
            var req = HttpRequestImpl(
                none(), URI("http://127.0.0.1:3000/referrerheadertest"), RequestType.LINK,
                PerRequestHeaderProfile(
                    RequestHeaderProfile().toOption(),
                    none(),
                    URI("http://127.0.0.1:3000/referrertest").toOption(),
                    URI("http://127.0.0.1:3000/referrertest")
                ),
                TagRepositoryImpl()
            )
            var ret = adapter.requestAsync(req).await()

            var c = ret.map {
                var msg =
                    HttpResponseMessage(it, PerformedRequesterInfo(RequesterEngineInfo("a"), RequesterSlotInfo(0)))
                val ref = ReferrerExtractorStream(msg)

                var ret = it.ifSuccAsync({
                    var ret = it.body.ifHtmlAsync({
                        var ret = it.parseAsHtmlDocumentAsync {
                            ref.extract(ParsedLink(uriIt.gen(), ReferrerInfo(none(), none())))
                        }

                        ret
                    }, {
                        fail()

                        IllegalArgumentException().left()
                    })

                    ret
                }, {
                    fail()

                    IllegalArgumentException().left()
                }).toOption()

                ret
            }.orNull().toOption().flatten()

            c.fold({ fail() }, {
                it.fold({ fail() }) {
                    assertEquals(it, "no-referrer")
                }
            })
        }
    }
}
