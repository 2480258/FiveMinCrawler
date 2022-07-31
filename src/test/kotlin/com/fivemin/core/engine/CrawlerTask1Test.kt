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

package com.fivemin.core.engine

import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.TaskMockFactory
import io.mockk.coVerify
import kotlinx.coroutines.runBlocking
import org.testng.Assert.fail
import org.testng.annotations.Test
import java.net.URI
import kotlin.test.assertEquals

class CrawlerTask1Test {
    
    @Test
    fun testStart_With_Throwing_Policy() {
        val crawlerTask1 = CrawlerTask1(TaskMockFactory.createThrowingPolicy())
        
        val doc = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK).upgrade()
        val state = TaskMockFactory.createSessionInitState()
        val result = runBlocking {
            crawlerTask1.start(doc, TaskMockFactory.createTaskInfo(), state).await()
        }
        
        result.fold({
            assert(it is NullPointerException)
        }, {
            fail()
        })
        
        coVerify(exactly = 1) {
            state.start<Any>(any(), any())
        }
    }
    
    @Test
    fun testStart_With_Falling_Policy() {
        val crawlerTask1 = CrawlerTask1(TaskMockFactory.createFallingPolicy())
        
        val doc = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK).upgrade()
        val state = TaskMockFactory.createSessionInitState()
    
        val result = runBlocking {
            crawlerTask1.start(doc, TaskMockFactory.createTaskInfo(), state).await()
        }
        
        result.fold({
            assert(it is NullPointerException)
        }, {
            fail()
        })
    
        coVerify(exactly = 1) {
            state.start<Any>(any(), any())
        }
    }
    
    @Test
    fun testStart_With_Succ_Policy() {
        val policySet = TaskMockFactory.createPolicySet()
        
        val crawlerTask1 = CrawlerTask1(policySet.find<InitialTransaction<Request>, PrepareTransaction<Request>>())
    
        val doc = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK).upgrade()
        val state = TaskMockFactory.createSessionInitState()
    
        val result = runBlocking {
            crawlerTask1.start(doc, TaskMockFactory.createTaskInfo(), state).await()
        }
    
        result.fold({
            fail()
        }, {
            assertEquals(it.request, doc.request)
        })
    
        coVerify(exactly = 1) {
            state.start<Any>(any(), any())
        }
    }
}