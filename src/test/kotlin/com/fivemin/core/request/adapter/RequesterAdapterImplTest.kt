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

package com.fivemin.core.request.adapter

import arrow.core.Either
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.engine.RequestType
import com.fivemin.core.request.RequestHeaderProfile
import com.fivemin.core.request.cookie.CustomCookieJar
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.net.URI

class RequesterAdapterImplTest {

    lateinit var req: RequesterAdapterImpl

    fun mockResponseAdapterThrows(): ResponseAdapterImpl {
        var mock = mockk<ResponseAdapterImpl>()

        every {
            mock.createWithReceived(any(), any(), any())
        } answers {
            throw IllegalArgumentException()
        }

        every {
            mock.createWithError(any(), any(), any())
        } returns (Either.Left(IllegalArgumentException()))

        return mock
    }

    @BeforeMethod
    fun before() {
    }

    @Test
    fun testRequestAsyncThrows() {

        runBlocking {
            req = RequesterAdapterImpl(CustomCookieJar(), mockResponseAdapterThrows(), RequestHeaderProfile())
            var ret = req.requestAsync(DocumentMockFactory.getRequest(URI("http://127.0.0.1:30001"), RequestType.LINK))
            ret.await().map {
                fail()
            }
        }
    }
}
