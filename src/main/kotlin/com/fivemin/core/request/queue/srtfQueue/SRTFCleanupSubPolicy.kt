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

package com.fivemin.core.request.queue.srtfQueue

import arrow.core.Either
import arrow.core.Some
import arrow.core.flatten
import arrow.core.none
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class SRTFLogSubPolicy constructor(
    private val timingRepo: SRTFTimingRepository,
    private val descriptorFactory: SRTFPageDescriptorFactory,
    private val srtfOptimizationPolicy: SRTFOptimizationPolicy
) : TransactionSubPolicy<PrepareTransaction<Request>, FinalizeRequestTransaction<Request>, Request> {
    override suspend fun <Ret> process(
        source: PrepareTransaction<Request>,
        dest: FinalizeRequestTransaction<Request>,
        info: TaskInfo,
        state: SessionStartedState,
        next: suspend (Deferred<Either<Throwable, FinalizeRequestTransaction<Request>>>) -> Deferred<Either<Throwable, Ret>>
    ): Deferred<Either<Throwable, Ret>> {
        try {
            val result = next(coroutineScope {
                async {
                    val time = dest.result.map {
                        it.responseBody.ifSucc({
                            Some(it.responseTime)
                        }, {
                            none()
                        })
                    }.orNone().flatten()
            
                    time.map {
                        timingRepo.reportTiming(descriptorFactory.convertTo(source), it.duration)
                    }
            
                    Either.Right(dest)
                }
            })
            
            result.await() // waits until request finishes
            
            return result
            
        } finally {
            srtfOptimizationPolicy.removeToken(dest.request.token)
        }
    }
}