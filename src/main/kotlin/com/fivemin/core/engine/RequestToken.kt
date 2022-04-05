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

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

data class RequestToken private constructor(val tokenNumber: Int): Comparable<RequestToken> {
    companion object {
        var LastUsed: Int = -1
        val lock = ReentrantLock()
    
        /**
         * Creates unique token for request object.
         */
        fun create(): RequestToken {
            return lock.withLock {
                LastUsed++
                return RequestToken(LastUsed)
            }
        }
    }
    
    //provided total ordering for performance
    override fun compareTo(other: RequestToken): Int {
        return tokenNumber.compareTo(other.tokenNumber)
    }
}
