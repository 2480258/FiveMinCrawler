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

import arrow.core.identity
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.getCriticalErrorBodyResponse
import com.fivemin.core.DocumentMockFactory.Companion.getSuccResponse_Html
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsRequestDoc
import com.fivemin.core.ElemIterator
import com.fivemin.core.TaskMockFactory
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.StringUniqueKeyProvider
import com.fivemin.core.engine.transaction.UriUniqueKeyProvider
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test
import java.net.URI
import kotlin.test.assertEquals

class RetrySubPolicyTest {
    @Test
    fun testProcess_Retry() {
        val retry = RetrySubPolicy<Request>()
        
        val src = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK).upgrade().upgradeAsDocument("a")
        val response = src.upgradeAsRequestDoc().upgrade().getCriticalErrorBodyResponse()
        val dest = src.upgrade(response)
        
        val info = TaskMockFactory.createTaskInfo()
        val state = TaskMockFactory.createSessionStarted<Request>()
        
        runBlocking {
            retry.process(src, dest, state, ::identity)
        }
        
        coVerify (exactly = 1){
            state.retryAsync<Any>(any())
        }
    }
    
    @Test
    fun testProcess_Pass() {
        val retry = RetrySubPolicy<Request>()
        
        val src = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK).upgrade().upgradeAsDocument("a")
        val response = src.upgradeAsRequestDoc().upgrade().getSuccResponse_Html()
        val dest = src.upgrade(response)
        
        val info = TaskMockFactory.createTaskInfo()
        val state = TaskMockFactory.createSessionStarted<Request>()
        
        val result = runBlocking {
            retry.process(src, dest, state, ::identity)
        }
        
        result.fold({
            throw NullPointerException()
        }, {
            assertEquals(it, dest) // reference equals
        })
    }
}
