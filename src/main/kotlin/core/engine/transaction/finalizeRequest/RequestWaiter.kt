package core.engine.transaction.finalizeRequest

import core.engine.Request
import core.engine.ResponseData
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore

class RequestWaiter(private val requestTaskFactory: RequestTaskFactory) {
    class TaskWaitHandle<T>{
        private var result : T? = null
        private val semaphore = Semaphore(1)

        suspend fun run(act: () -> Unit) : Deferred<T>{
            return coroutineScope {
                async{
                    try{
                        act()
                    } finally {
                        semaphore.acquire()
                    }

                    result!!
                }
            }
        }

        fun registerResult(_result : T){
            try{
                result = _result
            }finally {
                semaphore.release()
            }
        }
    }

    suspend fun <Document : Request, GivenResponse : ResponseData> request(request: DocumentRequest<Document>) : Deferred<Result<GivenResponse>> {
        var task = requestTaskFactory.create()
        var wait = TaskWaitHandle<Result<GivenResponse>>()

        return wait.run {
            task.run(request, ResponseCallback<Document, GivenResponse> { x ->
                wait.registerResult(x)
            })
        }
    }
}