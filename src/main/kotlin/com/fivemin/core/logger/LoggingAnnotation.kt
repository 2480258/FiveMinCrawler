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
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import kotlin.reflect.KClass
import kotlin.reflect.full.*

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Log(
    val logLevel: LogLevel,
    val logWhen: LogWhen,
    val message: String = ""
)

@Suppress("unused")
@Aspect
class AnnotationLogger {
    companion object {
        private val logger = LoggerController.getLogger("CrawlerTask")
        
        private val propertyExtractor = PropertyExtractor()
    }
    
    @Suppress("unused")
    @Before("@annotation(Log) && execution(* *(..))")
    fun logBefore(
        joinPoint: JoinPoint,
        Log: Log
    ) {
        val msg = generateStandardLoggingMessageFromContext(joinPoint)
        getLoggerPerLogLevel(Log.logLevel)(msg)
    }
    
    @Suppress("unused")
    @AfterReturning("@annotation(Log) && execution(* *(..))", returning = "retVal")
    fun logAfterReturning(
        joinPoint: JoinPoint,
        Log: Log,
        retVal: Any
    ) {
        val msg = generateStandardLoggingMessageFromContext(joinPoint)
        getLoggerPerLogLevel(Log.logLevel)(msg)
    }
    
    private fun generateCallLocationMessage(joinPoint: JoinPoint, logLocation: LogLocation): String {
        
        val callLoc = "${joinPoint.signature.declaringTypeName}.${joinPoint.signature.name}"
        
        return "$callLoc || $logLocation"
    }
    
    private fun generateLoggingMessageFromReturning(retVal: Any): String {
        if (retVal as? Either<Throwable, Any> != null) {
            val msg = retVal.fold({
                it.stackTraceToString()
            }, {
                generateLoggingMessageFromReturning(it)
            })
            
            return msg
        }
        
        val types = listOf<KClass<Any>>(
            Request::class as KClass<Any>,
            SessionToken::class as KClass<Any>,
            UniqueKey::class as KClass<Any>,
            UniqueKeyToken::class as KClass<Any>,
            FileIOToken::class as KClass<Any>,
            ExportHandle::class as KClass<Any>
        )
        
        var obj: Any? = null
        var count = 0
        
        while ((obj == null) && (count < types.size)) {
            obj = getObjectFromReturning(retVal, types[count])
            count++
        }
        
        val ret = when(obj) {
            (obj is Request) -> (obj as Request).gdi()
            (obj is SessionToken) -> (obj as SessionToken).gdi()
            (obj is UniqueKey) -> (obj as UniqueKey).gdi()
            (obj is UniqueKeyToken) -> (obj as UniqueKeyToken).gdi()
            (obj is FileIOToken) -> (obj as FileIOToken).gdi()
            (obj is ExportHandle) -> (obj as ExportHandle).gdi()
            (obj is Throwable) -> (obj as Throwable).stackTraceToString()
            else -> ""
        }
        
        return ret.orEmpty()
    }
    
    private fun generateStandardLoggingMessageFromContext(joinPoint: JoinPoint): String {
        val objList = listOf(
            getObjectFromContext(joinPoint, Request::class).gdi(),
            getObjectFromContext(joinPoint, SessionToken::class).gdi(),
            getObjectFromContext(joinPoint, UniqueKey::class).gdi(),
            getObjectFromContext(joinPoint, UniqueKeyToken::class).gdi(),
            getObjectFromContext(joinPoint, FileIOToken::class).gdi(),
            getObjectFromContext(joinPoint, ExportHandle::class).gdi()
        )
        
        return objList.filterNotNull().joinToString(" ")
    }
    
    private inline fun <reified T : Any> getObjectFromContext(joinPoint: JoinPoint, wantType: KClass<T>): T? {
        val objects = mutableListOf<Any>()
        objects.addAll(joinPoint.args)
        objects.add(joinPoint.`this`)
        
        var count = 0
        var ret: T? = null
        
        while ((ret == null) && (count < objects.size)) {
            ret = propertyExtractor.find(objects[count], wantType)
            count++
        }
        
        return ret
    }
    
    private inline fun <reified T : Any> getObjectFromReturning(retVal: Any, wantType: KClass<T>): T? {
        return propertyExtractor.find(retVal, wantType)
    }
    
    private fun getLoggerPerLogLevel(level: LogLevel): (String) -> Unit {
        return when (level) {
            LogLevel.ERROR -> logger::error
            LogLevel.WARN -> logger::warn
            LogLevel.INFO -> logger::info
            LogLevel.DEBUG -> logger::debug
            LogLevel.TRACE -> logger::trace
        }
    }
    
    private fun Request?.gdi(): String? {
        return this?.let {
            "${it.getDebugInfo()} <<"
        }
    }
    
    private fun SessionToken?.gdi(): String? {
        return this?.let {
            "[S${it.tokenNumber}] <<"
        }
    }
    
    private fun UniqueKey?.gdi(): String? {
        return this?.let {
            "$it <<"
        }
    }
    
    private fun UniqueKeyToken?.gdi(): String? {
        return this?.let {
            "[U${it.tokenNumber}] <<"
        }
    }
    
    private fun FileIOToken?.gdi(): String? {
        return this?.let {
            "$[F${it.fileName.name}] <<"
        }
    }
    
    private fun ExportHandle?.gdi(): String? {
        return this?.let {
            "${it.request.token.fileName.name} <<"
        }
    }
}

enum class LogLevel {
    ERROR,
    WARN,
    INFO,
    DEBUG,
    TRACE
}

enum class LogLocation {
    BEFORE,
    AFTER_RETURNING,
    AFTER_THROWING
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