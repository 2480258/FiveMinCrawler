package fivemin.core.engine.transaction.serialize.postParser

import arrow.core.Validated
import fivemin.core.engine.FinalizeRequestTransaction
import fivemin.core.engine.Request
import fivemin.core.engine.SessionStartedState
import fivemin.core.engine.TaskInfo
import fivemin.core.engine.transaction.serialize.PostParseInfo
import fivemin.core.engine.transaction.serialize.PostParser

class PostParserImpl (private val pages : List<PostParserContentPage<Request>>): PostParser<Request> {
    override suspend fun getPostParseInfo(
        request: FinalizeRequestTransaction<Request>,
        info: TaskInfo,
        state: SessionStartedState
    ): Validated<Throwable, PostParseInfo> {
        return Validated.catch {
            var ret = pages.map {
                it.extract(request, info, state)
            }.single()

            PostParseInfo(ret.await().toList())
        }
    }
}