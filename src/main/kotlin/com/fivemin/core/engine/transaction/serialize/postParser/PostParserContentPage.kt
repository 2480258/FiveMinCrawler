package com.fivemin.core.engine.transaction.serialize.postParser

import arrow.core.Option
import com.fivemin.core.engine.*
import kotlinx.coroutines.Deferred

interface PostParserContentPage<in Document : Request> {
    suspend fun extract(req : FinalizeRequestTransaction<Document>, info : TaskInfo, state : SessionStartedState) : Deferred<Option<Iterable<DocumentAttribute>>>
}