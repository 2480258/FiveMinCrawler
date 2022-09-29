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

import arrow.core.right
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsRequestDoc
import com.fivemin.core.engine.*
import com.fivemin.core.request.adapter.RequesterAdapterImpl
import com.fivemin.core.request.adapter.ResponseAdapterImpl
import com.fivemin.core.request.cookie.*
import com.fivemin.core.request.queue.srtfQueue.WSQueue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.testng.Assert.fail
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.net.URI

class RequesterTaskImplTest {
    
    lateinit var task: RequesterTaskImpl
    lateinit var queue: WSQueue
    @BeforeMethod
    fun before() {
        val selector: RequesterSelectorImpl = mockk()
        
        coEvery {
            selector.schedule<Request, ResponseData>((any()))
        } coAnswers {
            RequesterSelected<ResponseData>(
                createAdapter(),
                PerformedRequesterInfo(RequesterEngineInfo("a"), RequesterSlotInfo(0))
            ).right()
        }
        
        queue = mockk()
        
        coEvery {
            queue.enqueue(any(), any())
        } coAnswers {
            secondArg<EnqueueRequestInfo>().callBack(DequeuedRequest(firstArg(), DequeuedRequestInfo()).right())
        }
        
        coEvery {
            queue.cancelWSSet(any())
        } coAnswers {
            1
        }
        
        
        task = RequesterTaskImpl(RequestTaskOption(selector, queue))
    }
    
    fun createAdapter(): DefaultRequesterCore {
        val cookieFac = mockk<CookieResolveTargetFactory>()
        val jar = CustomCookieJar()
        
        val filterMock: MemoryFilter = mockk()
        every {
            filterMock.write(any(), any(), any())
        } returns (mockk())
        
        every {
            filterMock.flushAndExportAndDispose()
        } returns (mockk())
        
        every {
            filterMock.close()
        } returns (mockk())
        
        val mock: MemoryFilterFactory = mockk()
        
        every {
            mock.createHtmlFilter(any(), any(), any())
        } returns (filterMock)
        
        val req = RequesterAdapterImpl(CustomCookieJar(), ResponseAdapterImpl(mockk(), mock), RequestHeaderProfile())
        
        every {
            cookieFac.create(any(), any())
        } returns (CookieResolveTargetImpl(CookieRepositoryImpl(jar), OutwardPolicyImpl(listOf())))
        
        val reqExtra = RequesterExtraImpl()
        
        return DefaultRequesterCore(
            reqExtra,
            PerformedRequesterInfo(RequesterEngineInfo("a"), RequesterSlotInfo(0)),
            RequesterConfig(cookieFac),
            req
        )
    }
    
    @Test
    fun testRun() {
        val doc = DocumentMockFactory.getHttpRequest(URI("http://localhost:3000/home"), RequestType.LINK)
            .upgrade()
            .upgradeAsDocument("a")
            .upgradeAsRequestDoc()
        
        
        runBlocking {
            val job = task.run<Request, ResponseData>(doc)
            
            job.await().fold({ fail() }, {
                it.responseBody.ifHttpSucc({
                    assert(it.contentType.subType.contains("html"))
                }, { fail() })
            })
        }
    }
    
    @Test
    fun testRunCancel() {
        val doc = DocumentMockFactory.getHttpRequest(URI("http://localhost:3000/timeOut"), RequestType.LINK)
            .upgrade()
            .upgradeAsDocument("a")
            .upgradeAsRequestDoc()
        
        
        runBlocking {
            val job = task.run<Request, ResponseData>(doc)
            
            delay(1000)
            job.cancelAndJoin()
            assert(job.getCompletionExceptionOrNull() is CancellationException)
        }
        
        coVerify(exactly = 1) {
            queue.cancelWSSet(any())
        }
    }
}