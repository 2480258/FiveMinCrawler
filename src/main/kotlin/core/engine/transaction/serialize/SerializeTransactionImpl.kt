package core.engine.transaction.serialize

import core.engine.*

class SerializeTransactionImpl<Document : Request>(
    override val request: Document,
    override val tags: TagRepository,
    override val attributes: List<DocumentAttribute>
) : SerializeTransaction<Document> {
    fun modifyTags(tagRepository: TagRepository) : SerializeTransactionImpl<Document>{
        return SerializeTransactionImpl<Document>(request, tagRepository, attributes)
    }
}