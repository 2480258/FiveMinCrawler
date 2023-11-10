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

package com.fivemin.core.engine.transaction.serialize.postParser.nonBlocking

import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.TaskMockFactory
import com.fivemin.core.engine.InitialOption
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.RequestType
import org.testng.annotations.Test

import org.testng.annotations.BeforeTest
import java.net.URI

class DownloadHandler_nonBlockingTest {
    
    @BeforeTest
    fun before() {
    
    }
    
    @Test(timeOut = 20000)
    fun downloadLinks_is_nonBlocking_ExcludeCI() {
        val state = TaskMockFactory.createSessionStarted<Request>()
        val request = DocumentMockFactory.getHttpRequest(URI("http://localhost:3000/timeOut"), RequestType.LINK)
        
        val ret = state.downloadLinksWithCrawlerRequest(InitialOption(), request)
        
        ret.invokeOnCompletion {
            Thread.sleep(1000000)
        }
        
        assert(!ret.isCompleted)
    }
    
    @Test(timeOut = 20000)
    fun downloadAttributes_is_nonBlocking_ExcludeCI() {
        val state = TaskMockFactory.createSessionStarted<Request>()
        val request = DocumentMockFactory.getHttpRequest(URI("http://localhost:3000/timeOut"), RequestType.ATTRIBUTE)
        
        val ret = state.downloadAttributeWithCrawlerRequest(InitialOption(), request)
        
        ret.invokeOnCompletion {
            Thread.sleep(1000000)
        }
        
        assert(!ret.isCompleted)
    }
}