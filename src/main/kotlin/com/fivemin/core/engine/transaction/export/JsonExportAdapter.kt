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

import arrow.core.Either
import arrow.core.toOption
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.logger.Log
import com.fivemin.core.logger.LogLevel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.util.*

class JsonExportAdapter(private val fileNameExp: TagExpression, private val exportHandleFactory: ExportHandleFactory) :
    ExportAdapter {
    companion object {
        private val logger = LoggerController.getLogger("JsonExportAdapter")
    }
    
    override fun parseAndExport(
        request: Request,
        info: Iterable<ExportAttributeInfo>
    ): Iterable<Either<Throwable, ExportHandle>> {

        val attributeTuple = info.map { x ->
            x.element.match({ y ->
                Triple(x.tagRepo, x.locator, y.body)
            }, { null })
        }

        val tagsFromAttributes = attributeTuple.filter {
            it != null && it.second.isList
        }.map { x ->
            Tag(EnumSet.of(TagFlag.NONE), x!!.second.info.name, x.third) //Make internal attribute to tags. It allows to users change file path by parsed results.
        }

        val addedTagrepo = attributeTuple.map {
            Triple(fileNameExp.build(TagRepositoryImpl(tagsFromAttributes.toOption(), it!!.first.toOption())), it.second, it.third)
        }.groupBy {
            it.first
        }

        return addedTagrepo.map {
            val ret = save(it)
            
            ret
        }
    }
    
    @Log(
        beforeLogLevel = LogLevel.DEBUG,
        afterReturningLogLevel = LogLevel.DEBUG,
        afterThrowingLogLevel = LogLevel.ERROR,
        beforeMessage = "exporting text files",
        afterThrowingMessage = "failed to export text files"
    )
    private fun save(it: Map.Entry<String, List<Triple<String, ExportAttributeLocator, String>>>) =
        Either.catch {
            exportHandleFactory.create( //creates json contents.
                it.key,
                convertToJson(
                    it.value.map {
                        Pair(it.second, it.third)
                    }
                )
            )
        }
    
    private fun convertToJson(data: Iterable<Pair<ExportAttributeLocator, String>>): String {
        val json = Json {}

        val singleContents = data.filter {
            !it.first.isList
        }.associate {
            Pair(it.first.info.name, it.second)
        }

        val multiContents = data.filter {
            it.first.isList
        }.groupBy {
            it.first.info.name
        }.asIterable().associate {
            Pair(
                it.key,
                it.value.sortedBy { //sorts value by parsed results.
                    it.first.index.fold({ 0 }, { x -> x })
                }.map {
                    it.second
                }.toTypedArray()
            )
        }

        val multiMap = multiContents.map {
            Pair(it.key, json.encodeToJsonElement(serializer(Array<String>::class.java), it.value))
        }.toMap()

        val singleMap = singleContents.map {
            Pair(it.key, json.encodeToJsonElement(serializer(String::class.java), it.value))
        }.toMap()

        return Json.encodeToString(singleMap.plus(multiMap))
    }
}
