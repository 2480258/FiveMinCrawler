package com.fivemin.core.engine.transaction

import com.fivemin.core.engine.*

data class PrepareDocumentRequestTransactionImpl<out Document : Request>(
    override val previous: Transaction<Document>,
    override val tags: TagRepository,
    override val requestOption: RequestOption,
    override val parseOption: ParseOption,
    override val containerOption: ContainerOption
) : PrepareDocumentTransaction<Document> {
    override val request : Document = previous.request

    fun modifyTags(tags: TagRepository) : Taggable{
        return PrepareDocumentRequestTransactionImpl(previous, tags, requestOption, parseOption, containerOption)
    }
}


data class PrepareRequestTransactionImpl<Document : Request>(
    override val previous: Transaction<Document>,
    override val tags: TagRepository,
    override val requestOption: RequestOption
) : PrepareTransaction<Document>{

    override val request: Document
        get() = previous.request

    fun modifyTags(tags : TagRepository) : Taggable{
        return PrepareRequestTransactionImpl(previous, tags, requestOption)
    }
}