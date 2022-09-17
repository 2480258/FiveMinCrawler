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

package com.fivemin.core.engine.transaction.export

import arrow.core.Either
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.ExecuteExportMovement

class ExportTransactionMovement<Document : Request>(private val parser: ExportParser, private val state: ExportState) :
    ExecuteExportMovement<Document> {
    
    companion object {
        private val logger = LoggerController.getLogger("ExportTransactionMovement")
    }
    
    private fun saveResult(handles: Iterable<ExportHandle>): Iterable<Either<Throwable, ExportResultToken>> {
        return handles.map { x ->
            state.create(x)
        }.map {
            val ret = it.save()
            
            ret.mapLeft { x ->
                logger.warn(it.info.token.fileName.name.name + " < not exported due to: " + x.message)
            }
            
            ret.map { x ->
                logger.info(it.info.token.fileName.name.name + " < exported")
            }
            ret
        }
    }
    
    override suspend fun <Ret> move(
        source: SerializeTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState,
        next: suspend (Either<Throwable, ExportTransaction<Document>>) -> Either<Throwable, Ret>
    ): Either<Throwable, Ret> {
        logger.debug(source.request, "exporting transaction")
        val ret = parser.parse(source)
        
        return next(Either.catch {
            ExportTransactionImpl(source.request, source.tags, saveResult(ret))
        })
    }
}
