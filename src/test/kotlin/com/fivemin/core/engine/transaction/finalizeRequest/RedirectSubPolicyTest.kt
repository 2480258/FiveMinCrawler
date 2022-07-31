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

package com.fivemin.core.engine.transaction.finalizeRequest

import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.TaskMockFactory
import com.fivemin.core.engine.*
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.net.URI

class RedirectSubPolicyTest {
    
    val redirectSubPolicy = RedirectSubPolicy<Request>()
    
    @BeforeMethod
    fun setUp() {
    }
    
    fun getRedirectResponse(): ResponseData {
        val mockResponse: ResponseData = mockk(relaxed = true)
        val mockResponseBody: RedirectResponseBody = mockk(relaxed = true)
        
        every {
            mockResponse.responseBody
        } returns (mockResponseBody)
        
        every {
            mockResponseBody.redirectDest
        } returns (URI("https://aaa.com"))
        
        return mockResponse
    }
    
    @Test
    fun testProcess() {
        val req = DocumentMockFactory.getRequest(URI("https://a.com"), RequestType.LINK)
            .upgrade()
            .upgradeAsDocument("a")
        
        val fin = req.upgrade(getRedirectResponse())
        val info = TaskMockFactory.createTaskInfo()
        val state = TaskMockFactory.createSessionStarted<Request>()
        
        fin.result.map {
            it.responseBody.ifRedirect(
                {
                    println("1")
                }, {
                    println("0")
                }
            )
        }
        
        runBlocking {
            val result = redirectSubPolicy.process(req, fin, info, state).await()
        }
        coVerify(exactly = 1) {
            state.getChildSession<Any>(any())
        }
    }
}