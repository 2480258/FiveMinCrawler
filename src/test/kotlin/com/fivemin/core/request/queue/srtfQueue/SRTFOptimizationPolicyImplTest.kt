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

import arrow.core.Some
import arrow.core.none
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsRequestDoc
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.RequestToken
import com.fivemin.core.engine.RequestType
import com.fivemin.core.engine.WorkingSetMode
import com.fivemin.core.request.PreprocessedRequest
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test

import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import java.net.URI
import kotlin.time.Duration.Companion.minutes

class SRTFOptimizationPolicyImplTest {
    
    lateinit var optimizationPolicy: SRTFOptimizationPolicy
    lateinit var desc: SRTFPageDescriptorFactoryImpl
    lateinit var timingRepo: SRTFTimingRepository
    
    @BeforeMethod
    fun before() {
        timingRepo = SRTFTimingRepositoryImpl()
        desc = SRTFPageDescriptorFactoryImpl()
        optimizationPolicy = SRTFOptimizationPolicyImpl(timingRepo)
    }
    
    @Test
    fun testOnlyWorkingSetDocument_ReturnsNone() {
        // Only WS is seen by scheduler; scheduler expects 0s
        // scheduler works only when working set A has more document to be downloaded than working set B
        // in this case, B's document will be scheduled first
        
        val root = giveSamplesAndRemoveDescriptor("a", null, WorkingSetMode.Enabled)
        val child1 = giveSamplesAndRemoveDescriptor("b", root, WorkingSetMode.Disabled)
        val child2 = giveSamplesAndRemoveDescriptor("c", root, WorkingSetMode.Disabled)
        
        
        
        runBlocking {
            val score = optimizationPolicy.getScore(getSamplesWithoutReporing("a", null, WorkingSetMode.Enabled))
            
            assertEquals(score, Some(0.minutes))
        }
    }
    
    @Test
    fun testWorkingSetWithChildDocument_ReturnsNone() {
        // a page with already reported page name;
        val root = giveSamples("a", null, WorkingSetMode.Enabled)
        val child1 = giveSamplesAndRemoveDescriptor("b", root, WorkingSetMode.Disabled)
        
        
        
        runBlocking {
            val score = optimizationPolicy.getScore(getSamplesWithoutReporing("b", root, WorkingSetMode.Disabled))
            
            assertEquals(score, Some(1.minutes))
        }
    }
    
    @Test
    fun testWorkingSetWithChildrenDocument_ReturnsNone() {
        // a page with already reported page name;
        // and with another page to be downloaded in same working set
        val root = giveSamples("a", null, WorkingSetMode.Enabled)
        val child1 = giveSamples("b", root, WorkingSetMode.Disabled)
        
        
        
        runBlocking {
            val score = optimizationPolicy.getScore(getSamplesWithoutReporing("b", root, WorkingSetMode.Disabled))
            
            assertEquals(score, Some(2.minutes))
        }
    }
    
    fun getSamplesWithoutReporing(
        pageName: String,
        parent: RequestToken?,
        isWorkingSet: WorkingSetMode
    ): PreprocessedRequest<Request> {
        val prep = DocumentMockFactory.getRequest(URI("https://a.com"), RequestType.LINK, parent = parent)
            .upgrade()
            .upgradeAsDocument(pageName, isWorkingSet)
        
        runBlocking {
            optimizationPolicy.update(prep, desc.convertTo(prep))
        }
        
        return prep.upgradeAsRequestDoc().upgrade()
    }
    
    fun giveSamples(pageName: String, parent: RequestToken?, isWorkingSet: WorkingSetMode): RequestToken {
        val prep = DocumentMockFactory.getRequest(URI("https://a.com"), RequestType.LINK, parent = parent)
            .upgrade()
            .upgradeAsDocument(pageName, isWorkingSet)
        
        runBlocking {
            val timing = desc.convertTo(prep)
            
            optimizationPolicy.update(prep, desc.convertTo(prep))
            timingRepo.reportTiming(timing, 1.minutes)
        }
        
        return prep.request.token
    }
    
    fun giveSamplesAndRemoveDescriptor(
        pageName: String,
        parent: RequestToken?,
        isWorkingSet: WorkingSetMode
    ): RequestToken {
        val prep = DocumentMockFactory.getRequest(URI("https://a.com"), RequestType.LINK, parent = parent)
            .upgrade()
            .upgradeAsDocument(pageName, isWorkingSet)
        
        runBlocking {
            val timing = desc.convertTo(prep)
            
            optimizationPolicy.update(prep, desc.convertTo(prep))
            timingRepo.reportTiming(timing, 1.minutes)
            optimizationPolicy.removeDescriptor(prep.request.token, desc.convertTo(prep))
            optimizationPolicy.removeToken(prep.request.token)
        }
        
        return prep.request.token
    }
}