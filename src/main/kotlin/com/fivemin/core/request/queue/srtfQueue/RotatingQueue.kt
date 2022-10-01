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
import arrow.core.getOrNone
import arrow.core.toOption
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

interface RotatingQueue<Score : Comparable<Score>, UniversalKey, Value> {
    fun dequeue(): Value
    
    fun enqueue(key: UniversalKey, value: Value, score: Score)
    
    fun update(originalKey: UniversalKey, score: Score)
    
    fun removeKey(key: UniversalKey): Int
}


class RotatingQueueNode<UniversalKey, Value, Score : Comparable<Score>> constructor(val key: UniversalKey) {
    
    private val queue = TreeMap<Score, LinkedList<Value>>() // Lower score is better.
    private val lock = ReentrantLock()
    
    val size: Int
        get() {
            lock.withLock {
                return queue.map {
                    it.value.size
                }.sum()
            }
        }
    
    fun offer(value: Value, score: Score) {
        lock.withLock {
            offer_interal(value, score)
        }
    }
    
    fun poll(): Value? {
        lock.withLock {
            return poll_internal()
        }
    }
    
    fun removeAll() {
        lock.withLock {
            queue.clear()
        }
    }
    
    private fun ensureKeyList(score: Score) {
        if (!queue.containsKey(score)) {
            queue[score] = LinkedList()
        }
    }
    
    private fun offer_interal(value: Value, score: Score) {
        ensureKeyList(score)
        queue[score]!!.add(value)
    }
    
    private fun poll_internal(): Value? {
        val entry = queue.firstEntry()
        try {
            return entry?.value?.removeFirstOrNull()
        } finally {
            if (entry != null) {
                if (!entry.value.any()) {
                    queue.remove(entry.key)
                }
            }
        }
    }
    
    fun __test_assert_not_contains_list(): Boolean {
        lock.withLock {
            return queue.size == 0
        }
    }
}

class RotatingQueueImpl<Score : Comparable<Score>, UniversalKey : Any, Value> :
    RotatingQueue<Score, UniversalKey, Value> {
    
    
    private val table = HashMap<UniversalKey, Score>()
    private val data =
        TreeMap<Score, TreeMap<UniversalKey, RotatingQueueNode<UniversalKey, Value, Score>>>()  // Lower score is better.
    
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
        lock.withLock {
            return dequeue_internal()
        }
    }
    
    private fun waitFirstQueue() {
        while (data.isEmpty()) {
            condition.await(100, TimeUnit.MILLISECONDS)
        }
    }
    
    private fun releaseWait() {
        condition.signalAll()
    }
    
    private fun get_First(): Triple<Score, UniversalKey, RotatingQueueNode<UniversalKey, Value, Score>> {
        if (data.size == 0) {
            waitFirstQueue()
            get_First()
        }
    
        val score = data.firstKey()
        
        return data[score].toOption().fold({
            waitFirstQueue()
            get_First()
        }, {
            val entry = it.firstEntry()
            Triple(score, entry.key, entry.value)
        })
    }
    
    private fun dequeue_internal(): Value {
        val firstQueue = get_First()
        return elem_from_queue(firstQueue)
    }
    
    private fun elem_from_queue(firstQueue: Triple<Score, UniversalKey, RotatingQueueNode<UniversalKey, Value, Score>>): Value {
        val result = firstQueue.third.poll().toOption().fold({
            replaceHead()
            dequeue_internal()
        }) {
            it
        }
        
        if(data[firstQueue.first]!![firstQueue.second]?.size == 0) {
            data[firstQueue.first]?.remove(firstQueue.second)
            table.remove(firstQueue.second)
        }
        
        if((data[firstQueue.first]?.isEmpty()) == true) {
            data.remove(firstQueue.first)
        }
        
        return result
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
        
        if (firstQueue.size == 0) return true
        if (firstQueue.firstEntry().value.size != 0) return true
        
        return false
    }
    
    override fun removeKey(key: UniversalKey): Int {
        lock.withLock {
            val keyScore = table.getOrNone(key)
            
            val result = keyScore.map {
                val keyTable = data[it]?.get(key)
                
                val removeCount = keyTable?.size
                keyTable?.removeAll()
                
                if ((data[it]?.get(key)?.size) == 0) { // Will always true but for safe....
                    data[it]?.remove(key)
                    table.remove(key)
                }
                
                if ((data[it]?.isEmpty()) == true) {
                    data.remove(it)
                }
                
                removeCount.toOption()
            }.flatten().fold({ 0 }, { it })
            
            return result
        }
    }
    
    /**
     * Enqueues data
     * Key: specific hashcode for value(s)(for update)
     * Score: select order to dequeue. if same universalkey already inserted, uses previously given score.
     * Value: what to dequeue.
     */
    override fun enqueue(key: UniversalKey, value: Value, score: Score) {
        lock.withLock {
            enqueue_internal(key, value, score)
            releaseWait()
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
        
        
    }
    
    private fun ensureKeyExists(
        key: UniversalKey, score: Score
    ): TreeMap<UniversalKey, RotatingQueueNode<UniversalKey, Value, Score>> {
        val tableKey = table.getOrPut(key) { score }!! //why is this nullable?
        
        return data.getOrPut(tableKey) { TreeMap() }!!
    }
    
    
    override fun update(originalKey: UniversalKey, score: Score) {
        lock.withLock {
            update_internal(originalKey, score)
            releaseWait()
        }
    }
    
    private fun update_internal(originalKey: UniversalKey, score: Score) {
        if (!table.containsKey(originalKey)) {
            return
        }
        
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
    }
}