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

package com.fivemin.core

import arrow.core.Either
import arrow.core.Option
import arrow.core.none
import com.fivemin.core.engine.Request
import com.fivemin.core.logger.LoggerImpl

interface Logger {
    fun info(ex: Throwable, str: String? = null)

    fun debug(ex: Throwable, str: String? = null)

    fun warn(ex: Throwable, str: String? = null)

    fun error(ex: Throwable, str: String? = null)

    fun info(str: String)

    fun debug(str: String)

    fun warn(str: String)

    fun error(str: String)

    fun info(req: Request, str: String, e: Option<Throwable> = none())

    fun debug(req: Request, str: String, e: Option<Throwable> = none())

    fun warn(req: Request, str: String, e: Option<Throwable> = none())

    fun error(req: Request, str: String, e: Option<Throwable> = none())
    
    fun debug(either: Either<Throwable, Any?>, str: String)
}

class LoggerController {
    companion object {
        fun getLogger(name: String): Logger {
            return LoggerImpl(name)
        }
    }
}
