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

package io.fivemin.server.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fivemin.core.engine.Request
import com.fivemin.core.logger.WebSocketLogger
import io.mockk.every
import io.mockk.mockk
import org.testng.Assert.*
import org.testng.annotations.Test
import java.net.URI

class SerializedLogItemTest {
    @Test
    fun serializeEnqueueLogItemTest() {
        val objectMapper = ObjectMapper()
        val req : Request = mockk()
        
        every {
            req.target
        } returns (URI("http://aaa.com"))
        
        
        val item = SerializedLogItem(WebSocketLogger.EnqueueLogItem(0.1, req))
        val serialized = objectMapper.writeValueAsString(item)
        
        val ret = objectMapper.readValue(serialized, SerializedLogItem::class.java)
        
        assertEquals(ret, item)
    }
    
    @Test
    fun serializeDequeueLogItemTest() {
        val objectMapper = ObjectMapper()
        val req : Request = mockk()
        
        every {
            req.target
        } returns (URI("http://aaa.com"))
        
        
        val item = SerializedLogItem(WebSocketLogger.DequeueLogItem(req))
        val serialized = objectMapper.writeValueAsString(item)
        
        val ret = objectMapper.readValue(serialized, SerializedLogItem::class.java)
        
        assertEquals(ret, item)
    }
}