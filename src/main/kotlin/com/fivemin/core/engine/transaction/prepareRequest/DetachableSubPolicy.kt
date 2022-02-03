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

package com.fivemin.core.engine.transaction.prepareRequest

import arrow.core.*
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class TaskDetachedException : Exception()

class DetachableSubPolicy<Document : Request> :
    TransactionSubPolicy<InitialTransaction<Document>, PrepareTransaction<Document>, Document> {

    companion object {
        private val logger = LoggerController.getLogger("DetachableSubPolicy")
    }

    override suspend fun process(
        source: InitialTransaction<Document>,
        dest: PrepareTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, PrepareTransaction<Document>>> {
        return coroutineScope {
            async {
                val ret = if (dest.ifDocument({
                    it.containerOption.workingSetMode == WorkingSetMode.Enabled
                }, { false })
                ) {
                    var task = info.createTask<Document>()
                        .get4<InitialTransaction<Document>, PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, SerializeTransaction<Document>, ExportTransaction<Document>>(
                            dest.request.documentType
                        )

                    var disp = state.ifDetachable {
                        it.detach {
                            logger.debug(source.request, "trying to detach")

                            task.start(source, info, it).await().swap().orNone()
                        }
                    }

                    if (disp.isNotEmpty()) {
                        TaskDetachedException().left()
                    } else {
                        dest.right()
                    }
                } else {
                    dest.right()
                }

                ret
            }
        }
    }
}
