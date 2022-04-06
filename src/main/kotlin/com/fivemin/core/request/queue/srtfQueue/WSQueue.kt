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

import arrow.core.*
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.RequestToken
import com.fivemin.core.request.*
import com.fivemin.core.request.queue.DequeueOptimizationPolicy
import com.fivemin.core.request.queue.EnqueuedRequest
import com.fivemin.core.request.queue.RequestDeniedException
import kotlinx.coroutines.runBlocking
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime


interface SRTFKeyExtractor {
    suspend fun extractWorkingSetKey(req: PreprocessedRequest<Request>): RequestToken
}

class WSQueue constructor(
    private val optimizationPolicy: SRTFOptimizationPolicy,
    private val srtfKeyExtractor: SRTFKeyExtractor,
    private val srtfPageFactory: SRTFPageDescriptorFactory,
    private val maxRequestThread: Int = 3
) : RequestQueue {
    private val rotatingQueue = RotatingQueueImpl<Double, RequestToken, EnqueuedRequest<Request>>()
    
    
    
    
    companion object {
        private val logger = LoggerController.getLogger("WSQueue")
    }
    
    init {
        val workers = (0 until maxRequestThread).map {
            Thread { //TODO: Change to ThreadPool
                runBlocking {
                    work()
                }
            }
        }
        
        workers.forEach {
            it.start()
        }
    }
    
    @OptIn(ExperimentalTime::class)
    private suspend fun enqueueInternal(doc: PreprocessedRequest<Request>, info: EnqueueRequestInfo) {
        optimizationPolicy.update(doc.request.request, srtfPageFactory.convertTo(doc.request.request))
        
        rotatingQueue.enqueue(
            srtfKeyExtractor.extractWorkingSetKey(doc), EnqueuedRequest(doc, info),
            optimizationPolicy.getScore(doc).fold(
                { 0.1 }, //We can't do anything so download fastly.
                {
                    it.toDouble(DurationUnit.MILLISECONDS)
                })
        )
    }
    
    override suspend fun enqueue(doc: PreprocessedRequest<Request>, info: EnqueueRequestInfo) {
        enqueueInternal(doc, info)
    }
    
    private suspend fun work() {
        while (true) {
            try {
                dequeue()
            } catch (e: Throwable) {
                logger.warn(e)
            }
        }
    }
    
    private suspend fun dequeue() {
        
        val item: Option<EnqueuedRequest<Request>> = removeFirstFromQueue()
        
        item.map {
            optimizationPolicy.removeDescriptor(
                it.request.request.request.request.token,
                srtfPageFactory.convertTo(it.request.request.request)
            )
            
            when (it.request.info.dequeue.get()) {
                DequeueDecision.ALLOW -> {
                    it.info.callBack(DequeuedRequest(it.request, DequeuedRequestInfo()).right())
                }
                DequeueDecision.DENY -> {
                    it.info.callBack(RequestDeniedException("Request denied by DequeueDecision ").left())
                }
            }
        }
    }
    
    private fun removeFirstFromQueue(): Option<EnqueuedRequest<Request>> {
        val ret = Either.catch {
            rotatingQueue.dequeue()
        }
        
        ret.swap().map {
            logger.warn("can't take from queue because: ")
            logger.warn(it)
        }
        
        return ret.orNone()
    }
}