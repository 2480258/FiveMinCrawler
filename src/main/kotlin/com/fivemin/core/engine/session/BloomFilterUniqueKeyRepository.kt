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

package com.fivemin.core.engine.session

import arrow.core.Option
import arrow.core.toOption
import com.fivemin.core.DuplicateKeyException
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write


class UniqueKeyTokenFactory {
    fun create(key: UniqueKey): UniqueKeyToken {
        return UniqueKeyToken(key.hashCode())
    }
}

class KeyNotFoundException() : Exception() {

}

class TemporaryUniqueKeyRepository {
    private val hashMap = ConcurrentHashMap<UniqueKeyToken, UniqueKey>()
    private val uniqueKeyTokenFactory = UniqueKeyTokenFactory()
    
    fun addUniqueKey(key: UniqueKey): UniqueKeyToken {
        val token = uniqueKeyTokenFactory.create(key)
        hashMap[token] = key
        
        return token
    }
    
    fun deleteUniqueKey(token: UniqueKeyToken): Option<UniqueKey> {
        return hashMap.remove(token).toOption()
    }
    
    fun contains(token: UniqueKeyToken): Boolean {
        return hashMap.contains(token)
    }
    
    fun contains(key: UniqueKey): Boolean {
        return hashMap.containsValue(key)
    }
}

class BloomFilterUniqueKeyRepository constructor(
    factory: BloomFilterFactory,
    serialized: Option<SerializableAMQ>
) : UniqueKeyRepository, SessionRepository, FinishObserver, DetachObserver {
    private val notDetachableFilter: SerializableAMQ
    private val detachableFilter: SerializableAMQ
    private val temporaryUniqueKeyRepository = TemporaryUniqueKeyRepository()
    
    private val uniqueKeyTokenFactory = UniqueKeyTokenFactory()
    
    private val pageCount = AtomicInteger(0)
    private val finish: CountDownLatch = CountDownLatch(1)
    
    private val rwLock = ReentrantReadWriteLock()
    
    companion object {
        private val logger = LoggerController.getLogger("BloomFilterUniqueKeyRepository")
    }
    
    init {
        detachableFilter = serialized.fold({ factory.createEmpty() }, {
            it
        })
        
        notDetachableFilter = detachableFilter.copy()
    }
    
    override fun onStart() {
        pageCount.incrementAndGet()
    }
    
    override fun onFinish(token: SessionToken) {
        decrementAndFinishIfZero()
    }
    
    private fun decrementAndFinishIfZero() {
        val cur = pageCount.decrementAndGet()
        
        if (cur < 0) {
            throw IllegalAccessException()
        } else if (cur == 0) {
            finish.countDown()
        }
    }
    
    override fun waitFinish() {
        finish.await()
    }
    
    override fun create(parent: Option<SessionToken>): SessionInfo {
        return SessionInfo(this, this)
    }
    
    override fun addUniqueKeyWithDetachableThrows(key: UniqueKey): UniqueKeyToken {
        val token = uniqueKeyTokenFactory.create(key)
        if(notDetachableFilter.put(key)) {
            if(!detachableFilter.put(key)) { //BloomFilter.put() works atomically, so in this line it is guaranteed that this is not a duplicated key.
                throw DuplicateKeyException()
            }
        } else {
            throw DuplicateKeyException()
        }
        
        logger.debug("$key < $token < added uniquekey with detachable")
        
        return token
    }
    
    override fun addUniqueKeyWithNotDetachableThrows(key: UniqueKey): UniqueKeyToken {
        val token = uniqueKeyTokenFactory.create(key)
        if(!notDetachableFilter.put(key)) {
            throw DuplicateKeyException()
        }
    
        logger.debug("$key < $token < added uniquekey with not detachable")
        
        return token
    }
    
    override fun addUniqueKey(key: UniqueKey): UniqueKeyToken {
        val token = uniqueKeyTokenFactory.create(key)
        if(notDetachableFilter.put(key)) {
            //BloomFilter.put() works atomically, so in this line it is guaranteed that this is not a duplicated key.
            temporaryUniqueKeyRepository.addUniqueKey(key) //thread-safe
        } else {
            throw DuplicateKeyException()
        }
    
        logger.debug("$key < $token < added uniquekey with temparatory")
    
        return token
    }
    
    /**
     * Not Thread-Safe. Call it only if crawling is finished.
     */
    override fun export(): SerializableAMQ {
        return detachableFilter
    }
    
    private fun conveyToDetachable(token: UniqueKeyToken) {
        val key = temporaryUniqueKeyRepository.deleteUniqueKey(token) //thread-safe
        key.map { //no race condition with duplicated key; already filtered
            if(!detachableFilter.put(it)) { //should be not happen except false positive.
                throw DuplicateKeyException()
            }
        }
        
        logger.debug("$token < converys to detachable")
    }
    
    
    private fun conveyToNotDetachable(token: UniqueKeyToken) {
        temporaryUniqueKeyRepository.deleteUniqueKey(token) //thread-safe
        logger.debug("$token < converys to not detachable")
    }
    
    override fun notifyMarkedDetachable(tokens: Iterable<UniqueKeyToken>) {
        tokens.forEach {
            conveyToDetachable(it)
        }
    }
    
    override fun notifyMarkedNotDetachable(tokens: Iterable<UniqueKeyToken>) {
        tokens.forEach {
            conveyToNotDetachable(it)
        }
    }
}