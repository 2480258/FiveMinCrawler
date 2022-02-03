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

import com.fivemin.core.UniqueKeySerializer
import com.fivemin.core.engine.*
import kotlinx.serialization.Serializable
import java.net.URI

@Serializable(with = UniqueKeySerializer::class)
data class StringUniqueKey(val src: String) : UniqueKey() {
    override fun eq(key: UniqueKey): Boolean {
        if (key is StringUniqueKey) {
            return src == key.src
        }

        return false
    }

    override fun hash(): Int {
        return src.hashCode()
    }

    override fun toStr(): String {
        return src
    }
}

@Serializable(with = UniqueKeySerializer::class)
data class UriUniqueKey(val uri: URI) : UniqueKey() {
    override fun eq(key: UniqueKey): Boolean {
        if (key is UriUniqueKey) {
            return uri == key.uri
        }

        return false
    }

    override fun hash(): Int {
        return uri.hashCode()
    }

    override fun toStr(): String {
        return uri.toString()
    }
}

class StringUniqueKeyProvider : TagUniqueKeyProvider {
    override fun create(doc: TagRepository): Iterable<UniqueKey> {
        return doc.filter { x -> x.isAlias }.map { x -> StringUniqueKey(x.value) }
    }
}

class UriUniqueKeyProvider : DocumentUniqueKeyProvider {
    override fun <Document : Request> create(doc: Document): UniqueKey {
        return UriUniqueKey(doc.target)
    }
}
