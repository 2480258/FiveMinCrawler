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

package com.fivemin.core.engine.transaction.prepareRequest

import arrow.core.identity
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.TaskMockFactory
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.RequestType
import com.fivemin.core.engine.WorkingSetMode
import io.mockk.coVerify
import kotlinx.coroutines.runBlocking
import org.testng.Assert.assertEquals
import org.testng.Assert.assertThrows
import org.testng.annotations.Test
import java.net.URI

class DetachableSubPolicyTest {
    
    @Test
    fun testProcess_Not_WS() {
        val detachSubPolicy = DetachableSubPolicy<Request>()
        val doc = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK).upgrade()
        val prep = doc.upgradeAsDocument("a", WorkingSetMode.Disabled)
        
        val info = TaskMockFactory.createTaskInfo()
        val state = TaskMockFactory.createSessionStarted<Request>()
        
        val result = runBlocking {
            detachSubPolicy.process(doc, prep, state, ::identity)
        }
        
        result.fold({ throw NullPointerException() }, {
            assertEquals(it, prep) // reference equals
        })
    }
    
    @Test
    fun testProcess_WS_With_Not_Detachable() {
        val detachSubPolicy = DetachableSubPolicy<Request>()
        val doc = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK).upgrade()
        val prep = doc.upgradeAsDocument("a", WorkingSetMode.Enabled)
        
        val info = TaskMockFactory.createTaskInfo()
        val state = TaskMockFactory.createSessionStarted<Request>()
        
        val result = runBlocking {
            detachSubPolicy.process(doc, prep, state, ::identity)
        }
        
        result.fold({ throw NullPointerException() }, {
            assertEquals(it, prep) // reference equals
        })
    }
    
    @Test
    fun testProcess_WS_With_Detachable() {
        val detachSubPolicy = DetachableSubPolicy<Request>()
        val doc = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK).upgrade()
        val prep = doc.upgradeAsDocument("a", WorkingSetMode.Enabled)
        
        val info = TaskMockFactory.createTaskInfo()
        val state = TaskMockFactory.createDetachableSessionStarted<Request>()
        
        assertThrows {
            runBlocking {
                detachSubPolicy.process(doc, prep, state, ::identity)
            }
        }
        
        coVerify {
            state.detach(any())
        }
    }
    
}