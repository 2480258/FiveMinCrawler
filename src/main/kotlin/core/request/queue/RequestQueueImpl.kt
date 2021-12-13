package core.request.queue

import arrow.core.*
import core.engine.Request
import core.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import java.util.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class RequestQueueImpl (private val policy : DequeueOptimizationPolicy, private val maxRequestThread : Int = 3, private val maxDelayCount : Int = 5) :
    RequestQueue {
    data class EnqueuedRequest<Document : Request> (val request : PreprocessedRequest<Document>, val delayCount : Int, val info : EnqueueRequestInfo)

    private val enqueued : Semaphore = Semaphore(1)
    private val sync : Any = Any()

    private val workers : Iterable<Thread>

    private val queue : LinkedList<EnqueuedRequest<Request>>

    init {
        workers = (0 until maxRequestThread).map {
            Thread {
                work() }
        }

        workers.forEach {
            it.start()
        }

        queue = LinkedList()
    }

    private fun enqueueInternal(doc : PreprocessedRequest<Request>, delayCount: Int, info: EnqueueRequestInfo){
        synchronized(sync){
            queue.addLast(EnqueuedRequest(doc, delayCount, info))

            if(enqueued.availablePermits == 0){
                enqueued.release()
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun enqueueWithDelayedTask(req : EnqueuedRequest<Request>, delayCount: Duration = Duration.microseconds(3000)){
        coroutineScope {
            launch {
                delay(delayCount)
                enqueueInternal(req.request, req.delayCount + 1, req.info)
            }
        }
    }

    override fun enqueue(doc : PreprocessedRequest<Request>, info: EnqueueRequestInfo){
        enqueueInternal(doc, 0, info)
    }

    private fun work(){
        while(true){
            enqueued.tryAcquire()
            runBlocking {
                dequeue()
            }
        }
    }

    private suspend fun dequeue(){
        var item : Option<EnqueuedRequest<Request>>

        synchronized(sync){
            item = removeFirstFromQueue()

            item.map {
                if(enqueued.availablePermits == 0){
                    enqueued.release()
                }
            }
        }

        item.map {
            when(it.request.info.dequeue.get()){
                DequeueDecision.ALLOW -> {
                    it.info.callBack(DequeuedRequest(it.request, DequeuedRequestInfo()).valid())
                }
                DequeueDecision.DELAY -> {
                    it.info.callBack(RequestDeniedException("Request denied by DequeueDecision ").invalid())
                }
                DequeueDecision.DENY -> {
                    if(it.delayCount >= maxDelayCount){
                        it.info.callBack(RequestDeniedException("Request has denied more than maxRequest").invalid())
                    } else{
                        enqueueWithDelayedTask(it)
                    }
                }
            }
        }
    }

    private fun removeFirstFromQueue() : Option<EnqueuedRequest<Request>> {
        if(queue.isEmpty()){
            return none()
        }

        if(queue.count() == 1){
            return Some(queue.removeFirst())
        }

        var it =  //Because score changes overtime....
            queue.map{
                Pair(it, policy.getScore(it.request))
            }.sortedBy { x -> x.second }

        return it.firstOrNull()?.first.toOption()
    }


}

class RequestDeniedException(str : String) : Exception(str)