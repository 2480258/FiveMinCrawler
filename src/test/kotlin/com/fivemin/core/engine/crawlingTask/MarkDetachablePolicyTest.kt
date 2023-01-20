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

package com.fivemin.core.engine.crawlingTask

import arrow.core.identity
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.TaskMockFactory
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.RequestType
import com.fivemin.core.engine.WorkingSetMode
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test

import org.testng.Assert.*
import java.net.URI

@MockKExtension.ConfirmVerification
class MarkDetachablePolicyTest {
    
    @Test
    fun testProcess_Enabled() {
        val init = DocumentMockFactory.getRequest(URI("https://a.a.com"), RequestType.LINK)
            .upgrade()
        val preproc = init.upgradeAsDocument("a", WorkingSetMode.Enabled)
        
        val detach = MarkDetachablePolicy<Request>()
        val state = TaskMockFactory.createSessionStarted<Request>()
        
        
        
        runBlocking {
            detach.process(init, preproc, state, ::identity)
        }
        
        verify { state.setDetachable() }
        verify(exactly = 0) { state.setNonDetachable() }
    }
    
    @Test
    fun testProcess_Disabled() {
        val init = DocumentMockFactory.getRequest(URI("https://a.a.com"), RequestType.LINK)
            .upgrade()
        val preproc = init.upgradeAsDocument("a", WorkingSetMode.Disabled)
        
        val detach = MarkDetachablePolicy<Request>()
        val state = TaskMockFactory.createSessionStarted<Request>()
        
        
        
        runBlocking {
            detach.process(init, preproc, state, ::identity)
        }
        
        verify { state.setNonDetachable() }
        verify(exactly = 0) { state.setDetachable() }
    }
}