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
import arrow.core.flatten
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class AbstractPolicyOption<SrcTrans : Transaction<Document>, DstTrans : StrictTransaction<SrcTrans, Document>, out Document : Request>(
    val subPolicies: Iterable<TransactionSubPolicy<SrcTrans, DstTrans, Document>>
)

abstract class AbstractPolicy<SrcTrans : Transaction<Document>, DstTrans : StrictTransaction<SrcTrans, Document>, Document : Request>(
    private val option: AbstractPolicyOption<SrcTrans, DstTrans, Document>,
    private val movementFactory: TransactionMovementFactory<SrcTrans, DstTrans, Document>
) : TransactionPolicy<SrcTrans, DstTrans, Document, Document> {
    companion object {
        private val logger = LoggerController.getLogger("AbstractPolicy")
    }
    
    override suspend fun <Ret> progressAsync(
        trans: SrcTrans,
        info: TaskInfo,
        state: SessionStartedState,
        next: suspend (Deferred<Either<Throwable, DstTrans>>) -> Deferred<Either<Throwable, Ret>>
    ): Deferred<Either<Throwable, Ret>> {
        return coroutineScope {
            async {
                val movement = movementFactory.getMovement()
                val taskResult = movement.move(trans, info, state) {
                    it.await().map {
                        tailCall(trans, it, info, state, option.subPolicies, next)
                    }
                }
                
                taskResult.map {
                    it.await()
                }.flatten()
            }
        }
    }
    
    suspend fun <Ret> tailCall(
        trans: SrcTrans,
        dest: DstTrans,
        info: TaskInfo,
        state: SessionStartedState,
        policies: Iterable<TransactionSubPolicy<SrcTrans, DstTrans, Document>>,
        next: suspend (Deferred<Either<Throwable, DstTrans>>) -> Deferred<Either<Throwable, Ret>>
    ): Deferred<Either<Throwable, Ret>> {
        return coroutineScope {
            async {
                if (policies.count() == 1) {
                    policies.first().process(trans, dest, info, state, next)
                } else {
                    policies.first().process(trans, dest, info, state) {
                        tailCall(trans, dest, info, state, policies.drop(1), next)
                    }
                }
            }
        }.await()
    }
}
