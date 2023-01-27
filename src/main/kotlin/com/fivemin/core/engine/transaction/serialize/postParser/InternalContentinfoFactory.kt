/*
 *
 *     FiveMinCrawler
 *     Copyright (C) 2022  2480258
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.fivemin.core.engine.transaction.serialize.postParser

import arrow.core.Option
import arrow.core.toOption
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*

interface InternalContentInfoFactory<in Document : Request> {
    suspend fun get(trans: FinalizeRequestTransaction<Document>): Option<Iterable<InternalContentInfo>>
}

data class InternalContentInfo(val attributeName: String, val data: List<String>)

enum class TextSelectionMode {
    INNER_HTML, OUTER_HTML, TEXT_CONTENT
}

data class InternalContentParser(
    val attributeName: String,
    val nav: ParserNavigator,
    val selectionMode: TextSelectionMode
)

class InternalContentInfoFactoryImpl<Document : Request>(
    private val factories: Iterable<InternalContentParser>,
    private val attributeFactory: DocumentAttributeFactory,
    private val textExtractor: TextExtractor
) : InternalContentInfoFactory<Document> {
    
    companion object {
        private val logger = LoggerController.getLogger("InternalContentInfoFactoryImpl")
    }
    
    override suspend fun get(trans: FinalizeRequestTransaction<Document>): Option<List<InternalContentInfo>> {
        
        val ret = trans.result.map { responseData ->
            responseData.responseBody.ifSuccAsync({ successBody ->
                successBody.body.ifFile({ // remove temp file because anyway it should be read before.
                    it.file.remove()
                }, { })
                
                successBody.body.ifHtml({ htmlMemoryData ->
                    factories.map { x ->
                        InternalContentInfo(
                            x.attributeName,
                            textExtractor.parse(htmlMemoryData, x.nav, x.selectionMode).toList()
                        )
                    }
                }, { listOf() })
            }, { listOf() })
        }
        
        ret.swap().map {
            logger.warn(trans.request.getDebugInfo() + " < can't extract internal attribute from due to: " + it)
        }
        
        return ret.orNull().toOption()
        
    }
}
