package core.engine.transaction.finalizeRequest

import core.engine.*

class FinalizeRequestTransactionImpl<Document : Request>(
    override val result: Result<ResponseData>,
    override val tags: TagRepository,
    override val previous: Transaction<Document>
) : FinalizeRequestTransaction<Document>{

    override val request: Document
        get() = previous.request

    fun modifyTags(tagRepo : TagRepository) : FinalizeRequestTransaction<Document>{
        return FinalizeRequestTransactionImpl(result, tagRepo, previous)
    }
}