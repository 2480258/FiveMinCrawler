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
import com.fivemin.core.logger.Log
import com.fivemin.core.logger.LogLevel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock

class BloomFilterCache(private val factory: BloomFilterFactory) {
    val bloomFilter = factory.createEmpty()
    
    fun put(key: UniqueKey): Boolean {
        return bloomFilter.put(key)
    }
}

class SessionRepositoryImpl(private val detachObserver: DetachObserver, private val finishObserver: FinishObserver) :
    SessionRepository {
    override fun create(parent: Option<SessionToken>): SessionInfo {
        return SessionInfo(finishObserver, detachObserver)
    }
}

class CompositeUniqueKeyRepository(
    
    private val persister: UniqueKeyPersister,
    private val cache: BloomFilterCache,
    private val temporaryUniqueKeyRepository: TemporaryUniqueKeyRepository,
    private val uniqueKeyTokenFactory: UniqueKeyTokenFactory
) :
    UniqueKeyRepository, DetachObserver {
    
    companion object {
        private val logger = LoggerController.getLogger("CompositeUniqueKeyRepository")
    }
    
    @Log(
        beforeLogLevel = LogLevel.INFO,
        afterReturningLogLevel = LogLevel.DEBUG,
        afterThrowingLogLevel = LogLevel.ERROR,
        beforeMessage = "adding uniquekey",
        afterThrowingMessage = "failed to add uniquekey (detachable)"
    )
    override fun addUniqueKeyWithDetachableThrows(key: UniqueKey): UniqueKeyToken {
        if (!cache.put(key)) { // insertion failed -> already has the key
            // This process is atomic so no same key can go below
            throw DuplicateKeyException()
        }
        
        // insertion succeed -> key is now exists in cache
        
        if (!persister.persistKey(key)) { // insertion failed -> already has the key
            // This process is atomic so no same key can go below
            throw DuplicateKeyException()
        }
        
        // insertion succeed -> key is now exists in DB
        
        //now OK
        val token = uniqueKeyTokenFactory.create(key)
        
        return token
    }
    
    @Log(
        beforeLogLevel = LogLevel.INFO,
        afterReturningLogLevel = LogLevel.DEBUG,
        afterThrowingLogLevel = LogLevel.ERROR,
        beforeMessage = "adding uniquekey",
        afterThrowingMessage = "failed to add uniquekey (not detachable)"
    )
    override fun addUniqueKeyWithNotDetachableThrows(key: UniqueKey): UniqueKeyToken {
        if (!cache.put(key)) { // insertion failed -> already has the key
            // This process is atomic so no same key can go below
            throw DuplicateKeyException()
        }
        
        // insertion succeed -> key is now exists in cache
        
        if (persister.contains(key)) { // check failed -> already has the key
            throw DuplicateKeyException()
        }
        
        //now OK
        val token = uniqueKeyTokenFactory.create(key)
        
        return token
    }
    
    @Log(
        beforeLogLevel = LogLevel.INFO,
        afterReturningLogLevel = LogLevel.DEBUG,
        afterThrowingLogLevel = LogLevel.ERROR,
        beforeMessage = "adding uniquekey",
        afterThrowingMessage = "failed to add uniquekey (unknown)"
    )
    override fun addUniqueKey(key: UniqueKey): UniqueKeyToken {
        if (!cache.put(key)) { // insertion failed -> already has the key
            // This process is atomic so no same key can go below
            throw DuplicateKeyException()
        }
        
        // insertion succeed -> key is now exists in cache
        
        if (persister.contains(key)) { // check failed -> already has the key
            throw DuplicateKeyException()
        }
        
        //now OK
        val token = uniqueKeyTokenFactory.create(key)
        temporaryUniqueKeyRepository.addUniqueKey(key) //thread-safe, idempotent
        
        return token
    }
    
    @Log(
        beforeLogLevel = LogLevel.INFO,
        afterReturningLogLevel = LogLevel.DEBUG,
        afterThrowingLogLevel = LogLevel.ERROR,
        beforeMessage = "finalize uniquekey",
        afterThrowingMessage = "failed to convey uniquekey (unknown)"
    )
    override fun finalizeUniqueKey(key: UniqueKey) {
        persister.finalizeKey(key)
    }
    
    @Log(
        beforeLogLevel = LogLevel.INFO,
        afterReturningLogLevel = LogLevel.DEBUG,
        afterThrowingLogLevel = LogLevel.ERROR,
        beforeMessage = "marked detachable",
        afterThrowingMessage = "failed to mark detachable"
    )
    override fun notifyMarkedDetachable(tokens: Iterable<UniqueKeyToken>) {
        tokens.forEach {
            conveyToDetachable(it)
        }
    }
    
    @Log(
        beforeLogLevel = LogLevel.INFO,
        afterReturningLogLevel = LogLevel.DEBUG,
        afterThrowingLogLevel = LogLevel.ERROR,
        beforeMessage = "marked not detachable",
        afterThrowingMessage = "failed to mark not detachable"
    )
    override fun notifyMarkedNotDetachable(tokens: Iterable<UniqueKeyToken>) {
        tokens.forEach {
            conveyToNotDetachable(it)
        }
    }
    
    private fun conveyToDetachable(token: UniqueKeyToken) {
        val key = temporaryUniqueKeyRepository.deleteUniqueKey(token) //thread-safe
        key.map { //no race condition with duplicated key; already filtered
            if (!persister.persistKey(it)) {
                throw DuplicateKeyException()
            }
        }
    }
    
    
    private fun conveyToNotDetachable(token: UniqueKeyToken) {
        temporaryUniqueKeyRepository.deleteUniqueKey(token) //thread-safe
    }
    
    /**
     * Test purpose.
     */
    fun containsDetachable(key: UniqueKey): Boolean {
        return persister.contains(key)
    }
    
    /**
     * Test purpose.
     */
    fun containsNotDetachableAndAdd(key: UniqueKey): Boolean {
        return !cache.put(key)
    }
    
    /**
     * Test purpose.
     */
    fun isTempStorageEmpty(): Boolean {
        return temporaryUniqueKeyRepository.size == 0
    }
}

class UniqueKeyTokenFactory {
    fun create(key: UniqueKey): UniqueKeyToken {
        return UniqueKeyToken(key.longHashCode())
    }
}

class KeyNotFoundException() : Exception() {

}

class TemporaryUniqueKeyRepository {
    private val hashMap = ConcurrentHashMap<ULong, UniqueKey>()
    private val uniqueKeyTokenFactory = UniqueKeyTokenFactory()
    
    val size: Int
        get() {
            return hashMap.size
        }
    
    fun addUniqueKey(key: UniqueKey): UniqueKeyToken {
        val token = uniqueKeyTokenFactory.create(key)
        hashMap[token.tokenNumber] = key
        
        return token
    }
    
    fun deleteUniqueKey(token: UniqueKeyToken): Option<UniqueKey> {
        return hashMap.remove(token.tokenNumber).toOption()
    }
}

class FinishObserverImpl : FinishObserver {
    private val pageCount = AtomicInteger(0)
    private val finish: CountDownLatch = CountDownLatch(1)
    
    
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
}
