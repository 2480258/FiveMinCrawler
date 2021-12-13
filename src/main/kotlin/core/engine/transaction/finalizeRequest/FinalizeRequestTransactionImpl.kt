package core.engine.transaction.finalizeRequest

import arrow.core.Validated
import core.engine.*

class FinalizeRequestTransactionImpl<Document : Request>(
    override val result: Validated<Throwable, ResponseData>,
    override val tags: TagRepository, override val previous: PrepareTransaction<Document>
) : FinalizeRequestTransaction<Document>{

    override val request: Document
        get() = previous.request

    fun modifyTags(tagRepo : TagRepository) : FinalizeRequestTransaction<Document>{
        return FinalizeRequestTransactionImpl(result, tagRepo, previous)
    }
}