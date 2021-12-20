package fivemin.core

import fivemin.core.logger.LoggerImpl

interface Logger {
    fun info(str : String)

    fun debug(str : String)

    fun warn(str: String)

    fun error(str : String)

}

class LoggerController {
    companion object {
        fun getLogger(name : String) : Logger{
            return LoggerImpl(name)
        }
    }
}