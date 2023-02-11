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

import org.testng.annotations.Test

import org.testng.Assert.*

class BFSTest {
    class IntBFS(private val graph: List<List<Int>>) : BFS<Int, Int>() {
        override fun getKeysFromKey(key: Int): Iterable<Int> {
            return graph[key]
        }
    
        override fun equalsTarget(a: Int, b: Int): Boolean {
            return a == b
        }
    
        override fun convertToTarget(item: Int) = item
    
    }
    
    
    @Test
    fun testFindWithDefaultRoute() {
        val g = listOf(
            listOf(
                1
            ),
            listOf(
                2
            ),
            listOf(
                3
            ),
            listOf(
                4
            ),
            listOf(
            
            )
        )
        
        val b = IntBFS(g)
        val ret = b.find(0, 4)
        
        assertEquals(listOf(0, 1, 2, 3, 4), ret)
    }
    
    @Test
    fun testFindWithCycleRoute() {
        val g = listOf(
            listOf(
                0, 1
            ),
            listOf(
                0, 2
            ),
            listOf(
                0, 3
            ),
            listOf(
                0, 4
            ),
            listOf(
                0
            )
        )
        
        val b = IntBFS(g)
        val ret = b.find(0, 4)
        
        assertEquals(listOf(0, 1, 2, 3, 4), ret)
    }
    
    @Test
    fun testFindWithBFS() {
        val g = listOf(
            listOf(
                1, 2
            ),
            listOf(
                2
            ),
            listOf(
                2
            )
        )
        
        val b = IntBFS(g)
        val ret = b.find(0, 2)
        
        assertEquals(listOf(0, 2), ret)
    }
}