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

package com.fivemin.core.engine.session

import com.fivemin.core.engine.UniqueKey
import java.net.URI

/**
 * Represent UniqueKey for URL.
 */
class UriUniqueKey constructor(private val value: URI) : UniqueKey() {
    override fun equals(other: Any?): Boolean {
        if (other != null && other is UniqueKey) {
            return eq(other)
        }

        return false
    }

    override fun eq(key: UniqueKey): Boolean {
        if (key is UriUniqueKey) {
            return value == key.value
        }

        return false
    }

    override fun hash(): Int {
        return value.hashCode()
    }

    override fun toStr(): String {
        return value.toString()
    }
    
    override fun hashCode(): Int {
        return hash()
    }
}
