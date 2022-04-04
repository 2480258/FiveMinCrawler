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

import arrow.core.firstOrNone
import arrow.core.flatten
import arrow.core.singleOrNone
import arrow.core.toOption
import com.fivemin.core.DuplicateKeyException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.withLock
import kotlin.concurrent.write

interface RotatingQueue<Score : Comparable<Score>, UniversalKey, Value> {
    fun dequeue(): Value
    
    fun enqueue(key: UniversalKey, value: Value, score: Score)
    
    fun update(originalKey: UniversalKey, score: Score)
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

class RotatingQueueImpl<Score : Comparable<Score>, UniversalKey, Value> :
    RotatingQueue<Score, UniversalKey, Value> {
    
    
    private val table = ConcurrentHashMap<UniversalKey, Score>()
    private val data =
        Collections.synchronizedSortedMap(TreeMap<Score, LinkedList<RotatingQueueNode<UniversalKey, Value>>>())
    
    private val lock = ReentrantLock()
    private val condition: Condition = lock.newCondition()
    
    /**
     * Test purpose.
     */
    public val queueSize: Int get() = lock.withLock { data.size }
    
    /**
     * Test purpose.
     */
    public val listSize: Int
        get() = lock.withLock {
            data.map {
                it.value.map {
                    it.size
                }
            }.flatten().sum()
        }
    
    /**
     * Test purpose.
     */
    public val tableSize: Int get() = lock.withLock { table.size }
    
    
    override fun dequeue(): Value {
        try {
            lock.lock()
            return dequeue_internal()
        } finally {
            lock.unlock()
        }
    }
    
    private fun waitFirstQueue() {
        while (data.isEmpty()) {
            condition.await()
        }
    }
    
    private fun releaseWait() {
        condition.signalAll()
    }
    
    private fun get_queue(): RotatingQueueNode<UniversalKey, Value> {
        if (data.size == 0) {
            waitFirstQueue()
            get_queue()
        }
        
        return data[data.firstKey()].toOption().fold({
            waitFirstQueue()
            get_queue()
        }, {
            it.first()
        })
    }
    
    private fun dequeue_internal(): Value {
        val firstQueue = get_queue()
        return elem_from_queue(firstQueue)
    }
    
    private fun elem_from_queue(firstQueue: RotatingQueueNode<UniversalKey, Value>): Value {
        return firstQueue.poll().toOption().fold({
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
        if (data.size == 0) return
        if (isFirstQueueEmpty()) return
        
        val fKey = data.firstKey()
        val listElem = data[fKey]!!
        if (listElem.first().size == 0) {
            val removed = listElem.removeFirst().key!!
            table.remove(removed)
        }
        
        if (listElem.size == 0) {
            data.remove(fKey)!!
        }
    }
    
    /**
     * Check for replacement. Read lock required for queue.
     */
    private fun isFirstQueueEmpty(): Boolean {
        if (data.size == 0) return false
        
        val firstQueue = data[data.firstKey()] ?: return true
        synchronized(firstQueue) {
            firstQueue.firstOrNone().fold({ return true }) {
                if (it.size != 0) {
                    return true
                }
            }
        }
        return false
    }
    
    override fun enqueue(key: UniversalKey, value: Value, score: Score) {
        try {
            lock.lock()
            enqueue_internal(key, value, score)
        } finally {
            lock.unlock()
        }
    }
    
    private fun enqueue_internal(key: UniversalKey, value: Value, score: Score) {
        //if (table.containsKey(key)) throw DuplicateKeyException()
        
        val dataList = ensureKeyExists(key, score)
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
    
    private fun ensureKeyExists(key: UniversalKey, score: Score): LinkedList<RotatingQueueNode<UniversalKey, Value>> {
        val tableKey = table.getOrPut(key) { score }!! //why is this nullable?
        
        return data.getOrPut(tableKey) { LinkedList() }!!
    }
    
    
    override fun update(originalKey: UniversalKey, score: Score) {
        try {
            lock.lock()
            update_internal(originalKey, score)
        } finally {
            lock.unlock()
        }
    }
    
    private fun update_internal(originalKey: UniversalKey, score: Score) {
        val tableKey = table.remove(originalKey!!)!! //why is this nullable?
        val source = data[tableKey]!!
        
        if (tableKey == score) return
        
        val wantToMove = source.single {
            it.key == originalKey
        }
        
        source.removeIf {
            it.key == originalKey
        }
        
        if (source.isEmpty()) {
            data.remove(tableKey)
        }
        
        val destList = ensureKeyExists(originalKey, score)
        destList.add(wantToMove)
        
        releaseWait()
    }
}