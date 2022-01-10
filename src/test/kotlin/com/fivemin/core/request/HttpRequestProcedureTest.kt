package com.fivemin.core.request

import com.fivemin.core.engine.PerformedRequesterInfo
import com.fivemin.core.engine.RequesterEngineInfo
import com.fivemin.core.engine.RequesterSlotInfo
import com.fivemin.core.request.adapter.RequesterAdapterImpl
import com.fivemin.core.request.cookie.* // ktlint-disable no-unused-imports
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.net.URI

class HttpRequestProcedureTest {

    lateinit var prod: HttpRequestProcedure
    lateinit var destRepo: CookieRepository

    @BeforeMethod
    fun before() {
        val cookieFac = mockk<CookieResolveTargetFactory>()
        val jar = CustomCookieJar()

        destRepo = CookieRepositoryImpl(CustomCookieJar())

        every {
            cookieFac.create(any(), any())
        } returns (CookieResolveTargetImpl(CookieRepositoryImpl(jar), OutwardPolicy(listOf(destRepo))))

        prod = HttpRequestProcedure(
            PerformedRequesterInfo(RequesterEngineInfo("a"), RequesterSlotInfo(0)),
            RequesterConfig(cookieFac),
            mockAdapter(jar)
        )
    }

    fun mockAdapter(jar: CustomCookieJar): RequesterAdapterImpl {
        val impl = mockk<RequesterAdapterImpl>()

        coEvery {
            impl.requestAsync(any())
        } coAnswers {
            val cookie = Cookie.parseAll(URI("http://aaa.com").toHttpUrlOrNull()!!, Headers.headersOf("set-cookie", "NAME=VALUE;"))

            jar.saveFromResponse(URI("http://aaa.com").toHttpUrlOrNull()!!, cookie)

            mockk()
        }

        return impl
    }

    @Test
    fun testRequest() {
        runBlocking {
            prod.request(mockk())
        }

        destRepo.getAllCookies().fold({ fail() }) {
            assertEquals(it.count(), 1)
            assertEquals(it.first().domain, "aaa.com")
            assertEquals(it.first().name, "NAME")
            assertEquals(it.first().value, "VALUE")
        }
    }
}
