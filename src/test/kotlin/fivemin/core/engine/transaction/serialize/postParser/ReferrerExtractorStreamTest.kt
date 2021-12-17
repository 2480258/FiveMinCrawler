package fivemin.core.engine.transaction.serialize.postParser

import arrow.core.flatten
import arrow.core.invalid
import arrow.core.none
import arrow.core.toOption
import fivemin.core.DocumentMockFactory
import fivemin.core.DocumentMockFactory.Companion.getSuccResponse
import fivemin.core.DocumentMockFactory.Companion.upgrade
import fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import fivemin.core.DocumentMockFactory.Companion.upgradeAsRequestReq
import fivemin.core.ElemIterator
import fivemin.core.UriIterator
import fivemin.core.engine.*
import fivemin.core.export.ConfigControllerImpl
import fivemin.core.parser.HtmlDocumentFactoryImpl
import fivemin.core.request.HttpResponseMessage
import fivemin.core.request.MemoryFilterFactoryImpl
import fivemin.core.request.RequestHeaderProfile
import fivemin.core.request.adapter.RequesterAdapterImpl
import fivemin.core.request.adapter.ResponseAdapterImpl
import fivemin.core.request.cookie.CustomCookieJar
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test

import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
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
                MemoryFilterFactoryImpl(DirectIOImpl(ConfigControllerImpl()), HtmlDocumentFactoryImpl())
            )
        )
    }


    @Test
    fun testExtract() {
        runBlocking {
            var req = HttpRequestImpl(
                none(), URI("https://localhost:44376/home/referrertest"), RequestType.LINK, PerRequestHeaderProfile(
                    RequestHeaderProfile(),
                    none(),
                    URI("https://localhost:12345"),
                    URI("https://localhost:44376/home/referrertest")
                ), TagRepositoryImpl()
            )
            var ret = adapter.requestAsync(req).await()

            var c = ret.map {
                var msg =
                    HttpResponseMessage(it, PerformedRequesterInfo(RequesterEngineInfo("a"), RequesterSlotInfo(0)))
                val ref = ReferrerExtractorStream(msg)

                var ret = it.ifSucc({
                    var ret = it.body.ifHtml({
                        var ret = it.parseAsHtmlDocument {
                            ref.extract(it.getElements(ParserNavigator("a")).last())
                        }

                        ret
                    }, {
                        fail()

                        IllegalArgumentException().invalid()
                    })

                    ret
                }, {
                    fail()

                    IllegalArgumentException().invalid()
                }).toOption()

                ret
            }.toOption().flatten()

            c.fold({ fail() }, {
                assertEquals(c, "no-referrer-when-downgrade")
            })
        }
    }
}