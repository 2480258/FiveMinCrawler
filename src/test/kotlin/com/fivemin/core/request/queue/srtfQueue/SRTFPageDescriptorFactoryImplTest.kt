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

package com.fivemin.core.request.queue.srtfQueue

import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsAttribute
import com.fivemin.core.engine.RequestType
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test

import java.net.URI
import kotlin.test.assertEquals

class SRTFPageDescriptorFactoryImplTest {
    
    @Test
    fun testConvertTo_Attribute_NoExt() {
        val doc_a = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.ATTRIBUTE).upgrade()
            .upgradeAsAttribute()
        val doc_b = DocumentMockFactory.getRequest(URI("https://aaa.com/abc"), RequestType.ATTRIBUTE).upgrade()
            .upgradeAsAttribute()
    
    
        val factory = SRTFPageDescriptorFactoryImpl()
        runBlocking {
            val a = factory.convertTo(doc_a)
            val b= factory.convertTo(doc_b)
            
            assertEquals(a, b)
        }
    }
    
    
    @Test
    fun testConvertTo_Attribute_Ext() {
        val doc_a = DocumentMockFactory.getRequest(URI("https://aaa.com/a.jpg"), RequestType.ATTRIBUTE).upgrade()
            .upgradeAsAttribute()
        val doc_b = DocumentMockFactory.getRequest(URI("https://aaa.com/abc/a.jpg"), RequestType.ATTRIBUTE).upgrade()
            .upgradeAsAttribute()
        
        
        val factory = SRTFPageDescriptorFactoryImpl()
        runBlocking {
            val a = factory.convertTo(doc_a)
            val b= factory.convertTo(doc_b)
            
            assertEquals(a, b)
        }
    }
}