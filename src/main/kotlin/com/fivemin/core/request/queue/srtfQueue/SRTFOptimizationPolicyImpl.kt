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
import com.fivemin.core.engine.*
import com.fivemin.core.request.PreprocessedRequest
import com.fivemin.core.request.queue.DequeueOptimizationPolicy
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.URI
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.HashMap
import kotlin.concurrent.withLock
import kotlin.time.Duration

interface SRTFOptimizationPolicy : DequeueOptimizationPolicy {
    suspend fun update(req: PrepareTransaction<Request>, descriptor: SRTFPageDescriptor)
    
    suspend fun removeToken(token: RequestToken)
    
    suspend fun removeDescriptor(token: RequestToken, descriptor: SRTFPageDescriptor)
}

interface SRTFPageDescriptorFactory {
    fun convertTo(trans: PrepareTransaction<Request>): SRTFPageDescriptor
}

class SRTFPageDescriptorFactoryImpl : SRTFPageDescriptorFactory {
    override fun convertTo(trans: PrepareTransaction<Request>): SRTFPageDescriptor {
        return trans.ifDocument({
            SRTFPageDescriptor(it.parseOption.name)
        }, {
            SRTFPageDescriptor(getUriExtension(it.request.target))
        })
    }
    
    private fun getUriExtension(u: URI): String {
        val q = (u.query ?: "")
        
        if (!q.contains('.')) {
            return ""
        }
        
        return q.substring(q.lastIndexOf('.') + 1)
    }
}

class SRTFOptimizationPolicyImpl(private val timingRepository: SRTFTimingRepository) : SRTFOptimizationPolicy, SRTFKeyExtractor {
    private val wsMap = HashMap<RequestToken, WorkingSetList>()
    private val childMap = HashMap<RequestToken, RequestToken>() //key: parentToken, value: workingSet
    
    private val lock = Mutex()
    
    private suspend fun addAsWorkingSet_ReturnsWorkingSet(currentToken: RequestToken): Option<RequestToken> {
        childMap[currentToken] = currentToken
        wsMap[currentToken] = WorkingSetList()
        
        return Some(currentToken)
    }
    
    private suspend fun addParentMap_ReturnsWorkingSet(
        currentToken: RequestToken,
        parent: Option<RequestToken>
    ): Option<RequestToken> {
        return option {
            val ptt = parent.bind()
            val ws = childMap[ptt].toOption().bind()
            
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
    
    /**
     * Update SRTF Info.
     * If add page descriptor to workingset's remaining time.
     */
    override suspend fun update(req: PrepareTransaction<Request>, descriptor: SRTFPageDescriptor) {
        lock.withLock {
            val workingSet = req.ifDocumentAsync({
                if (it.containerOption.workingSetMode == WorkingSetMode.Enabled) {
                    addAsWorkingSet_ReturnsWorkingSet(req.request.token)
                }
        
                addParentMap_ReturnsWorkingSet(req.request.token, req.request.parent)
            }, {
                addParentMap_ReturnsWorkingSet(req.request.token, req.request.parent)
            })
            
            option {
                val ws = workingSet.bind()
                val wsList = wsMap[ws].toOption().bind()
                
                wsList.addPage(descriptor)
            }
        }
    }
    
    override suspend fun removeToken(token: RequestToken) {
        lock.withLock {
            wsMap.remove(token)
            childMap.remove(token) //the only case parent is finished working set. so it's stable.
        }
    }
    
    override suspend fun removeDescriptor(token: RequestToken, descriptor: SRTFPageDescriptor) {
        lock.withLock {
            option {
                val ws = childMap[token].toOption().bind()
                val pages = wsMap[ws].toOption().bind()
                
                pages
            }.map {
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
    
    override suspend fun extractWorkingSetKey(req: PreprocessedRequest<Request>): RequestToken {
        return lock.withLock {
            val ws = if (req.request.info.detachState == DetachableState.WANT)
                addAsWorkingSet_ReturnsWorkingSet(req.request.request.request.token)
            else addParentMap_ReturnsWorkingSet(req.request.request.request.token, req.request.request.request.parent)
            
            ws.fold({ req.request.request.request.token }, { it })
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
            val ret = pageMap[descriptor].toOption()
                .map { //On none() case, it can't be happened but leave it as is for failsafe.
                    it.updateAndGet {
                        Math.min(it - 1, 0)
                    }
                }
            
            ret.map {
                if (it == 0) {
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
