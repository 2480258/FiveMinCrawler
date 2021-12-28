package fivemin.core.engine.transaction.serialize.postParser

import arrow.core.*
import fivemin.core.DocumentMockFactory
import fivemin.core.DocumentMockFactory.Companion.getRequest
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
                MemoryFilterFactoryImpl(DirectIOImpl(ConfigControllerImpl(), none()), HtmlDocumentFactoryImpl())
            )
        )
    }

    @Test
    fun referrerLinkMetaTest() {
        runBlocking {
            var req = HttpRequestImpl(
                none(), URI("http://127.0.0.1:30001/referrertest"), RequestType.LINK, PerRequestHeaderProfile(
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
                it.fold({fail()}) {
                    assertEquals(it, "no-referrer-when-downgrade")
                }
            })
        }
    }


    @Test
    fun referrerGlobalMetaTest() {
        runBlocking {
            var req = HttpRequestImpl(
                none(), URI("http://127.0.0.1:30001/referrermetatest"), RequestType.LINK, PerRequestHeaderProfile(
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
                            ref.extract(it.getElements(ParserNavigator("a")).first())
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
                it.fold({fail()}) {
                    assertEquals(it, "origin")
                }
            })
        }
    }

    @Test
    fun referrerHeaderTest() {
        runBlocking {
            var req = HttpRequestImpl(
                none(), URI("http://127.0.0.1:30001/referrerheadertest"), RequestType.LINK, PerRequestHeaderProfile(
                    RequestHeaderProfile(),
                    none(),
                    URI("http://127.0.0.1:30001/referrertest"),
                    URI("http://127.0.0.1:30001/referrertest")
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
                            ref.extract(it.getElements(ParserNavigator("a")).first())
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
                it.fold({fail()}) {
                    assertEquals(it, "no-referrer")
                }
            })
        }
    }
}