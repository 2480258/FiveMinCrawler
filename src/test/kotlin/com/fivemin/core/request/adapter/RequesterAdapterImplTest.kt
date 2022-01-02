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
