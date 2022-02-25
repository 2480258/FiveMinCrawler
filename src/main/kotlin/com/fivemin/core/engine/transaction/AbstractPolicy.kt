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
import kotlinx.coroutines.*

data class AbstractPolicyOption<
    in SrcTrans : Transaction<Document>,
    DstTrans : StrictTransaction<SrcTrans, Document>,
    out Document : Request>(val subPolicies: Iterable<TransactionSubPolicy<SrcTrans, DstTrans, Document>>)

abstract class AbstractPolicy<
    in SrcTrans : Transaction<Document>,
    DstTrans : StrictTransaction<SrcTrans, Document>,
    Document : Request>(
    private val option: AbstractPolicyOption<SrcTrans, DstTrans, Document>,
    private val movementFactory: MovementFactory<Document>
) : TransactionPolicy<SrcTrans, DstTrans, Document, Document> {
    companion object {
        private val logger = LoggerController.getLogger("AbstractPolicy")
    }

    protected abstract fun getMovement(factory: MovementFactory<Document>): TransactionMovement<SrcTrans, DstTrans, Document>

    override suspend fun progressAsync(
        trans: SrcTrans,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, DstTrans>> {
        return coroutineScope {
            async {
                Either.catch {
                    val movement = getMovement(movementFactory)
                    val taskResult = movement.move(trans, info, state)

                    val spResult = option.subPolicies.fold(taskResult) { acc, transactionSubPolicy ->
                        coroutineScope {
                            async {
                                val dstTrans = acc.await()

                                dstTrans.map {
                                    transactionSubPolicy.process(trans, it, info, state).await()
                                }.flatten()
                            } // https://typelevel.org/cats/datatypes/Either.html
                        }
                    }

                    spResult.await()
                }.flatten()
            }
        }
    }
}
