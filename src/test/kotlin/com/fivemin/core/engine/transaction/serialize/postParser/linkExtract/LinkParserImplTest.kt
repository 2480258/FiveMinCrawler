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
                none(), URI("http://127.0.0.1:30001/referrerattributetest"), RequestType.LINK,
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
