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

import org.testng.annotations.Test

import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
class SRTFTimingRepositoryImplTest {
    
    var repo = SRTFTimingRepositoryImpl()
    val des = "test".map {
        SRTFPageDescriptor(it.toString())
    }
    
    @BeforeMethod
    fun setUp() {
        repo = SRTFTimingRepositoryImpl()
    }
    
    @Test
    fun testGetTiming() {
        repo.reportTiming(des[0], Duration.Companion.milliseconds(10))
        repo.reportTiming(des[0], Duration.Companion.milliseconds(20))
        repo.reportTiming(des[0], Duration.Companion.milliseconds(30))
    
        assertEquals(20, repo.getTiming(des[0]).inWholeMilliseconds)
    }
    
    @Test
    fun testGetTimingZero() {
        assertEquals(0, repo.getTiming(des[0]).inWholeMilliseconds)
    }
}