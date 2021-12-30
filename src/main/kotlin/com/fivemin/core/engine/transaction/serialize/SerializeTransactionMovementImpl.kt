package com.fivemin.core.engine.transaction.serialize

import arrow.core.*
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.ExecuteSerializeMovement
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.*

class SerializeTransactionMovementImpl<Document : Request>(private val postParser: PostParser<Document>) :
    ExecuteSerializeMovement<Document> {
    companion object {
        private val logger = LoggerController.getLogger("SerializeTransactionMovementImpl")
    }

    override suspend fun move(
        source: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, SerializeTransaction<Document>>> {
        logger.debug(source.request.getDebugInfo() + " < serializing transaction")

        return coroutineScope {
            async {
                Either.catch {
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
            }
        }
    }

    private fun convertAttributeToTag(attr: Iterable<DocumentAttribute>, connect: TagRepository): TagRepositoryImpl {
        var ret = attr.map { x ->
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