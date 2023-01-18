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
import arrow.core.left
import arrow.core.right
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.CancellationException
import java.util.concurrent.atomic.AtomicInteger

/**
 * Subpolicy for limiting enitre crawling pages.
 */
class LimitMaxPageSubPolicy<Document : Request>(private val maxPageNum: Int) :
    TransactionSubPolicy<InitialTransaction<Document>, PrepareTransaction<Document>, Document> {
    
    val pageCount: AtomicInteger = AtomicInteger(0)
    
    /**
     * Increase page count. if count is equals or exceeds, returns ExceedsMaxPageException.
     * Call only once for one document.
     */
    override suspend fun <Ret> process(
        source: InitialTransaction<Document>,
        dest: PrepareTransaction<Document>,
        
        state: SessionStartedState,
        next: suspend (Either<Throwable, PrepareTransaction<Document>>) -> Either<Throwable, Ret>
    ): Either<Throwable, Ret> {
        val cnt = pageCount.getAndIncrement()
        
        return if (cnt >= maxPageNum) {
            throw ExceedsMaxPageException()
        } else {
            next(
                dest.right()
            )
        }
    }
}

class ExceedsMaxPageException : CancellationException()