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
import com.fivemin.core.engine.*
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.withLock
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
    serialized: Option<SerializedBloomFilter>
) : UniqueKeyRepository, SessionRepository, FinishObserver, DetachObserver {
    private val notDetachableFilter: BloomFilter
    private val detachableFilter: BloomFilter
    private val temporaryUniqueKeyRepository = TemporaryUniqueKeyRepository()
    
    private val uniqueKeyTokenFactory = UniqueKeyTokenFactory()
    
    private val pageCount = AtomicInteger(0)
    private val finish: CountDownLatch = CountDownLatch(1)
    
    private val rwLock = ReentrantReadWriteLock()
    
    init {
        notDetachableFilter = factory.createEmpty()
        detachableFilter = serialized.fold({ factory.createEmpty() }, {
            it.create()
        })
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
        return SessionInfo(this, parent, this)
    }
    
    override fun addUniqueKeyWithDetachableThrows(key: UniqueKey): UniqueKeyToken {
        return addTo(detachableFilter, key)
    }
    
    override fun addUniqueKeyWithNotDetachableThrows(key: UniqueKey): UniqueKeyToken {
        return addTo(notDetachableFilter, key)
    }
    
    override fun addUniqueKey(key: UniqueKey): UniqueKeyToken {
        return rwLock.read {
            checkDuplicated(key)
            
            rwLock.write {
                checkDuplicated(key)
                temporaryUniqueKeyRepository.addUniqueKey(key)
            }
        }
    }
    
    private fun conveyToDetachable(token: UniqueKeyToken) {
        conveyTo(detachableFilter, token)
    }
    
    
    private fun conveyToNotDetachable(token: UniqueKeyToken) {
        conveyTo(notDetachableFilter, token)
    }
    
    private fun addTo(bf: BloomFilter, key: UniqueKey): UniqueKeyToken {
        val token = uniqueKeyTokenFactory.create(key)
    
        return rwLock.read {
            checkDuplicated(key)
        
            rwLock.write {
                checkDuplicated(key) //https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.concurrent/java.util.concurrent.locks.-reentrant-read-write-lock/write.html
            
                if (!bf.put(key)) { //False means key is already added
                    throw DuplicateKeyException()
                }
            
                token
            }
        }
    }
    
    private fun conveyTo(bf: BloomFilter, token: UniqueKeyToken) {
        rwLock.write {
            val key = temporaryUniqueKeyRepository.deleteUniqueKey(token)
            key.fold({ throw KeyNotFoundException() }, {
                if (!bf.put(it)) {
                    throw DuplicateKeyException() //if thrown, it may be indicate inconsistancy error; TODO: Notify users that.
                }
            })
        }
    }
    
    private fun checkDuplicated(key: UniqueKey) {
        rwLock.read {
            if(!detachableFilter.mightContains(key) || notDetachableFilter.mightContains(key) || temporaryUniqueKeyRepository.contains(key)) {
                throw DuplicateKeyException()
            }
        }
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