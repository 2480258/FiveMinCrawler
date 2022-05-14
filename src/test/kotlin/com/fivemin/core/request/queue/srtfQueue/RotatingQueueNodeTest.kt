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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import org.testng.Assert.*
import java.lang.Math.abs
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class RotatingQueueNodeTest {
    lateinit var queue: RotatingQueueImpl<Int, Int, Int>
    
    @BeforeMethod
    fun setUp() {
        queue = RotatingQueueImpl()
    }
    
    fun assertQueueEmpty() {
        assertEquals(queue.queueSize, 1)
        assertEquals(queue.listSize, 0)
        assertEquals(queue.tableSize, 1)
    }
    
    @Test
    fun testOffer() {
        queue.enqueue(1, 1, 1)
        val ret = queue.dequeue()
        
        assertEquals(ret, 1)
        
        assertQueueEmpty()
    }
    
    @Test
    fun testOrder() {
        queue.enqueue(4, 4, 4)
        queue.enqueue(3, 3, 3)
        queue.enqueue(2, 2, 2)
        queue.enqueue(1, 1, 1)
        
        assertEquals(queue.dequeue(), 1)
        assertEquals(queue.dequeue(), 2)
        assertEquals(queue.dequeue(), 3)
        assertEquals(queue.dequeue(), 4)
    
        assertQueueEmpty()
    }
    
    @Test
    fun testDuplicated() {
        queue.enqueue(41, 4, 4)
        queue.enqueue(31, 3, 3)
        queue.enqueue(22, 2, 2)
        queue.enqueue(21, 2, 2)
        queue.enqueue(11, 1, 1)
        
        assertEquals(queue.dequeue(), 1)
        assertEquals(queue.dequeue(), 2)
        assertEquals(queue.dequeue(), 2)
        assertEquals(queue.dequeue(), 3)
        assertEquals(queue.dequeue(), 4)
    
        assertQueueEmpty()
    }
    
    @Test
    fun testUpdate() {
        queue.enqueue(4, 4, 4)
        queue.enqueue(3, 3, 3)
        queue.enqueue(2, 2, 2)
        queue.enqueue(1, 1, 1)
        queue.update(1, 5)
        
        assertEquals(queue.dequeue(), 2)
        assertEquals(queue.dequeue(), 3)
        assertEquals(queue.dequeue(), 4)
        assertEquals(queue.dequeue(), 1)
    
        assertQueueEmpty()
    }
    
    @Test
    fun testSameKeyUpdate() {
        queue.enqueue(4, 4, 4)
        queue.enqueue(3, 3, 3)
        queue.enqueue(2, 2, 2)
        queue.enqueue(1, 1, 1)
        queue.update(1, 1)
        
        assertEquals(queue.dequeue(), 1)
        assertEquals(queue.dequeue(), 2)
        assertEquals(queue.dequeue(), 3)
        assertEquals(queue.dequeue(), 4)
    
        assertQueueEmpty()
    }
    
    @Test
    fun testThrowsOnNotExistKeyUpdate() {
        assertThrows {
            queue.update(1, 1)
        }
    }
    
    @Test
    fun testDuplicatedKey() {
        queue.enqueue(4, 4, 4)
        queue.enqueue(4, 3, 3)
    
        assertEquals(queue.dequeue(), 3)
        assertEquals(queue.dequeue(), 4)
        
        assertQueueEmpty()
    }
    
    @Test
    fun testDuplicationKeyUpdate() {
        queue.enqueue(4, 4, 4)
        queue.enqueue(3, 3, 1)
        queue.enqueue(3, 2, 4)
        queue.enqueue(1, 1, 4)
    
        queue.update(3, 3)
    
        assertEquals(queue.dequeue(), 2)
        assertEquals(queue.dequeue(), 3)
    }
    
    @Test
    fun testPoll() {
        runBlocking {
            GlobalScope.launch { assertEquals(queue.dequeue(), 1) }
            delay(500)
            queue.enqueue(1, 1, 1)
        }
    }
    
    
    @Test
    fun testMultiThreaded() {
        val count = AtomicInteger()
        val threads = ConcurrentHashMap<Thread, Any>()
        val samples = 1000000
        val randoms = Random(1)
        
        for (i in 0 until 10000) {
            val t = Thread {
                for (j in 0 until samples / 1000) {
                    val ret = queue.dequeue()
                    count.incrementAndGet()
                    
                    //println("get result: " + ret)
                }
            }
            t.start()
            threads[t] = ""
        }
        
        for (i in 0 until 10000) {
            val t = Thread {
                for (j in 0 until samples / 1000) {
                    try {
                        queue.enqueue((i * 1000) + j, (abs(randoms.nextInt()) % 5000), (abs(randoms.nextInt()) % 5000))
                        //println("enqueue finished: " + (i * 100) + j)
                        queue.update(
                            (i * 1000) + j,
                            (randoms.nextInt())
                        )
                        //println("update finished: " + (i * 100) + j)
                    } catch (e: Exception) {
                        //println(e.toString())
                    }
                }
            }
            t.start()
            threads[t] = ""
        }
        
        for (th in threads) {
            th.key.join()
        }
        
        assertEquals(count.toInt(), 10000000)
        
        assertQueueEmpty()
    }
}