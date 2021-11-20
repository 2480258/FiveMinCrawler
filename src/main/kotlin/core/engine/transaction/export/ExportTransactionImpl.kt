package core.engine.transaction.export

import core.engine.*

class ExportTransactionImpl<Document : Request>(
    override val request: Document,
    override val tags: TagRepository,
    override val exportHandles: Iterable<Result<ExportResultToken>>,
) : ExportTransaction<Document> {
    fun modifyTags(tags: TagRepository) : Taggable{
        return ExportTransactionImpl(request, tags, exportHandles)
    }
}