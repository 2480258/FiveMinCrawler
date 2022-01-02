package com.fivemin.core.request

import arrow.core.none
import arrow.core.toOption
import com.fivemin.core.ElemIterator
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.*
import com.fivemin.core.export.ConfigControllerImpl
import com.fivemin.core.parser.HtmlDocumentFactoryImpl
import com.fivemin.core.request.adapter.RequesterAdapterImpl
import com.fivemin.core.request.adapter.ResponseAdapterImpl
import com.fivemin.core.request.cookie.CustomCookieJar
import kotlinx.coroutines.runBlocking
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.fail

class RequesterAdapterImplTest {

    lateinit var adapter: RequesterAdapterImpl
    val uriIt = ElemIterator(UriIterator())

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
    fun errRedirectRequestTest() {

        var req = HttpRequestImpl(
            none(), URI("http://127.0.0.1:30001/redirect"), RequestType.LINK,
            PerRequestHeaderProfile(
                RequestHeaderProfile().toOption(), none(), URI("https://localhost:12345").toOption(), URI("http://127.0.0.1:30001/redirect")
            ),
            TagRepositoryImpl()
        )

        runBlocking {
            var ret = adapter.requestAsync(req)
            var cret = ret.await()

            cret.map {
                it.ifRedirect({ x ->
                    assertEquals(x.code, 302)
                    assertEquals(x.redirectDest.toString(), "/about")
                }, {
                    fail()
                })
            }
        }
    }

    @Test
    fun errTimeoutRequestTest() {

        var req = HttpRequestImpl(
            none(), URI("https://localhost:12345/where"), RequestType.LINK,
            PerRequestHeaderProfile(
                RequestHeaderProfile().toOption(), none(), URI("https://localhost:12345").toOption(), URI("https://localhost:12345")
            ),
            TagRepositoryImpl()
        )

        runBlocking {
            var ret = adapter.requestAsync(req)
            var cret = ret.await()

            cret.map {
                it.ifCriticalErr({
                    assertEquals(it.error.isNotEmpty(), true)
                }, {
                    fail()
                })
            }
        }
    }

    @Test
    fun err404RequestTest() {

        var req = HttpRequestImpl(
            none(), URI("http://127.0.0.1:30001/nowhere"), RequestType.LINK,
            PerRequestHeaderProfile(
                RequestHeaderProfile().toOption(), none(), URI("https://localhost:44376").toOption(), URI("https://localhost:44376/where")
            ),
            TagRepositoryImpl()
        )

        runBlocking {
            var ret = adapter.requestAsync(req)
            var cret = ret.await()

            cret.map {
                it.ifRecoverableErr({
                    assertEquals(it.code, 404)
                }, {
                    fail()
                })
            }
        }
    }

    @Test
    fun succRequestTest() {

        var req = HttpRequestImpl(
            none(), URI("http://127.0.0.1:30001/home"), RequestType.LINK,
            PerRequestHeaderProfile(
                RequestHeaderProfile().toOption(), none(), URI("https://localhost:44376").toOption(), URI("https://localhost:44376")
            ),
            TagRepositoryImpl()
        )

        runBlocking {
            var ret = adapter.requestAsync(req)
            var cret = ret.await()

            cret.map {
                it.ifSucc({
                    it.body.openStreamAsByteAndDispose {
                        assertEquals(it.available() > 1, true)
                    }
                }, {
                    fail()
                })
            }
        }
    }
}
