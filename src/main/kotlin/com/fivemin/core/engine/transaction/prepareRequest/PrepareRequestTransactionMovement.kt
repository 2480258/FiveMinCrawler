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

import arrow.core.Either
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.PageNotFoundException
import com.fivemin.core.engine.transaction.PrepareRequestMovement
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class PrepareRequestTransactionMovement<Document : Request> (private val preParser: PreParser) : PrepareRequestMovement<Document> {

    companion object {
        private val logger = LoggerController.getLogger("PrepareRequestTransactionMovement")
    }

    override suspend fun move(
        source: InitialTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, PrepareTransaction<Document>>> {
        return coroutineScope {
            async {
                logger.debug(source.request.getDebugInfo() + " < Creating prepare transaction")
                preParser.generateInfo(source).toEither { PageNotFoundException() }
            }
        }
    }
}
