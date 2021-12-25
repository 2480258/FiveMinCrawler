package fivemin.core.engine.transaction.serialize

import arrow.core.*
import fivemin.core.LoggerController
import fivemin.core.engine.*
import fivemin.core.engine.transaction.ExecuteSerializeMovement
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
                        SerializeTransactionImpl<Document>(
                            source.request,
                            convertAttributeToTag(it.attribute, source.tags),
                            it.attribute.toList()
                        )
                    }
                }.flatten()
            }
        }


    }

    private fun convertAttributeToTag(attr: Iterable<DocumentAttribute>, connect: TagRepository): TagRepositoryImpl {
        var ret = attr.map { x ->
            Pair(x.info.name, x.item.map { y ->
                y.match({ z -> Some(z.body) }, { none() })
            }.filterOption())
        }.map {
            Tag(EnumSet.of(TagFlag.CONVERT_TO_ATTRIBUTE), it.first, it.second.first())
        }

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
    val attribute: List<DocumentAttribute>) {
}

interface PostParser<in Document : Request> {
    suspend fun getPostParseInfo(
        request: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Either<Throwable, PostParseInfo>
}