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

package com.fivemin.core.logger

import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsRequestDoc
import com.fivemin.core.engine.RequestType
import com.fivemin.core.request.EnqueueRequestInfo
import com.fivemin.core.request.queue.srtfQueue.SRTFOptimizationPolicyImpl
import com.fivemin.core.request.queue.srtfQueue.SRTFPageDescriptorFactoryImpl
import com.fivemin.core.request.queue.srtfQueue.SRTFTimingRepositoryImpl
import com.fivemin.core.request.queue.srtfQueue.WSQueue
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test

import org.testng.Assert.*
import org.testng.annotations.AfterTest
import org.testng.annotations.BeforeTest
import java.net.URI
import kotlin.system.exitProcess

class QueueLoggerTest {
    
    @Test
    fun testEnqueueLogToNetwork() {
        try {
            val wsLogger : WebSocketLogger = mockk()
    
            every {
                wsLogger.logViaNetworkEnqueue(any(), any())
            } returns (Unit)
    
            QueueLogger.webSocketLogger = wsLogger
    
            val timing = SRTFTimingRepositoryImpl()
            val optimizationPolicy = SRTFOptimizationPolicyImpl(timing)
            val pagedesc = SRTFPageDescriptorFactoryImpl()
            val keyExtractor = optimizationPolicy
            val que = WSQueue(optimizationPolicy, keyExtractor, pagedesc)
    
            val req = DocumentMockFactory.getRequest(URI("http://aa.com"), RequestType.LINK)
                .upgrade()
                .upgradeAsDocument("a")
                .upgradeAsRequestDoc()
                .upgrade()
    
            runBlocking {
                que.enqueue(req, EnqueueRequestInfo {  })
            }
    
            verify {
                wsLogger.logViaNetworkEnqueue(any(), any())
            }
        } finally {
            QueueLogger.webSocketLogger = null
        }

    }
    
    @Test
    fun testDequeueLogToWebSocket() {
        try {
            val wsLogger : WebSocketLogger = mockk()
            QueueLogger.webSocketLogger = wsLogger
    
            every {
                wsLogger.logViaNetworkEnqueue(any(), any())
            } returns (Unit)
    
            every {
                wsLogger.logViaNetworkDequeue(any())
            } returns (Unit)
    
            val timing = SRTFTimingRepositoryImpl()
            val optimizationPolicy = SRTFOptimizationPolicyImpl(timing)
            val pagedesc = SRTFPageDescriptorFactoryImpl()
            val keyExtractor = optimizationPolicy
            val que = WSQueue(optimizationPolicy, keyExtractor, pagedesc)
    
            val req = DocumentMockFactory.getRequest(URI("http://aa.com"), RequestType.LINK)
                .upgrade()
                .upgradeAsDocument("a")
                .upgradeAsRequestDoc()
                .upgrade()
    
            runBlocking {
                que.enqueue(req, EnqueueRequestInfo {  })
                delay(500)
            }
    
            verify {
                wsLogger.logViaNetworkDequeue(any())
            }
    
            return
        } finally {
            QueueLogger.webSocketLogger = null
        }
    }
}