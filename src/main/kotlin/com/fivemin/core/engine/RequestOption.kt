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

import arrow.core.Option
import arrow.core.Some

class RequestOption(val preference: RequesterPreference)

class RequesterPreference(val engine: RequesterEngineInfo, val slot: Option<RequesterSlotInfo>) {
    fun withSlot(nslot: RequesterSlotInfo): RequesterPreference {
        return RequesterPreference(engine, Some(nslot))
    }
}

class RequesterEngineInfo(val name: String) {
    override fun equals(other: Any?): Boolean {
        if (other != null && other is RequesterEngineInfo) {
            return other.name == name
        }

        return false
    }

    override fun toString(): String {
        return "Engine: $name"
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

class RequesterSlotInfo(val index: Int) {
    override fun equals(other: Any?): Boolean {
        if (other != null && other is RequesterSlotInfo) {
            return other.index == index
        }

        return false
    }

    override fun toString(): String {
        return "Index: $index"
    }

    override fun hashCode(): Int {
        return index
    }
}
