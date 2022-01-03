package com.fivemin.core.logger

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
}
