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

package com.fivemin.core.request.queue

import arrow.core.*
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.Request
import com.fivemin.core.request.*
import kotlinx.coroutines.*
import java.util.concurrent.PriorityBlockingQueue

class RequestQueueImpl(
    private val policy: DequeueOptimizationPolicy,
    private val maxRequestThread: Int = 3,
    private val maxDelayCount: Int = 5
) :
    RequestQueue {
    data class EnqueuedRequest<Document : Request>(
        val request: PreprocessedRequest<Document>,
        val delayCount: Int,
        val info: EnqueueRequestInfo
    )

    companion object {
        private val logger = LoggerController.getLogger("RequestQueue")
    }

    private val blockQueue: PriorityBlockingQueue<EnqueuedRequest<Request>> =
        PriorityBlockingQueue(10) { o1, o2 ->
            policy.getScore(o1.request).toInt() - policy.getScore(o2.request).toInt()
        }

    init {
        val workers = (0 until maxRequestThread).map {
            Thread {
                runBlocking {
                    work()
                }
            }
        }

        workers.forEach {
            it.start()
        }
    }

    private fun enqueueInternal(doc: PreprocessedRequest<Request>, delayCount: Int, info: EnqueueRequestInfo) {
        blockQueue.put(EnqueuedRequest(doc, delayCount, info))
    }

    private suspend fun enqueueWithDelayedTask(
        req: EnqueuedRequest<Request>,
        delayCount: Long = 3000
    ) {
        runBlocking {
            delay(delayCount)
            enqueueInternal(req.request, req.delayCount + 1, req.info)
        }
    }

    override fun enqueue(doc: PreprocessedRequest<Request>, info: EnqueueRequestInfo) {
        enqueueInternal(doc, 0, info)
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
            when (it.request.info.dequeue.get()) {
                DequeueDecision.ALLOW -> {
                    it.info.callBack(DequeuedRequest(it.request, DequeuedRequestInfo()).right())
                }
                DequeueDecision.DENY -> {
                    it.info.callBack(RequestDeniedException("Request denied by DequeueDecision ").left())
                }
                DequeueDecision.DELAY -> {
                    if (it.delayCount >= maxDelayCount) {
                        it.info.callBack(RequestDeniedException("Request has delayed more than maxRequest").left())
                    } else {
                        enqueueWithDelayedTask(it)
                    }
                }
            }
        }
    }

    private fun removeFirstFromQueue(): Option<EnqueuedRequest<Request>> {
        val ret = Either.catch {
            blockQueue.take()
        }

        ret.swap().map {
            logger.warn("can't take from queue because: ")
            logger.warn(it)
        }

        return ret.orNone()
    }
}

class RequestDeniedException(str: String) : Exception(str)
