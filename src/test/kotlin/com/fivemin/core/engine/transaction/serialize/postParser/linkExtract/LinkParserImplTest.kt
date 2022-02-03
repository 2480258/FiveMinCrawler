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

package com.fivemin.core.engine.transaction.serialize.postParser.linkExtract

import arrow.core.none
import arrow.core.toOption
import com.fivemin.core.ElemIterator
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.*
import com.fivemin.core.export.ConfigControllerImpl
import com.fivemin.core.parser.HtmlDocumentFactoryImpl
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

class LinkParserImplTest {

    lateinit var adapter: RequesterAdapterImpl
    val uriIt = ElemIterator(UriIterator())
    val fac = HtmlDocumentFactoryImpl()

    var parser = LinkParserImpl()

    @BeforeMethod
    fun before() {
        parser = LinkParserImpl()

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
    fun testParseReferrer() {
        runBlocking {
            var req = HttpRequestImpl(
                none(), URI("http://127.0.0.1:3000/referrerattributetest"), RequestType.LINK,
                PerRequestHeaderProfile(
                    RequestHeaderProfile().toOption(),
                    none(),
                    URI("https://localhost:12345").toOption(),
                    URI("https://localhost:44376/home/referrertest")
                ),
                TagRepositoryImpl()
            )
            var ret = adapter.requestAsync(req).await()

            ret.fold({ fail() }) {
                it.ifSuccAsync({
                    it.body.ifHtmlAsync({
                        it.parseAsHtmlDocumentAsync {
                            val link = parser.parse(it, uriIt.gen(), none())

                            link.first().referrerInfo.referrerPolicy.fold({ fail() }) {
                                assertEquals(it, "same-origin")
                            }
                            assert(link.first().referrerInfo.rel.isEmpty())

                            link.last().referrerInfo.rel.fold({ fail() }) {
                                assertEquals(it, "noreferrer")
                            }
                            assert(link.last().referrerInfo.referrerPolicy.isEmpty())
                        }
                    }, {
                        fail()
                    })
                }, {
                    fail()
                })
            }
        }
    }
}
