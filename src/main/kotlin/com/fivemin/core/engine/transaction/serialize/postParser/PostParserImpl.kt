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

package com.fivemin.core.engine.transaction.serialize.postParser

import arrow.core.Either
import arrow.core.filterOption
import arrow.core.getOrElse
import arrow.core.singleOrNone
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.FinalizeRequestTransaction
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.SessionStartedState
import com.fivemin.core.engine.TaskInfo
import com.fivemin.core.engine.transaction.serialize.PostParseInfo
import com.fivemin.core.engine.transaction.serialize.PostParser

class PostParserImpl(private val pages: List<PostParserContentPage<Request>>) : PostParser<Request> {
    
    companion object {
        private val logger = LoggerController.getLogger("PostParserImpl")
    }
    
    override suspend fun getPostParseInfo(
        request: FinalizeRequestTransaction<Request>,
        info: TaskInfo,
        state: SessionStartedState
    ): Either<Throwable, PostParseInfo> {
        val ret = Either.catch {
            val extractedAttributes = pages.map {
                it.extract(request, info, state).await()
            }.filterOption()

            val results = extractedAttributes.singleOrNone {
                it.any()
            }.getOrElse { listOf() }

            PostParseInfo(results.toList())
        }
        
        logger.debug(ret, "failed to getPostParseInfo")
        
        return ret
    }
}
