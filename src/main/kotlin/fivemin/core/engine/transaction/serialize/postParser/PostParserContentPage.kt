package fivemin.core.engine.transaction.serialize.postParser

import fivemin.core.engine.*
import kotlinx.coroutines.Deferred

interface PostParserContentPage<in Document : Request> {
    suspend fun extract(req : FinalizeRequestTransaction<Document>, info : TaskInfo, state : SessionStartedState) : Deferred<Iterable<DocumentAttribute>>
}