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

interface TransactionSubPolicy <in SrcTrans : Transaction<Document>,
    DstTrans : StrictTransaction<SrcTrans, Document>,
    out Document : Request> {
    suspend fun <Ret> process(source: SrcTrans, dest: DstTrans, state: SessionStartedState, next: suspend (Either<Throwable, DstTrans>) -> Either<Throwable,Ret>): Either<Throwable, Ret>
}
