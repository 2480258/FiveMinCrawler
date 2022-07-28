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
import com.fivemin.core.DocumentMockFactory.Companion.getSuccResponse_Html
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsAttribute
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsRequestReq
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test

import org.testng.Assert.*
import java.net.URI

class DocumentAttributeFactoryImplTest {
    
    fun mockDocumentInfo() : DocumentAttributeInfo{
        val info = DocumentAttributeInfo("a")
        
        return info
    }
    
    @Test
    fun testGetInternal() {
        val factory = DocumentAttributeFactoryImpl()
        val result = runBlocking {
            factory.getInternal(mockDocumentInfo(), listOf("a", "b", "c")).fold({throw it}, {
                assertEquals(it.info, mockDocumentInfo())
                assertEquals(it.item.count(), 3)
                
                val isText = it.item.map {
                    it.match({1}, {0})
                }
                
                assertEquals(isText.sum(), 3)
            })
        }
    }
    
    @Test
    fun testGetExternal() {
        val factory = DocumentAttributeFactoryImpl()
        val docs = (0 until 3).map {
            val data = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.ATTRIBUTE).upgrade().upgradeAsAttribute().upgradeAsRequestReq().upgrade().getSuccResponse_Html("abc")
            
            DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.ATTRIBUTE).upgrade().upgradeAsAttribute().upgrade(data)
        }
        
        runBlocking {
            val result = factory.getExternal(mockDocumentInfo(), docs)
            result.fold({throw it}, {
                assertEquals(it.item.count(), 3)
    
    
                val isText = it.item.map {
                    it.match({1}, {0})
                }
                
                assertEquals(isText.sum(), 0)
            })
        }
    }
}