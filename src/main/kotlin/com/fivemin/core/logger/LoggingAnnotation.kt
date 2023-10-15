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
import com.fivemin.core.LoggerController
import com.fivemin.core.TaskDetachedException
import com.fivemin.core.engine.*
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.*

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Log(
    val beforeLogLevel: LogLevel = LogLevel.INFO,
    val afterReturningLogLevel: LogLevel = LogLevel.INFO,
    val afterThrowingLogLevel: LogLevel = LogLevel.WARN,
    val beforeMessage: String = "",
    val afterReturningMessage: String = "",
    val afterThrowingMessage: String = ""
)

@Suppress("unused")
@Aspect
class AnnotationLogger(private val logger: Logger = LoggerController.getLogger("")) {
    companion object {
        private val propertyExtractor = PropertyExtractor()
    }
    
    @Suppress("unused")
    @Before("@annotation(Log) && call(* *(..))")
    fun logBefore(
        joinPoint: JoinPoint,
        Log: Log
    ) {
        if (checksCoroutineBefore(joinPoint)) return
        
        val target = getObjectsFromJoinPoint(joinPoint)
        
        val objInfo = generateStandardLoggingMessageFromContext(target)
        val errInfo = generateErrorLoggingMessageFromContext(target)
        
        val msg = Log.beforeMessage.ifBlank {
            generateCallLocationMessage(joinPoint, LogLocation.BEFORE)
        }
        
        logIfTextIsNotBlank(Log.beforeLogLevel, "$msg $objInfo $errInfo")
    }
    
    private fun checksCoroutineBefore(joinPoint: JoinPoint): Boolean {
        val cc = joinPoint.args.firstOrNull {
            it != null
        }
        
        if ((cc != null) && cc::class.qualifiedName == null) {
            return true
        }
        return false
    }
    
    @Suppress("unused")
    @AfterReturning("@annotation(Log) && call(* *(..))", returning = "retVal")
    fun logAfterReturning(
        joinPoint: JoinPoint,
        Log: Log,
        retVal: Any?
    ) {
        if (checksCoroutineAfterReturning(retVal)) return
        
        if (retVal == null) {
            val msg = Log.afterReturningMessage.ifBlank {
                generateCallLocationMessage(joinPoint, LogLocation.AFTER_RETURNING)
            }
            
            logIfTextIsNotBlank(Log.afterReturningLogLevel, msg)
            
            return
        }
        
        
        val objInfo = generateStandardLoggingMessageFromContext(listOf(retVal))
        val errInfo = generateErrorLoggingMessageFromContext(listOf(retVal))
        
        if (errInfo.isNotBlank()) {
            val msg = Log.afterThrowingMessage.ifBlank {
                generateCallLocationMessage(joinPoint, LogLocation.AFTER_RETURNING)
            } + " (handled)"
            
            logIfTextIsNotBlank(Log.afterThrowingLogLevel, "$msg $objInfo $errInfo")
        } else {
            val msg = Log.afterReturningMessage.ifBlank {
                generateCallLocationMessage(joinPoint, LogLocation.AFTER_RETURNING)
            }
            
            logIfTextIsNotBlank(Log.afterReturningLogLevel, "$msg $objInfo $errInfo")
        }
    }
    
    private fun checksCoroutineAfterReturning(retVal: Any?): Boolean {
        if ((retVal != null) && retVal::class.qualifiedName?.lowercase()?.contains("coroutine") == true) {
            return true
        }
        return false
    }
    
    @Suppress("unused")
    @AfterThrowing("@annotation(Log) && call(* *(..))", throwing = "retVal")
    fun logAfterThrowing(
        joinPoint: JoinPoint,
        Log: Log,
        retVal: Throwable
    ) {
        val objInfo = generateStandardLoggingMessageFromContext(listOf(retVal))
        val errInfo = generateErrorLoggingMessageFromContext(listOf(retVal))
        
        val msg = Log.afterThrowingMessage.ifBlank {
            generateCallLocationMessage(joinPoint, LogLocation.AFTER_THROWING)
        }
        
        logIfTextIsNotBlank(Log.afterThrowingLogLevel, "$msg | $objInfo $errInfo")
        
        throw retVal
    }
    
    private fun generateCallLocationMessage(joinPoint: JoinPoint, logLocation: LogLocation): String {
        
        val callLoc = "${joinPoint.signature.declaringTypeName}.${joinPoint.signature.name}"
        
        return "$callLoc || $logLocation"
    }
    
    private fun generateErrorLoggingMessageFromContext(joinPoint: List<Any?>): String {
        
        val santilizedJoinPoint = joinPoint.filterNotNull().filter {
            it !is TaskDetachedException
        }
        
        val firstEither =
            findAndGetFirstOrNull<Either<Throwable, Any?>>( // no support for multiple either due to performance reasons
                santilizedJoinPoint,
                Either::class.createType(
                    listOf(
                        KTypeProjection(KVariance.OUT, Throwable::class.starProjectedType),
                        KTypeProjection(KVariance.OUT, Any::class.starProjectedType.withNullability(true))
                    )
                )
            ) // types which has no upper bound actually has Any? as upper bound.
        
        val mappedEither = firstEither?.fold({ it.stackTraceToString() }, {
            it?.let {
                generateErrorLoggingMessageFromContext(listOf(it))
            }
        })
        
        val objList = listOf(
            findAndGetFirstOrNull(santilizedJoinPoint, Throwable::class)?.stackTraceToString(),
            mappedEither
        )
        
        
        return objList.filterNotNull().joinToString(" ")
    }
    
    private fun generateStandardLoggingMessageFromContext(joinPoint: List<Any?>): String {
        
        val santilizedJoinPoint = joinPoint.filterNotNull().filter {
            it !is TaskDetachedException
        }
        val firstEither =
            findAndGetFirstOrNull<Either<Throwable, Any?>>( // no support for multiple either due to performance reasons
                santilizedJoinPoint,
                Either::class.createType(
                    listOf(
                        KTypeProjection(KVariance.OUT, Throwable::class.starProjectedType),
                        KTypeProjection(KVariance.OUT, Any::class.starProjectedType.withNullability(true))
                    )
                )
            ) // types which has no upper bound actually has Any? as upper bound.
        
        val firstOption =
            findAndGetFirstOrNull<Option<Any?>>( // no support for multiple options due to performance reasons
                santilizedJoinPoint,
                Option::class.createType(
                    listOf(
                        KTypeProjection(KVariance.OUT, Any::class.starProjectedType.withNullability(true))
                    )
                )
            ) // types which has no upper bound actually has Any? as upper bound.
        
        val mappedEither = firstEither?.fold({ "" }, {
            it?.let {
                generateStandardLoggingMessageFromContext(listOf(it))
            }
        })
        
        val mappedOption = firstOption?.fold({ "" }, {
            it?.let {
                generateStandardLoggingMessageFromContext(listOf(it))
            }
        })
        
        val objList = listOf(
            findAndGetFirstOrNull(santilizedJoinPoint, Request::class).gdi(),
            findAndGetFirstOrNull(santilizedJoinPoint, SessionToken::class).gdi(),
            findAndGetFirstOrNull(santilizedJoinPoint, UniqueKey::class).gdi(),
            findAndGetFirstOrNull(santilizedJoinPoint, UniqueKeyToken::class).gdi(),
            findAndGetFirstOrNull(santilizedJoinPoint, FileIOToken::class).gdi(),
            findAndGetFirstOrNull(santilizedJoinPoint, ExportHandle::class).gdi(),
            findAndGetFirstOrNull(santilizedJoinPoint, DocumentAttributeInfo::class).gdi(),
            mappedEither,
            mappedOption
        )
        
        val tstr = "(toString() of return value (may repeat 2 or more times): ${
            santilizedJoinPoint.map { it?.toString() ?: "null" }.joinToString(" ")
        })" // it can be null
        
        return objList.filterNotNull().joinToString(" ")
    }
    
    
    private fun getObjectsFromJoinPoint(joinPoint: JoinPoint): List<Any> {
        val objects = mutableListOf<Any>()
        objects.addAll(joinPoint.args)
        
        
        if (joinPoint.target != null) { // this can be null.... by test
            objects.add(joinPoint.target)
        }
        
        return objects
    }
    
    private inline fun <reified T : Any> findAndGetFirstOrNull(
        objs: List<Any>,
        wantType: KClass<T>,
        genericTypes: List<KTypeProjection> = listOf()
    ): T? {
        return findAndGetFirstOrNull(objs, wantType.createType(genericTypes))
    }
    
    private inline fun <reified T : Any> findAndGetFirstOrNull(objs: List<Any>, wantType: KType): T? {
        
        
        val ret = objs.firstNotNullOfOrNull {
            propertyExtractor.find<T>(it, wantType)
        }
        
        return ret
    }
    
    private fun logIfTextIsNotBlank(level: LogLevel, text: String) {
        val logger = when (level) {
            LogLevel.ERROR -> logger::error
            LogLevel.WARN -> logger::warn
            LogLevel.INFO -> logger::info
            LogLevel.DEBUG -> logger::debug
            LogLevel.TRACE -> logger::trace
        }
        
        if(text.isNotBlank()) {
            logger(text)
        }
    }
    
    private fun Request?.gdi(): String? {
        return this?.let {
            "${it.getDebugInfo()} <<"
        }
    }
    
    private fun SessionToken?.gdi(): String? {
        return this?.let {
            "S[${it.tokenNumber}] <<"
        }
    }
    
    private fun UniqueKey?.gdi(): String? {
        return this?.let {
            "$it <<"
        }
    }
    
    private fun UniqueKeyToken?.gdi(): String? {
        return this?.let {
            "U[${it.tokenNumber}] <<"
        }
    }
    
    private fun FileIOToken?.gdi(): String? {
        return this?.let {
            "F[${it.fileName.name.name}] <<"
        }
    }
    
    private fun ExportHandle?.gdi(): String? {
        return this?.let {
            "${it.request.token.fileName.name.name} <<"
        }
    }
    
    private fun DocumentAttributeInfo?.gdi(): String? {
        return this?.let {
            "${it.name} <<"
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