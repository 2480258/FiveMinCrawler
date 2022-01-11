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
                none(), URI("http://127.0.0.1:30001/referrertest"), RequestType.LINK,
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
                none(), URI("http://127.0.0.1:30001/referrermetatest"), RequestType.LINK,
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
                none(), URI("http://127.0.0.1:30001/referrerheadertest"), RequestType.LINK,
                PerRequestHeaderProfile(
                    RequestHeaderProfile().toOption(),
                    none(),
                    URI("http://127.0.0.1:30001/referrertest").toOption(),
                    URI("http://127.0.0.1:30001/referrertest")
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
