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

package com.fivemin.core.engine.transaction.serialize.postParser

import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.TaskMockFactory
import com.fivemin.core.engine.InitialOption
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.RequestType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test

import org.testng.annotations.BeforeTest
import java.net.URI

class DownloadHandlerImplTest {
    
    lateinit var handler: DownloadHandlerImpl
    
    @BeforeTest
    fun before() {
        handler = DownloadHandlerImpl()
    }
    
    @Test(timeOut = 10000)
    fun downloadLinks_is_NonBlocking() {
        val state = TaskMockFactory.createSessionStarted<Request>()
        val parent = DocumentMockFactory.getHttpRequest(URI("http://localhost:3000/timeOut"), RequestType.LINK)
        
        var finished = false
        
        state.quick_DownloadLinks(InitialOption(), parent).invokeOnCompletion {
            Thread.sleep(100000)
            finished = true
        }
        
        
        assert(!finished)
    }
    
    @Test(timeOut = 10000)
    fun downloadAttributes_is_NonBlocking() {
        val state = TaskMockFactory.createSessionStarted<Request>()
        val parent = DocumentMockFactory.getHttpRequest(URI("http://localhost:3000/timeOut"), RequestType.ATTRIBUTE)
        
        var finished = false
        
        state.quick_DownloadAttributes(InitialOption(), parent).invokeOnCompletion {
            Thread.sleep(100000)
            finished = true
        }
        
        
        assert(!finished)
    }
}