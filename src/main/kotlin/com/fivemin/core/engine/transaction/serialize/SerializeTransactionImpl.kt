package com.fivemin.core.engine.transaction.serialize

import com.fivemin.core.engine.*

class SerializeTransactionImpl<Document : Request>(
    override val request: Document,
    override val tags: TagRepository,
    override val attributes: List<DocumentAttribute>,
    override val serializeOption: SerializeOption
) : SerializeTransaction<Document> {
    fun modifyTags(tagRepository: TagRepository) : SerializeTransactionImpl<Document>{
        return SerializeTransactionImpl<Document>(request, tagRepository, attributes, serializeOption)
    }
}