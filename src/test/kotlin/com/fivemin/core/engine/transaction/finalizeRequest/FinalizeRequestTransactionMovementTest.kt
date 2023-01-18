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

import arrow.core.Either
import arrow.core.right
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.getSuccResponse_Html
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsRequestDoc
import com.fivemin.core.TaskMockFactory
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.RequestType
import com.fivemin.core.engine.ResponseData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test
import java.net.URI

class FinalizeRequestTransactionMovementTest {
    
    
    @Test
    fun testValidateReleasingRequester_Move() {
        val doc =
            DocumentMockFactory.getRequest(URI("http://aaa.com"), RequestType.LINK).upgrade().upgradeAsDocument("a")
        val resp = doc.upgradeAsRequestDoc().upgrade().getSuccResponse_Html()
        val ret = doc.upgrade()
        
        val waiter: RequestWaiter = mockk()
        
        coEvery {
            waiter.request<Request, ResponseData>(any())
        } coAnswers {
            coroutineScope {
                async {
                    resp.right()
                }
            }
        }
        
        coEvery {
            resp.releaseRequester()
        } coAnswers {
            mockk()
        }
        
        val finalmove = FinalizeRequestTransactionMovement<Request>(waiter)
        
        runBlocking {
            finalmove.move(
                doc,
                TaskMockFactory.createDetachableSessionStarted<Request>(),
                { Either.catch { } })
        }
        
        coVerify(exactly = 1) {
            resp.releaseRequester()
        }
    }
}