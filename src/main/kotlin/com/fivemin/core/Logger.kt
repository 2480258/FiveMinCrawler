package com.fivemin.core

import arrow.core.Option
import arrow.core.none
import com.fivemin.core.engine.Request
import com.fivemin.core.logger.LoggerImpl

interface Logger {
    fun info(str: String)

    fun debug(str: String)

    fun warn(str: String)

    fun error(str: String)

    fun info(req: Request, str: String, e: Option<Throwable> = none())

    fun debug(req: Request, str: String, e: Option<Throwable> = none())

    fun warn(req: Request, str: String, e: Option<Throwable> = none())

    fun error(req: Request, str: String, e: Option<Throwable> = none())
}

class LoggerController {
    companion object {
        fun getLogger(name: String): Logger {
            return LoggerImpl(name)
        }
    }
}
