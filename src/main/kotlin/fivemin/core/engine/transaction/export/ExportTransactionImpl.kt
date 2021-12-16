package fivemin.core.engine.transaction.export

import arrow.core.Validated
import fivemin.core.engine.*

class ExportTransactionImpl<Document : Request>(
    override val request: Document,
    override val tags: TagRepository,
    override val exportHandles: Iterable<Validated<Throwable, ExportResultToken>>,
) : ExportTransaction<Document> {
    fun modifyTags(tags: TagRepository) : Taggable{
        return ExportTransactionImpl(request, tags, exportHandles)
    }
}