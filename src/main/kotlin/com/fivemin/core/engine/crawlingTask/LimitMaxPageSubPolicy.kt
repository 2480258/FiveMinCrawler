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

package com.fivemin.core.engine.crawlingTask

import arrow.core.Either
import arrow.core.right
import arrow.core.left
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.atomic.AtomicInteger

class LimitMaxPageSubPolicy<Document : Request> (private val maxPageNum: Int) :
    TransactionSubPolicy<InitialTransaction<Document>, PrepareTransaction<Document>, Document>{
    
    val pageCount : AtomicInteger = AtomicInteger(0)
    
    override suspend fun process(
        source: InitialTransaction<Document>,
        dest: PrepareTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, PrepareTransaction<Document>>> {
        return coroutineScope {
            async {
                val cnt = pageCount.getAndIncrement()
    
                if(cnt >= maxPageNum) {
                    ExceedsMaxPageException().left()
                } else {
                    dest.right()
                }
    
            }
        }
    
    }
}

class ExceedsMaxPageException : Exception()