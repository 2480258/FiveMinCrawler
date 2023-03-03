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

package com.fivemin.core.logger

import arrow.core.Either
import arrow.core.Option
import com.fivemin.core.Logger
import com.fivemin.core.engine.Request
import mu.KotlinLogging

class LoggerImpl(private val name: String) : Logger {
    private val logger = KotlinLogging.logger(name)

    init {
    }

    override fun info(ex: Throwable, str: String?) {
        logger.info(ex) { str }
    }

    override fun info(str: String) {
        logger.info {
            str
        }
    }

    override fun info(req: Request, str: String, e: Option<Throwable>) {
        val logStr = req.getDebugInfo() + " < " + str

        e.fold({ logger.info(logStr) }) {
            logger.info(it) { logStr }
        }
    }
    
    override fun debug(ex: Throwable, str: String?) {
        logger.debug(ex) { str }
    }

    override fun debug(str: String) {
        logger.debug {
            str
        }
    }

    override fun debug(req: Request, str: String, e: Option<Throwable>) {
        val logStr = req.getDebugInfo() + " < " + str

        e.fold({ logger.debug(logStr) }) {
            logger.debug(it) { logStr }
        }
    }
    
    override fun debug(either: Either<Throwable, Any?>, str: String) {
        either.swap().map {
            debug(it.message ?: "null")
            debug(it.stackTraceToString())
        }
    }
    
    override fun warn(ex: Throwable, str: String?) {
        logger.warn(ex) { str }
    }

    override fun warn(str: String) {
        logger.warn {
            str
        }
    }

    override fun warn(req: Request, str: String, e: Option<Throwable>) {
        val logStr = req.getDebugInfo() + " < " + str

        e.fold({ logger.warn(logStr) }) {
            logger.warn(it) { logStr }
        }
    }
    
    override fun error(ex: Throwable, str: String?) {
        logger.error(ex) { str }
    }

    override fun error(str: String) {
        logger.error {
            str
        }
    }

    override fun error(req: Request, str: String, e: Option<Throwable>) {
        val logStr = req.getDebugInfo() + " < " + str

        e.fold({ logger.error(logStr) }) {
            logger.error(it) { logStr }
        }
    }
    
    override fun trace(str: String) {
        logger.trace {
            str
        }
    }
}
