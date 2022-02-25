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

package com.fivemin.core.engine

import java.util.*

data class Tag(val flag: EnumSet<TagFlag>, val name: String, val value: String) {
    private fun checkFlag(ref: TagFlag): Boolean {
        return flag.contains(ref)
    }
    
    /**
     * Returns if tag is alias.
     * This means tags is used to check conflict with URL.
     */
    val isAlias: Boolean
        get() {
            return checkFlag(TagFlag.ALIAS)
        }

    val isUnique: Boolean
        get() {
            return checkFlag(TagFlag.UNIQUE)
        }

    val convertToAttribute: Boolean
        get() {
            return checkFlag(TagFlag.CONVERT_TO_ATTRIBUTE)
        }
}

enum class TagFlag(val tag: Number) {
    NONE(0), UNIQUE(1), ALIAS(0b100), CONVERT_TO_ATTRIBUTE(0b1000)
}
