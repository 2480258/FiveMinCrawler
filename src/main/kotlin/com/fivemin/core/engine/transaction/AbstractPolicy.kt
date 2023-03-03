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
import arrow.core.right
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*

data class AbstractPolicyOption<SrcTrans : Transaction<Document>, DstTrans : StrictTransaction<SrcTrans, Document>, out Document : Request>(
    val subPolicies: Iterable<TransactionSubPolicy<SrcTrans, DstTrans, Document>>
)

abstract class AbstractPolicy<SrcTrans : Transaction<Document>, DstTrans : StrictTransaction<SrcTrans, Document>, Document : Request>(
    private val option: AbstractPolicyOption<SrcTrans, DstTrans, Document>,
    private val movementFactory: TransactionMovementFactory<SrcTrans, DstTrans, Document>
) : TransactionPolicy<SrcTrans, DstTrans, Document, Document> {
    
    override suspend fun <Ret> progressAsync(
        trans: SrcTrans,
        
        state: SessionStartedState,
        next: suspend (Either<Throwable, DstTrans>) -> Either<Throwable, Ret>
    ): Either<Throwable, Ret> {
        val movement = movementFactory.getMovement()
        val taskResult = movement.move(trans, state) {
            it.map {
                tailCall(trans, it, state, option.subPolicies, next)
            }.flatten()
        }
        
        return taskResult
    }
    
    private suspend fun <Ret> tailCall(
        trans: SrcTrans,
        dest: DstTrans,
        
        state: SessionStartedState,
        policies: Iterable<TransactionSubPolicy<SrcTrans, DstTrans, Document>>,
        next: suspend (Either<Throwable, DstTrans>) -> Either<Throwable, Ret>
    ): Either<Throwable, Ret> {
        return if (policies.count() == 0) {
            next(dest.right())
        } else if (policies.count() == 1) {
            policies.first().process(trans, dest, state, next)
        } else {
            policies.first().process(trans, dest, state) {
                tailCall(trans, dest, state, policies.drop(1), next)
            }
        }
    }
}
