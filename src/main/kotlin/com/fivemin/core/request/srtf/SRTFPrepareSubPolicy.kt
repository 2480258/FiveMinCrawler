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

package com.fivemin.core.request.srtf

import arrow.core.Either
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class SRTFPrepareSubPolicy(private val sc: SRTFScheduler) : TransactionSubPolicy<InitialTransaction<Request>, PrepareTransaction<Request>, Request> {

    companion object {
        private val logger = LoggerController.getLogger("SRTFPrepareSubPolicy")
    }

    override suspend fun process(
        source: InitialTransaction<Request>,
        dest: PrepareTransaction<Request>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, PrepareTransaction<Request>>> {
        return coroutineScope {
            async {
                val detachablity = state.isDetachable == DetachableState.WANT

                val result = Either.catch {
                    sc.atPrepareStage(dest, detachablity)

                    dest
                }

                result.swap().map {
                    logger.warn(it)
                }

                result
            }
        }
    }
}
