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

import com.fivemin.core.engine.PerformedRequesterInfo
import com.fivemin.core.engine.RequesterEngineInfo
import com.fivemin.core.engine.RequesterSlotInfo
import com.fivemin.core.request.adapter.RequesterAdapterImpl
import com.fivemin.core.request.cookie.* // ktlint-disable no-unused-imports
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
        } returns (CookieResolveTargetImpl(CookieRepositoryImpl(jar), OutwardPolicyImpl(listOf(destRepo))))

        prod = HttpRequestProcedure(
            PerformedRequesterInfo(RequesterEngineInfo("a"), RequesterSlotInfo(0)),
            RequesterConfig(cookieFac),
            mockAdapter(jar)
        )
    }

    fun mockAdapter(jar: CustomCookieJar): RequesterAdapterImpl {
        val impl = mockk<RequesterAdapterImpl>()
        val cookieRepo = CookieRepositoryImpl(jar)
        coEvery {
            impl.requestAsync(any())
        } coAnswers {
            val cookie = Cookie.parseAll(URI("http://aaa.com").toHttpUrlOrNull()!!, Headers.headersOf("set-cookie", "NAME=VALUE;"))

            jar.saveFromResponse(URI("http://aaa.com").toHttpUrlOrNull()!!, cookie)

            coroutineScope {
                async {
                    mockk()
                }
            }
        }
        
        every {
            impl.cookieRepository
        } returns (cookieRepo)

        return impl
    }

    @Test
    fun testRequest() {
        runBlocking {
            prod.request(mockk())
        }

        destRepo.getAllCookies_Interlocked().fold({ fail() }) {
            assertEquals(it.count(), 1)
            assertEquals(it.first().domain, "aaa.com")
            assertEquals(it.first().name, "NAME")
            assertEquals(it.first().value, "VALUE")
        }
    }
}
