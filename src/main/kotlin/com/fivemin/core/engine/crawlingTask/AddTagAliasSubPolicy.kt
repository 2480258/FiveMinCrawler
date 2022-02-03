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

package com.fivemin.core.engine.crawlingTask

import arrow.core.Either
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.*

class AddTagAliasSubPolicy<SrcTrans : Transaction<Document>, DstTrans : StrictTransaction<SrcTrans, Document>, Document : Request> :
    TransactionSubPolicy<SrcTrans, DstTrans, Document> {

    companion object {
        private val logger = LoggerController.getLogger("AddTagAliasSubPolicy")
    }

    override suspend fun process(
        source: SrcTrans,
        dest: DstTrans,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, DstTrans>> {
        return coroutineScope {
            async {
                Either.catch {
                    logger.debug(source.request, "adding tags")
                    var ret = info.uniqueKeyProvider.tagKey.create(dest.tags)

                    ret.forEach {
                        state.addAlias(it)

                        logger.info(source.request.getDebugInfo() + " < [Tag]" + it.toString())
                    }

                    dest
                }
            }
        }
    }
}
