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

class RotatingQueueNodeTest {
    lateinit var node : RotatingQueueNode<Int, Int, Int>
    
    @BeforeMethod
    fun setUp() {
        node = RotatingQueueNode(-1)
    }
    
    @Test
    fun testOffer() {
        node.offer(0, 0)
        node.offer(0, 1)
        node.offer(1, 1)
    
        assertEquals(node.size, 3)
    }
    
    @Test
    fun testPoll() {
        node.offer(-1, 1)
        node.offer(-3, 3)
        node.offer(-2, 2)
    
        assertEquals(node.poll(), -1)
        assertEquals(node.poll(), -2)
        assertEquals(node.poll(), -3)
        
        assertEquals(node.size, 0)
        assert(node.__test_assert_not_contains_list())
    }
    
    @Test
    fun testRemoveAll() {
        node.offer(0, 0)
        node.offer(0, 1)
        node.offer(1, 1)
        node.offer(-1, 1)
        node.offer(-3, 3)
        node.offer(-2, 2)
    
        assertEquals(node.size, 6)
        
        node.removeAll()
    
        assertEquals(node.size, 0)
        assert(node.__test_assert_not_contains_list())
    }
    
    @Test
    fun testPollWhenEmpty() {
        assertEquals(node.poll(), null)
    }
}