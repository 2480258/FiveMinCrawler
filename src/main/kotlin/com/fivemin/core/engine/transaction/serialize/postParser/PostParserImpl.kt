package com.fivemin.core.engine.transaction.serialize.postParser

import arrow.core.Either
import arrow.core.filterOption
import arrow.core.getOrElse
import arrow.core.singleOrNone
import com.fivemin.core.engine.FinalizeRequestTransaction
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.SessionStartedState
import com.fivemin.core.engine.TaskInfo
import com.fivemin.core.engine.transaction.serialize.PostParseInfo
import com.fivemin.core.engine.transaction.serialize.PostParser

class PostParserImpl(private val pages: List<PostParserContentPage<Request>>) : PostParser<Request> {
    override suspend fun getPostParseInfo(
        request: FinalizeRequestTransaction<Request>,
        info: TaskInfo,
        state: SessionStartedState
    ): Either<Throwable, PostParseInfo> {
        return Either.catch {
            var ret = pages.map {
                it.extract(request, info, state).await()
            }.filterOption()

            var q = ret.singleOrNone {
                it.any()
            }.getOrElse { listOf() }

            PostParseInfo(q.toList())
        }
    }
}
