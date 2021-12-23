package fivemin.core.request

import arrow.core.none
import fivemin.core.DocumentMockFactory
import fivemin.core.ElemIterator
import fivemin.core.UriIterator
import fivemin.core.engine.*
import fivemin.core.export.ConfigControllerImpl
import fivemin.core.parser.HtmlDocumentFactoryImpl
import fivemin.core.request.adapter.RequesterAdapterImpl
import fivemin.core.request.adapter.ResponseAdapterImpl
import fivemin.core.request.cookie.CookieResolveTarget
import fivemin.core.request.cookie.CookieResolveTargetFactory
import fivemin.core.request.cookie.CustomCookieJar
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
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
                MemoryFilterFactoryImpl(DirectIOImpl(ConfigControllerImpl(), none()), HtmlDocumentFactoryImpl())
            )
        )
    }

    @Test
    fun errRedirectRequestTest() {

        var req = HttpRequestImpl(
            none(), URI("https://localhost:44376/Home/RedirectSource"), RequestType.LINK, PerRequestHeaderProfile(
                RequestHeaderProfile(), none(), URI("https://localhost:12345"), URI("https://localhost:44376/Home/RedirectSource")
            ), TagRepositoryImpl()
        )

        runBlocking {
            var ret = adapter.requestAsync(req)
            var cret = ret.await()


            cret.map {
                it.ifRedirect({ x ->
                    assertEquals(x.code, 302)
                    assertEquals(x.redirectDest.toString(), "/Home/RedirectDest")
                }, {
                    fail()
                })
            }
        }
    }

    @Test
    fun errTimeoutRequestTest() {

        var req = HttpRequestImpl(
            none(), URI("https://localhost:12345/where"), RequestType.LINK, PerRequestHeaderProfile(
                RequestHeaderProfile(), none(), URI("https://localhost:12345"), URI("https://localhost:12345")
            ), TagRepositoryImpl()
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
            none(), URI("https://localhost:44376/where"), RequestType.LINK, PerRequestHeaderProfile(
                RequestHeaderProfile(), none(), URI("https://localhost:44376"), URI("https://localhost:44376/where")
            ), TagRepositoryImpl()
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
            none(), URI("https://localhost:44376"), RequestType.LINK, PerRequestHeaderProfile(
                RequestHeaderProfile(), none(), URI("https://localhost:44376"), URI("https://localhost:44376")
            ), TagRepositoryImpl()
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