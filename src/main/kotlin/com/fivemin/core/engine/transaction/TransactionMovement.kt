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
import com.fivemin.core.engine.*
import kotlinx.coroutines.Deferred

interface TransactionMovement<
    in SrcTrans : Transaction<Request>,
    out DstTrans : StrictTransaction<SrcTrans, Document>,
    out Document : Request> {
    suspend fun <Ret> move(source: SrcTrans, info: TaskInfo, state: SessionStartedState, next : suspend (Deferred<Either<Throwable, DstTrans>>) -> Deferred<Either<Throwable, Ret>>): Deferred<Either<Throwable, Ret>>
}

interface PrepareRequestMovement<Document : Request> :
    TransactionMovement<InitialTransaction<Document>, PrepareTransaction<Document>, Document>

interface ExecuteRequestMovement<Document : Request> :
    TransactionMovement<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document>

interface ExecuteSerializeMovement<Document : Request> :
    TransactionMovement<FinalizeRequestTransaction<Document>, SerializeTransaction<Document>, Document>

interface ExecuteExportMovement<Document : Request> :
    TransactionMovement<SerializeTransaction<Document>, ExportTransaction<Document>, Document>

class PageNotFoundException : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
