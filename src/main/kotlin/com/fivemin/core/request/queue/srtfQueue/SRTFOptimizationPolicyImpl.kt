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

import arrow.core.Option
import arrow.core.Some
import arrow.core.computations.option
import arrow.core.toOption
import com.fivemin.core.engine.DetachableState
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.RequestToken
import com.fivemin.core.request.PreprocessedRequest
import com.fivemin.core.request.queue.DequeueOptimizationPolicy
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.HashMap
import kotlin.concurrent.withLock
import kotlin.time.Duration

interface SRTFOptimizationPolicy: DequeueOptimizationPolicy {
    suspend fun update(token: RequestToken, descriptor: SRTFPageDescriptor)
    
    suspend fun removeToken(token: RequestToken)
    
    suspend fun removeDescriptor(token: RequestToken, descriptor: SRTFPageDescriptor)
}

class SRTFOptimizationPolicyImpl(private val timingRepository: SRTFTimingRepository) : SRTFOptimizationPolicy {
    private val wsMap = HashMap<RequestToken, WorkingSetList>()
    private val parentMap = HashMap<RequestToken, TreeSet<RequestToken>>() //RequestToken can be ordered.
    private val childMap = HashMap<RequestToken, RequestToken>()
    
    private val lock = Mutex()
    
    private suspend fun addAsWorkingSet_ReturnsWorkingSet(currentToken: RequestToken): Option<RequestToken> {
        parentMap[currentToken] = TreeSet()
        
        return Some(currentToken)
    }
    
    private suspend fun addParentMap_ReturnsWorkingSet(currentToken: RequestToken, parent: Option<RequestToken>): Option<RequestToken> {
        return option {
            val ptt = parent.bind()
            val ws = childMap[ptt].toOption().bind()
            val pt = parentMap[ws].toOption().bind()
            
            pt.add(currentToken)
            childMap[currentToken] = ws
            
            ws
        }
    }
    
    private suspend fun calculate(workingSet: Option<RequestToken>): Option<Duration> {
        return option {
            val ws = workingSet.bind()
            val list = wsMap[ws].toOption().bind()
            
            list.calculateWith(timingRepository)
        }
    }
    
    override suspend fun update(token: RequestToken, descriptor: SRTFPageDescriptor) {
        lock.withLock {
            wsMap[token].toOption().map {
                it.addPage(descriptor)
            }
        }
    }
    
    override suspend fun removeToken(token: RequestToken) {
        lock.withLock {
            wsMap.remove(token)
            childMap[token].toOption().map { ws ->
                parentMap[ws].toOption().map {
                    it.remove(token)
                }
            }
            
            childMap.remove(token)
        }
    }
    
    override suspend fun removeDescriptor(token: RequestToken, descriptor: SRTFPageDescriptor) {
        lock.withLock {
            wsMap.remove(token).toOption().map {
                it.substractPage(descriptor)
            }
        }
    }
    
    override suspend fun getScore(req: PreprocessedRequest<Request>): Option<Duration> {
        return lock.withLock {
            val ws = if (req.request.info.detachState == DetachableState.WANT)
                addAsWorkingSet_ReturnsWorkingSet(req.request.request.request.token)
            else addParentMap_ReturnsWorkingSet(req.request.request.request.token, req.request.request.request.parent)
    
            calculate(ws)
        }
    }
    
    
}

class WorkingSetList {
    private val pageMap = HashMap<SRTFPageDescriptor, AtomicInteger>()
    private val lock = ReentrantLock()
    
    /**
     * Add page to map. thread safe.
     */
    fun addPage(descriptor: SRTFPageDescriptor) {
        lock.withLock {
            pageMap.getOrPut(descriptor) { AtomicInteger() }.incrementAndGet()
        }
    }
    
    /**
     * Add page to map. thread safe.
     */
    fun substractPage(descriptor: SRTFPageDescriptor) {
        lock.withLock {
            val ret = pageMap[descriptor].toOption().map { //On none() case, it can't be happened but leave it as is for failsafe.
                it.updateAndGet {
                    Math.min(it - 1, 0)
                }
            }
            
            ret.map {
                if(it == 0) {
                    pageMap.remove(descriptor)
                }
            }
        }
    }
    
    fun calculateWith(timingRepository: SRTFTimingRepository): Duration {
        return lock.withLock {
            pageMap.asIterable().fold(Duration.ZERO) { r, it ->
                r + timingRepository.getTiming(it.key) * it.value.toInt()
            }
        }
    }
}
