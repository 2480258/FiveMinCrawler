package com.fivemin.core.engine.transaction.finalizeRequest

import arrow.core.Either
import com.fivemin.core.engine.*

class FinalizeRequestTransactionImpl<Document : Request>(
    override val result: Either<Throwable, ResponseData>,
    override val tags: TagRepository, override val previous: PrepareTransaction<Document>
) : FinalizeRequestTransaction<Document>{

    override val request: Document
        get() = previous.request

    fun modifyTags(tagRepo : TagRepository) : FinalizeRequestTransaction<Document>{
        return FinalizeRequestTransactionImpl(result, tagRepo, previous)
    }
}