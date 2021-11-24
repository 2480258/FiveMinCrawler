package core.engine.transaction.serialize.postParser

import arrow.core.Validated
import core.engine.FinalizeRequestTransaction
import core.engine.Request
import core.engine.SessionStartedState
import core.engine.TaskInfo
import core.engine.transaction.serialize.PostParseInfo
import core.engine.transaction.serialize.PostParser

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