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

import arrow.core.singleOrNone
import arrow.core.toOption
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.collections.LinkedHashMap
import kotlin.concurrent.read
import kotlin.concurrent.withLock
import kotlin.concurrent.write

interface RotatingQueue<Score : Comparable<Score>, UniversalKey, Value> {
    fun dequeue(): Value
    
    fun enqueue(key: UniversalKey, value: Value)
    
    fun update(originalKey: UniversalKey, newKey: Score)
}

interface CalculateScore<Score : Comparable<Score>, Value> {
    fun getScore(value: Value): Score
}

class RotatingQueueNode<UniversalKey, Value> constructor(val key: UniversalKey) {
    
    private val queue = PriorityBlockingQueue<Value>()
    
    val size: Int
        get() {
            return queue.size
        }
    
    fun offer(value: Value) {
        queue.offer(value)
    }
    
    fun poll(): Value? {
        //doesn't wait or block as passing by while(...) statement
        //https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/concurrent/PriorityBlockingQueue.java
        return queue.poll(0, TimeUnit.NANOSECONDS)
    }
}
//https://www.boost.org/sgi/stl/StrictWeakOrdering.html

class RotatingQueueImpl<Score : Comparable<Score>, UniversalKey, Value> constructor(val score: CalculateScore<Score, Value>) :
    RotatingQueue<Score, UniversalKey, Value> {
    
    
    private val table = ConcurrentHashMap<UniversalKey, Score>()
    private val data =
        Collections.synchronizedSortedMap(TreeMap<Score, LinkedList<RotatingQueueNode<UniversalKey, Value>>>())
    
    private val queueRwLock = ReentrantReadWriteLock()
    
    private val lock = ReentrantLock()
    private val condition: Condition = lock.newCondition()
    
    override fun dequeue(): Value {
        return dequeue_internal()
    }
    
    private fun waitFirstQueue() {
        condition.await()
    }
    
    private fun releaseWait() {
        synchronized(lock) {
            condition.signal()
        }
    }
    
    private fun get_queue(): RotatingQueueNode<UniversalKey, Value> {
        return queueRwLock.read {
            data[data.firstKey()]
        }.toOption().fold({
            waitFirstQueue()
            get_queue()
        }, {
            synchronized(it) {
                it.first()
            }
        })
    }
    
    private fun dequeue_internal(): Value {
        val firstQueue = get_queue()
        return elem_from_queue(firstQueue)
    }
    
    private fun elem_from_queue(firstQueue: RotatingQueueNode<UniversalKey, Value>): Value {
        return queueRwLock.read {
            firstQueue.poll()
        }.toOption().fold({
            replaceHead()
            dequeue_internal()
        }) {
            it
        }
    }
    
    /**
     * Moves first queue to cache if it is empty.
     * Nothing happens with non-empty queue
     */
    private fun replaceHead() {
        queueRwLock.read { //pre-checks without write lock.
            val firstQueue = data[data.firstKey()] ?: return
            if (firstQueue.isNotEmpty()) return
        }
        
        queueRwLock.write {
            val firstQueue = data[data.firstKey()] ?: return
            if (firstQueue.isEmpty()) {
                val fKey = data.firstKey()
                val listElem = data[fKey]!!
                
                synchronized(listElem) {
                    if (listElem.first().size == 0) {
                        listElem.removeFirst()
                    }
                    
                    if (listElem.size == 0) {
                        data.remove(fKey)!!
                    }
                }
            }
        }
    }
    
    override fun enqueue(key: UniversalKey, value: Value) {
        val sc = score.getScore(value)
    
        val dataList = queueRwLock.write {
            val tableKey = table.getOrPut(key) { sc }!! //why is this nullable?
            
            data.getOrPut(tableKey) { LinkedList() }!!
        }
        
        synchronized(dataList) {
            val dataElem = dataList.singleOrNone {
                it.key == key
            }.fold({
                val ret = RotatingQueueNode<UniversalKey, Value>(key)
                dataList.add(ret)
                
                ret
            }) {
                it
            }
            
            dataElem.offer(value)
            releaseWait()
        }
    }
    
    override fun update(originalKey: UniversalKey, newKey: Score) {
        val tableKey = table[originalKey]!!
        
        queueRwLock.write {
            val removed = data.remove(tableKey)
            data.put(newKey, removed)
        }
    }
}