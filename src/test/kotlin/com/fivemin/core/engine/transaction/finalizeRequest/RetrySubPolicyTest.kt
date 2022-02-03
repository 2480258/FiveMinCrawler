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
