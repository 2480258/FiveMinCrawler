package fivemin.core.engine.transaction

import fivemin.core.engine.*

data class InitialTransactionImpl<Document : Request>(
    override val option: InitialOption,
    override val tags: TagRepository,
    override val request: Document
) : InitialTransaction<Document>{

    fun modifyTags(tags : TagRepository) : Taggable{
        return InitialTransactionImpl(option, tags, request)
    }
}