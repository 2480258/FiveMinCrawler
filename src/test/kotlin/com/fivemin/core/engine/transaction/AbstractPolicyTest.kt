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

package com.fivemin.core.engine.transaction

import arrow.core.Either
import arrow.core.right
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.TaskMockFactory
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.serialize.PostParseInfo
import com.fivemin.core.engine.transaction.serialize.PostParser
import com.fivemin.core.engine.transaction.serialize.SerializeTransactionMovementImpl
import com.fivemin.core.engine.transaction.serialize.SerializeTransactionPolicy
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test
import java.net.URI

class TestSubPolicy : TransactionSubPolicy<FinalizeRequestTransaction<Request>, SerializeTransaction<Request>, Request> {
    override suspend fun <Ret> process(
        source: FinalizeRequestTransaction<Request>,
        dest: SerializeTransaction<Request>,
        info: TaskInfo,
        state: SessionStartedState,
        next: suspend (Either<Throwable, SerializeTransaction<Request>>) -> Either<Throwable, Ret>
    ): Either<Throwable, Ret> {
        return next(dest.right())
    }
    
}

class AbstractPolicyTest {
    @Test
    fun testProgressAsync() {
        val sub1 = spyk(TestSubPolicy())
        val sub2 = spyk(TestSubPolicy())
        
        val option = AbstractPolicyOption<FinalizeRequestTransaction<Request>, SerializeTransaction<Request>, Request>(
            listOf(
                sub1, sub2
            )
        )
        val movementFac: TransactionMovementFactory<FinalizeRequestTransaction<Request>, SerializeTransaction<Request>, Request> =
            mockk()
        val postParser : PostParser<Request> = mockk()
        val postParserInfo : PostParseInfo = mockk()
        
        coEvery {
            postParserInfo.attribute
        } coAnswers {
            listOf()
        }
        
        coEvery {
            postParser.getPostParseInfo(any(), any(), any())
        } coAnswers {
            postParserInfo.right()
        }
        
        val movement  = SerializeTransactionMovementImpl<Request>(postParser)
        
        val src =
            DocumentMockFactory.getRequest(URI("http://aaa.com"), RequestType.LINK).upgrade().upgradeAsDocument("a")
                .upgrade()
        
        coEvery {
            movementFac.getMovement()
        } coAnswers {
            movement
        }
        
        val policy = SerializeTransactionPolicy<Request>(option, movementFac)
        
        runBlocking {
            policy.progressAsync(src,
                TaskMockFactory.createTaskInfo(),
                TaskMockFactory.createDetachableSessionStarted<Request>(),
                { Either.catch { } })
            
        }
        
        coVerify(exactly = 1) {
            sub1.process(any(), any(), any(), any(), any<suspend (Either<Throwable, SerializeTransaction<Request>>) -> Either<Throwable, Any>>())
            sub2.process(any(), any(), any(), any(), any<suspend (Either<Throwable, SerializeTransaction<Request>>) -> Either<Throwable, Any>>())
        }
    }
}