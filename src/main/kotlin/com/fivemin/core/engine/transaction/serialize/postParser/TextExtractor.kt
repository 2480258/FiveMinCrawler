package com.fivemin.core.engine.transaction.serialize.postParser

import com.fivemin.core.engine.HtmlMemoryData
import com.fivemin.core.engine.ParserNavigator

interface TextExtractor {
    fun parse(data: HtmlMemoryData, nav: ParserNavigator, mode: TextSelectionMode): Iterable<String>
}
