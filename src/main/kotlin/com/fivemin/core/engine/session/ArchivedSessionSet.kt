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
import kotlinx.serialization.Serializable


/**
 * Saves a set of ArchivedSession.
 * Note: A Session means one request and its normal, retry, redirect and other(started by this) request URL.
 */
@Serializable
data class ArchivedSessionSet constructor(private val set: List<ArchivedSession>) {
    /**
     * Check if UniqueKey is in this set. O(N).
     * TODO: Performance Optimization Required.
     */
    fun isConflict(key: UniqueKey): Boolean {
        return set.any { it -> it.isConflict(key) }
    }
}

/**
 * Saves a set of UniqueKey.
 * Note: A Session means one request and its normal, retry, redirect and other(started by this) request URL.
 */
@Serializable
data class ArchivedSession constructor(private val set: List<UniqueKey>) {
    /**
     * Check if UniqueKey is in this set. O(N).
     * TODO: Performance Optimization Required.
     */
    fun isConflict(key: UniqueKey): Boolean {
        return set.any {
            it.equals(key)
        }
    }
}
