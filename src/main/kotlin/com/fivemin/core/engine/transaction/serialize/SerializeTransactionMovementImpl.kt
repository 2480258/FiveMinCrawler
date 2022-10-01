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

package com.fivemin.core.engine.transaction.serialize

import arrow.core.*
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.ExecuteSerializeMovement
import kotlinx.coroutines.Deferred
import java.util.*

class SerializeTransactionMovementImpl<Document : Request>(private val postParser: PostParser<Document>) :
    ExecuteSerializeMovement<Document> {
    companion object {
        private val logger = LoggerController.getLogger("SerializeTransactionMovementImpl")
    }
    
    private fun convertAttributeToTag(attr: Iterable<DocumentAttribute>, connect: TagRepository): TagRepositoryImpl {
        val ret = attr.map { x ->
            Pair(
                x.info.name,
                x.item.map { y ->
                    y.match({ z -> Some(z.body) }, { none() })
                }.filterOption()
            )
        }.map {
            if (it.second.any()) {
                Tag(EnumSet.of(TagFlag.CONVERT_TO_ATTRIBUTE), it.first, it.second.first()).toOption()
            } else {
                none()
            }
        }.filterOption()
        
        return TagRepositoryImpl(ret.toOption(), connect.toOption())
    }
    
    override suspend fun <Ret> move(
        source: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState,
        next: suspend (Either<Throwable, SerializeTransaction<Document>>) -> Either<Throwable, Ret>
    ): Either<Throwable, Ret> {
        logger.debug(source.request.getDebugInfo() + " < serializing transaction")
        
        val ret = Either.catch {
            postParser.getPostParseInfo(source, info, state).map {
                source.previous.ifDocument({ doc ->
                    SerializeTransactionImpl<Document>(
                        source.request,
                        convertAttributeToTag(it.attribute, source.tags),
                        it.attribute.toList(),
                        SerializeOption(doc.requestOption, doc.parseOption, doc.containerOption)
                    )
                }, {
                    throw IllegalArgumentException("not support for serialization transaction of non-text based document")
                })
            }
        }.flatten()
        
        logger.debug(ret, "failed to move")
        
        return next(ret)
    }
}

interface PostParserContentPage<in Document : Request> {
    fun extract(
        request: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, Iterable<DocumentAttribute>>>
}

data class PostParseInfo(
    val attribute: List<DocumentAttribute>
)

interface PostParser<in Document : Request> {
    suspend fun getPostParseInfo(
        request: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Either<Throwable, PostParseInfo>
}
