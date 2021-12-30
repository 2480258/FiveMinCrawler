package com.fivemin.core.engine.transaction.export

import arrow.core.Either
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*

class ExportTransactionImpl<Document : Request>(
    override val request: Document,
    override val tags: TagRepository,
    override val exportHandles: Iterable<Either<Throwable, ExportResultToken>>,
) : ExportTransaction<Document> {

    companion object {
        private val logger = LoggerController.getLogger("SerializeTransactionMovementImpl")
    }

    fun modifyTags(tags: TagRepository): Taggable {
        return ExportTransactionImpl(request, tags, exportHandles)
    }
}