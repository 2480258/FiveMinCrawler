package com.fivemin.core.request.queue

import arrow.core.*
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.Request
import com.fivemin.core.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.ExperimentalTime

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

    private val conditionLock = ReentrantLock()
    private val notEmpty: Condition = conditionLock.newCondition()
    
    private val sync: Any = Any()
    private val workers: Iterable<Thread>

    private val queue: LinkedList<EnqueuedRequest<Request>>

    init {
        workers = (0 until maxRequestThread).map {
            Thread {
                runBlocking {
                    work()
                }
            }
        }

        workers.forEach {
            it.start()
        }

        queue = LinkedList()
    }

    private fun enqueueInternal(doc: PreprocessedRequest<Request>, delayCount: Int, info: EnqueueRequestInfo) {
        synchronized(sync) {
            queue.addLast(EnqueuedRequest(doc, delayCount, info))

            if (notEmpty.signalAll()) {
                enqueued.release()
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun enqueueWithDelayedTask(
        req: EnqueuedRequest<Request>,
        delayCount: Duration = 3000.microseconds
    ) {
        runBlocking {
            delay(3000)
            enqueueInternal(req.request, req.delayCount + 1, req.info)
        }
    }

    override fun enqueue(doc: PreprocessedRequest<Request>, info: EnqueueRequestInfo) {
        enqueueInternal(doc, 0, info)
    }

    private suspend fun work() {
        while (true) {
            runBlocking {
                enqueued.acquire()
                dequeue()
            }
        }
    }

    private suspend fun dequeue() {
        var item: Option<EnqueuedRequest<Request>>

        synchronized(sync) {
            try {
                item = removeFirstFromQueue()
            } finally {
                if ((!queue.isEmpty()) && enqueued.availablePermits == 0) {
                    enqueued.release()
                }
            }
        }

        item.map {
            when (it.request.info.dequeue.get()) { // TODO Fix if Exception
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
        if (queue.isEmpty()) {
            return none()
        }

        if (queue.count() == 1) {
            return Some(queue.removeFirst())
        }

        var it = // Because score changes overtime....
            queue.map {
                Pair(it, policy.getScore(it.request))
            }.sortedBy { x -> x.second }

        var ret = it.firstOrNull()?.first.toOption()

        ret.map {
            if (!queue.remove(it)) {
                logger.error(it.request.request.request.request, "can't remove queue from element")
            }
        }

        return ret
    }
}

class RequestDeniedException(str: String) : Exception(str)
