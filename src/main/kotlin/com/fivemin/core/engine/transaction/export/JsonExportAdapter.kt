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
import com.fivemin.core.engine.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.util.*

class JsonExportAdapter(private val fileNameExp: TagExpression, private val exportHandleFactory: ExportHandleFactory) :
    ExportAdapter {
    override fun parse(
        request: Request,
        info: Iterable<ExportAttributeInfo>
    ): Iterable<Either<Throwable, ExportHandle>> {

        var ret = info.map { x ->
            x.element.match({ y ->
                Triple(x.tagRepo, x.info, y.body)
            }, { null })
        }

        var tags = ret.filter {
            it != null && it.second.isList
        }.map { x ->
            Tag(EnumSet.of(TagFlag.NONE), x!!.second.info.name, x.third)
        }

        var tagrepo = ret.map {
            Triple(fileNameExp.build(TagRepositoryImpl(tags.toOption(), it!!.first.toOption())), it.second, it.third)
        }.groupBy {
            it.first
        }

        return tagrepo.map {
            Either.catch {
                exportHandleFactory.create(
                    it.key,
                    convertToJson(
                        it.value.map {
                            Pair(it.second, it.third)
                        }
                    )
                )
            }
        }
    }

    private fun convertToJson(data: Iterable<Pair<ExportAttributeLocator, String>>): String {
        val json = Json {}

        var single = data.filter {
            !it.first.isList
        }.associate {
            Pair(it.first.info.name, it.second)
        }

        var multi = data.filter {
            it.first.isList
        }.groupBy {
            it.first.info.name
        }.asIterable().associate {
            Pair(
                it.key,
                it.value.sortedBy {
                    it.first.index.fold({ 0 }, { x -> x })
                }.map {
                    it.second
                }.toTypedArray()
            )
        }

        var multiMap = multi.map {
            Pair(it.key, json.encodeToJsonElement(serializer(Array<String>::class.java), it.value))
        }.toMap()

        var singleMap = single.map {
            Pair(it.key, json.encodeToJsonElement(serializer(String::class.java), it.value))
        }.toMap()

        return Json.encodeToString(singleMap.plus(multiMap))
    }
}
