package com.fivemin.core.engine.transaction.export

import arrow.core.Option
import com.fivemin.core.engine.DocumentAttributeElement
import com.fivemin.core.engine.DocumentAttributeInfo
import com.fivemin.core.engine.TagRepository

data class ExportAttributeInfo(val info: ExportAttributeLocator, val element: DocumentAttributeElement, val tagRepo: TagRepository)

data class ExportAttributeLocator(val info: DocumentAttributeInfo, val index: Option<Int>) {
    val isList: Boolean = !index.isEmpty()
}
