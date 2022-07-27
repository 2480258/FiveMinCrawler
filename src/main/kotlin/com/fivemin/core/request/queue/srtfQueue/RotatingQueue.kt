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

import arrow.core.flatten
import arrow.core.toOption
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

interface RotatingQueue<Score : Comparable<Score>, UniversalKey, Value> {
    fun dequeue(): Value
    
    fun enqueue(key: UniversalKey, value: Value, score: Score)
    
    fun update(originalKey: UniversalKey, score: Score)
}


class RotatingQueueNode<UniversalKey, Value, Score : Comparable<Score>> constructor(val key: UniversalKey) {
    
    private val queue = ConcurrentSkipListMap<Score, Value>()
    
    val size: Int
        get() {
            return queue.size
        }
    
    fun offer(value: Value, score: Score) {
        queue[score] = value
    }
    
    fun poll(): Value? {
        return queue.pollFirstEntry()?.value
    }
}
//https://www.boost.org/sgi/stl/StrictWeakOrdering.html

class RotatingQueueImpl<Score : Comparable<Score>, UniversalKey, Value> : RotatingQueue<Score, UniversalKey, Value> {
    
    
    private val table = ConcurrentHashMap<UniversalKey, Score>()
    private val data =
        Collections.synchronizedSortedMap(TreeMap<Score, TreeMap<UniversalKey, RotatingQueueNode<UniversalKey, Value, Score>>>())
    
    private val lock = ReentrantLock()
    private val condition: Condition = lock.newCondition()
    
    /**
     * Test purpose.
     */
    val queueSize: Int get() = lock.withLock { data.size }
    
    /**
     * Test purpose.
     */
    val listSize: Int
        get() = lock.withLock {
            data.map {
                it.value.map {
                    it.value.size
                }
            }.flatten().sum()
        }
    
    /**
     * Test purpose.
     */
    val tableSize: Int get() = lock.withLock { table.size }
    
    
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
    
    private fun get_queue(): RotatingQueueNode<UniversalKey, Value, Score> {
        if (data.size == 0) {
            waitFirstQueue()
            get_queue()
        }
        
        return data[data.firstKey()].toOption().fold({
            waitFirstQueue()
            get_queue()
        }, {
            it.firstEntry().value
        })
    }
    
    private fun dequeue_internal(): Value {
        val firstQueue = get_queue()
        return elem_from_queue(firstQueue)
    }
    
    private fun elem_from_queue(firstQueue: RotatingQueueNode<UniversalKey, Value, Score>): Value {
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
        if (listElem.firstEntry().value.size == 0) {
            val removed = listElem.remove(listElem.firstKey())!!.key!!
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
            if (firstQueue.size == 0) return true
            if (firstQueue.firstEntry().value.size != 0) return true
        }
        return false
    }
    
    /**
     * Enqueues data
     * Key: specific hashcode for value(s)(for update)
     * Score: select order to dequeue. if same universalkey already inserted, uses previously given score.
     * Value: what to dequeue.
     */
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
        val dataElem = dataList[key].toOption().fold({
            val ret = RotatingQueueNode<UniversalKey, Value, Score>(key)
            dataList[key] = ret
            
            ret
        }) {
            it
        }
        dataElem.offer(value, score)
        
        releaseWait()
        
    }
    
    private fun ensureKeyExists(
        key: UniversalKey, score: Score
    ): TreeMap<UniversalKey, RotatingQueueNode<UniversalKey, Value, Score>> {
        val tableKey = table.getOrPut(key) { score }!! //why is this nullable?
        
        return data.getOrPut(tableKey) { TreeMap() }!!
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
        
        val wantToMove = source[originalKey]!!
        
        source.remove(originalKey)
        
        if (source.isEmpty()) {
            data.remove(tableKey)
        }
        
        val destList = ensureKeyExists(originalKey, score)
        destList[originalKey] = wantToMove
        
        releaseWait()
    }
}