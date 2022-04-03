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

import io.mockk.every
import io.mockk.mockk
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import org.testng.Assert.*

class RotatingQueueNodeTest {
    lateinit var queue : RotatingQueue<Int, Int, Int>
    
    @BeforeMethod
    fun setUp() {
        val calculateScore: CalculateScore<Int, Int> = mockk()
        
        every {
            calculateScore.getScore(any())
        }.answers {
            firstArg()
        }
        
        queue = RotatingQueueImpl(calculateScore)
    }
    
    @Test
    fun testOffer() {
        queue.enqueue(1, 1)
        queue.dequeue()
    }
    
    @Test
    fun testPoll() {
    
    }
}