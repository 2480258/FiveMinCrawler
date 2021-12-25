package fivemin.core.logger

import fivemin.core.Logger
import mu.KotlinLogging

class LoggerImpl(private val name : String) : Logger {
    private val logger = KotlinLogging.logger(name)

    init {
    }
    
    
    override fun info(str : String) {
        logger.info {
            str
        }
    }

    override fun debug(str : String) {
        logger.info {
            str
        }
    }

    override fun warn(str: String) {
        logger.warn {
            str
        }
    }

    override fun error(str : String) {
        logger.error {
            str
        }
    }
}