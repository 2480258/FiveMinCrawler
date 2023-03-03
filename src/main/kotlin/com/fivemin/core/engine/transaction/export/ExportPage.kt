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

package com.fivemin.core.engine.transaction.export

import arrow.core.*
import arrow.core.filterOption
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.logger.Log
import com.fivemin.core.logger.LogLevel

interface ExportPage {
    val pageName: String
    
    /**
     * Checks can be exported by this Page.
     */
    fun <Document : Request> isAcceptable(trans: SerializeTransaction<Document>): Boolean
    
    /**
     * Export pages.
     */
    fun <Document : Request> export(trans: SerializeTransaction<Document>): Iterable<ExportHandle>
}

class ExportPageImpl(override val pageName: String, private val targetAttributeName: Iterable<String>, private val adapter: ExportAdapter) : ExportPage {

    private val specialAttributeTagFactory: SpecialAttributeTagFactory = SpecialAttributeTagFactory()
    override fun <Document : Request> isAcceptable(trans: SerializeTransaction<Document>): Boolean {
        return trans.serializeOption.parseOption.name.name == pageName
    }
    
    override fun <Document : Request> export(trans: SerializeTransaction<Document>): Iterable<ExportHandle> {
        return adapter.parseAndExport(trans.request, parseInfo(trans)).map {
            it.orNull().toOption()
        }.filterOption()
    }

    private fun <Document : Request> parseInfo(trans: SerializeTransaction<Document>): Iterable<ExportAttributeInfo> {
        val ret = tagSelect(trans).map {
            Pair(it.info, buildTagAll(trans, it.info.name))
        }

        return ret.flatMap { x ->
            (0 until x.second.count()).map { y ->
                ExportAttributeInfo(ExportAttributeLocator(x.first, y.toOption()), x.second.entries.elementAt(y).key, x.second.entries.elementAt(y).value)
            }
        }
    }

    private fun <Document : Request> tagSelect(trans: SerializeTransaction<Document>): Iterable<DocumentAttribute> {
        return trans.attributes.filter {
            targetAttributeName.contains(it.info.name)
        }
    }

    private fun <Document : Request> buildTagAll(trans: SerializeTransaction<Document>, targetAttributeName: String): Map<DocumentAttributeElement, TagRepository> {
        val ret = trans.attributes.single {
            it.info.name == targetAttributeName
        }

        return (0 until ret.item.count()).associate {
            Pair(ret.item.elementAt(it), buildSpecialTags(trans, targetAttributeName, it))
        }
    }

    private fun <Document : Request> buildSpecialTags(trans: SerializeTransaction<Document>, targetAttributeName: String, targetAttrIdx: Int): TagRepository {
        return specialAttributeTagFactory.build(trans, AttributeLocator(targetAttributeName, targetAttrIdx))
    }
}
