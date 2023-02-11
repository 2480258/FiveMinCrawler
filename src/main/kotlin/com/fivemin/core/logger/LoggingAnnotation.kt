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

import com.fivemin.core.LoggerController
import com.fivemin.core.engine.Request
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import java.util.LinkedList
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.*

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Log(
    val logLevel: LogLevel,
    val logWhen: LogWhen,
    val message: String = "",
    val name_Transaction: String = "",
    val name_Request: String = "",
    val name_UniqueKey: String = "",
    val name_SessionInfo: String = "",
    val log_return_either_throwable_any: Boolean = false
)

@Suppress("unused")
@Aspect
class AnnotationLogger {
    companion object {
        private val logger = LoggerController.getLogger("CrawlerTask")
        
        private val memo =
            mutableMapOf<Pair<KProperty1<Any, Any>, KClass<Any>>, List<String>>() // Map<Pair<ThisObject, WantToFind>, PropertyNames>>
    }
    
    @Suppress("unused")
    @Before("@annotation(Log) && execution(* *(..))")
    fun logBeforeWithDebugLevelWithTransaction(
        joinPoint: JoinPoint,
        Log: Log
    ) {
    
    }
}

enum class LogLevel {
    ERROR,
    WARN,
    INFO,
    DEBUG,
    TRACE
}

enum class LogWhen(val value: Int) {
    BEFORE(0b1),
    AFTER_RETURNING(0b10),
    AFTER_THROWING(0b100);
    
    private fun hasSomething(arg: LogWhen) = (value and arg.value) == arg.value
    
    fun hasBefore() = hasSomething(BEFORE)
    
    fun hasAfterReturning() = hasSomething(AFTER_RETURNING)
    
    fun hasAfterThrowing() = hasSomething(AFTER_THROWING)
}
