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
import arrow.core.toOption
import com.fivemin.core.LoggerController
import com.fivemin.core.TaskDetachedException
import com.fivemin.core.engine.CrawlerTask4
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.Transaction
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before

@Aspect

class AopLogger {
    companion object {
        private val logger = LoggerController.getLogger("??")
    }
    @Suppress("unused")
    @Around("execution(* com.fivemin.core.engine.CrawlerTask*.proceed(..))")
    fun logCrawlerTask(joinPoint: ProceedingJoinPoint) : Any {
        val req = joinPoint.args.first() as Transaction<Request>
        
        logger.info(req.request, "starting task")
        val ret = joinPoint.proceed() as Either<Throwable, Any>
        
        
        val report = ret.fold({
            
            
            logger.error(req.request, "task ended with $it. You may check document number because this exception could be logged before. \nStackTrace is: ${it.stackTraceToString()}")
        }, {
            logger.info(req.request, "task ended")
        })
        return ret
    }
}