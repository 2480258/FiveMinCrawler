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

import kotlinx.coroutines.delay
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.lang.Math.abs
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class TestQueueItem constructor(val value: Int)

class RotatingQueueTest {
    lateinit var queue: RotatingQueueImpl<Int, Int, Int>
    
    @BeforeMethod
    fun setUp() {
        queue = RotatingQueueImpl()
    }
    
    fun assertQueueEmpty() {
        assertEquals(queue.queueSize, 0)
        assertEquals(queue.listSize, 0)
        assertEquals(queue.tableSize, 0)
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
    
    //@Test
    //fun testThrowsOnNotExistKeyUpdate() {
    //    assertThrows {
    //        queue.update(1, 1)
    //    }
    //}
    
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
        queue.enqueue(400, 4, 40)
        queue.enqueue(300, 3, 10)
        queue.enqueue(300, 3, 40)
        queue.enqueue(100, 1, 40)
        
        queue.update(300, 30)
        
        assertEquals(queue.dequeue(), 3)
        assertEquals(queue.dequeue(), 3)
    }
    
    @Test
    fun testPoll() {
        val t = Thread {
            assertEquals(queue.dequeue(), 1)
            assertQueueEmpty()
        }
        t.start()
        
        Thread.sleep(500)
        queue.enqueue(1, 1, 1)
        
        t.join()
    }
    
    @Test
    fun testCancel() {
        queue.enqueue(400, 4, 40)
        queue.enqueue(300, 3, 10)
        queue.enqueue(300, 3, 40)
        queue.enqueue(100, 1, 40)
        
        assertEquals(queue.removeKey(100), 1)
        assertEquals(queue.removeKey(300), 2)
        assertEquals(queue.removeKey(400), 1)
        
        assertQueueEmpty()
    }
    
    @Test
    fun testSameKeyValueUpdate() {
        queue.enqueue(4, 4, 4)
        queue.enqueue(4, 4, 4)
        queue.update(4, 4)
        
        assertEquals(queue.dequeue(), 4)
        assertEquals(queue.dequeue(), 4)
        
        assertQueueEmpty()
    }
    
    @Test
    fun testMultiThreaded() {
        val itemQueue = RotatingQueueImpl<Int, Int, TestQueueItem>()
        
        val enqueued = AtomicInteger()
        val dequeued = AtomicInteger()
        val threads = ConcurrentHashMap<Thread, Any>()
        val samples = 1000000
        val randoms = Random(1)
        
        for (i in 0 until 1000) {
            val t = Thread {
                try {
                    for (j in 0 until samples / 1000) {
                        val ret = itemQueue.dequeue()
                        dequeued.incrementAndGet()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            t.start()
            threads[t] = ""
        }
        
        for (i in 0 until 1000) {
            val t = Thread {
                for (j in 0 until samples / 1000) {
                    try {
                        itemQueue.enqueue(
                            i * j, TestQueueItem((abs(randoms.nextInt()) % 5000)), (abs(randoms.nextInt()) % 5000)
                        )
                        itemQueue.update(i * j, randoms.nextInt())
                        enqueued.incrementAndGet()
                        if(randoms.nextInt() % 2 == 0) {
                            val removedCount = queue.removeKey(i * j)
                            dequeued.addAndGet(removedCount)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            t.start()
            threads[t] = ""
        }
        
        for (th in threads) {
            th.key.join()
        }
        
        assertEquals(enqueued.toInt(), samples)
        assertEquals(dequeued.toInt(), samples)
        assertQueueEmpty()
    }
}