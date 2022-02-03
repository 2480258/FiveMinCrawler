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

package com.fivemin.core.engine.transaction

import arrow.core.Option
import arrow.core.filterOption
import arrow.core.toOption
import com.fivemin.core.engine.*
import java.net.URI
import java.util.*

class TagBuilder <in Trans : Transaction<Document>, out Document : Request>
(private val selector: Option<Iterable<TagSelector>>) {
    fun build(init: InitialTransaction<Request>): TagRepository {
        var ret = selector.fold({ listOf() }, {
            it.map {
                it.build(init.request.target)
            }
        }).filterOption()

        return TagRepositoryImpl(ret.toOption(), init.tags.toOption())
    }
}
data class TagSelector(val name: String, val regex: Regex, val flag: EnumSet<TagFlag>) {
    @OptIn(ExperimentalStdlibApi::class)
    fun build(uri: URI): Option<Tag> {

        return regex.find(uri.toString()).toOption().map {
            Tag(flag, name, it.value)
        }
    }
}
