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

import arrow.core.*
import com.fivemin.core.engine.SessionToken
import com.fivemin.core.engine.UniqueKey
import com.fivemin.core.engine.UniqueKeyRepository
import com.fivemin.core.exclusiveSingleOrNone
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Saves ArchivedSessionSet for conflict check.
 */
class UniqueKeyRepositoryImpl constructor(private val set: Option<ArchivedSessionSet>) : UniqueKeyRepository {

    private val dic: MutableMap<SessionToken, UniqueKeyOwnership> = mutableMapOf()
    private val list: MutableMap<UniqueKeyOwnership, MutableList<UniqueKeyState>> = mutableMapOf()
    private val lock: ReentrantLock = ReentrantLock()
    
    /**
     * Not Thread-Safe.
     * Find conflict with all sessions except token that contains ownership.
     * This behavior is used to allow retry, redirect requests for previously requested.
     *
     * @param ownership Ownership to except.
     * @param key key to find.
     */
    private fun findGlobalExceptSelf(
        ownership: UniqueKeyOwnership,
        key: UniqueKey
    ): Option<UniqueKeyState> {
        if (set.fold({ false }, { it.isConflict(key) }) //find key from given resume file.
        ) {
            throw UniqueKeyDuplicateException("Attempted to search with $key")
        }

        val lst = list.filter { //filters ownership key. TODO: performance optimization.
            it.key != ownership
        }

        val ret = lst.map { //finds conflicts from remain lists.
            findFromStateList(it.value, key)
        }

        return ret.filterOption().exclusiveSingleOrNone() //returns key. if key is duplicated, throws exception.
    }
    
    /**
     * Not Thread-Safe.
     * Find conflict with ownership session.
     * This behavior is used to control retry, redirect requests.
     *
     * @param self finds key from this ownership.
     * @param key key to find.
     */
    private fun findFromSelf(self: UniqueKeyOwnership, key: UniqueKey): Option<UniqueKeyState> {
        return list[self].toOption().map {
            it.exclusiveSingleOrNone { //filter and check this is exclusive.
                it.key == key
            }
        }.flatten()
    }
    
    /**
     * Not Thread-Safe.
     * Find conflict from states.
     * This function is used to control retry, redirect requests.
     *
     * @param states find from this list.
     * @param key key to find.
     */
    private fun findFromStateList(
        states: List<UniqueKeyState>,
        key: UniqueKey
    ): Option<UniqueKeyState> {
        return states.exclusiveSingleOrNone { it.key == key }
    }
    
    /**
     * Synchronized.
     * Adds or updates duplication count for handle about key.
     * Throws if key is duplicated.
     *
     * @param handle changed by given key
     * @param key key to change.
     */
    private fun addOrUpdateKey(handle: UniqueKeyOwnership, key: UniqueKey) {
        lock.withLock {
            // firstly, we check key is already added.
            
            val globalKeyChecks = findGlobalExceptSelf(handle, key)
            val isUnique = globalKeyChecks.isNotEmpty()

            if (isUnique) {
                throw UniqueKeyDuplicateException("Attempted to start task with duplicated key: $key")
            }
            
            
            // find unique key states.
            val stateLst = list[handle].toOption()

            if (stateLst.isEmpty()) {
                list[handle] = mutableListOf()
            }

            findFromSelf(handle, key).fold(
                { list[handle]!!.add(UniqueKeyState(key)) }, //add URL to this states. it means redirect.
                { it.increaseDuplicationCount() } //increase if duplicated (it means requests is doing retry)
            )
        }
    }
    
    /**
     * Synchronized.
     * converts SessionToken to ownership
     *
     */
    private fun getOwnership(token: SessionToken): UniqueKeyOwnership {
        return lock.withLock {
            return dic[token].toOption().fold( //if not exists, create one.
                {
                    var os = UniqueKeyOwnership.create()
                    dic[token] = os
                    os
                },
                { x -> x }
            )
        }
    }
    
    /**
     * Synchronized.
     * Adds or updates duplication count.
     * May throw when key is duplciated.
     *
     * @param token where key is added
     * @param key key to add
     */
    override fun addAlias(token: SessionToken, key: UniqueKey) {
        lock.withLock {
            var os = getOwnership(token)
            addOrUpdateKey(os, key)
        }
    }
    
    /**
     * Synchronized.
     * Transfers ownership to other token.
     * This function is for detached requests.
     */
    override fun transferOwnership(src: SessionToken, dest: SessionToken) {
        lock.withLock {
            var s = dic[src]!!
            dic[dest] = s
            dic.remove(src)
        }
    }
    
    /**
     * Synchronized.
     * Returns collected detachable tokens.
     */
    fun export(detachables: Iterable<SessionToken>): ArchivedSessionSet {
        lock.withLock {
            var lst = detachables.filter { x ->
                dic.contains(x)
            }

            return ArchivedSessionSet(
                lst.map {
                    ArchivedSession(list[dic[it]]!!.map { it.key })
                }
            )
        }
    }
    
    /**
     * Represents duplication counts for redirect/retry operations.
     */
    class UniqueKeyState constructor(val key: UniqueKey) {
        private val maxDuplication: Int = 3
        private var duplicateCount: Int = 1
    
        /**
         * Not Thread-safe.
         * increase duplication count.
         * if duplicateCount >= maxDuplication, then throws.
         */
        fun increaseDuplicationCount() {
            if (duplicateCount >= maxDuplication) {
                throw RetryCountMaxedException()
            }

            duplicateCount++
        }
    }
    
    /**
     * Represents ownership. for a token number.
     */
    class UniqueKeyOwnership private constructor(val tokenNumber: Int) {
        companion object {
            private var LastUsed: Int = -1
            private val lock = ReentrantLock()

            fun create(): UniqueKeyOwnership {
                lock.withLock {
                    LastUsed++
                    return UniqueKeyOwnership(LastUsed)
                }
            }
        }
    }
}

class RetryCountMaxedException : Exception()

class UniqueKeyDuplicateException(msg: String) : Exception(msg)
