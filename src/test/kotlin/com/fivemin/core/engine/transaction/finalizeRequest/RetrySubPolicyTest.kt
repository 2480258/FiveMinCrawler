package com.fivemin.core.engine.transaction.finalizeRequest

import com.fivemin.core.ElemIterator
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.StringUniqueKeyProvider
import com.fivemin.core.engine.transaction.UriUniqueKeyProvider
import io.mockk.every
import io.mockk.mockk

class RetrySubPolicyTest {

    val uriIt = ElemIterator(UriIterator())

//    @Test
//    fun testProcess() {
//        val retryProc = RetrySubPolicy<Request>()
//
    //       runBlocking {
//            val req = getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a")
//            val info = mockInfo_withget1()
//            val state = mockState()

//            retryProc.process(req, req.upgrade(req.upgradeAsRequestReq().upgrade().getCriticalBodyResponse()), info.first, mockState())

//            coVerify(exactly = 1) {
//                state.retryAsync(any<suspend (Any) -> Deferred<Either<Throwable, Any>>>())
    //           }
    //       }
    //   }

    fun mockInfo_withget1(): Pair<TaskInfo, CrawlerTask1<PrepareTransaction<Request>, FinalizeRequestTransaction<Request>, Request, Request>> {
        val provider = KeyProvider(UriUniqueKeyProvider(), StringUniqueKeyProvider())
        val taskInfo: TaskInfo = mockk()

        val createdTaskFac: CrawlerTaskFactory<Request> = mockk()
        val createdTask: CrawlerTask1<PrepareTransaction<Request>, FinalizeRequestTransaction<Request>, Request, Request> = mockk()

        every {
            taskInfo.uniqueKeyProvider
        } returns (provider)

        every {
            taskInfo.createTask<Request>()
        } returns(createdTaskFac)

        return Pair(taskInfo, createdTask)
    }
}
