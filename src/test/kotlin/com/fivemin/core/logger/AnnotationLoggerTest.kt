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

import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.RequestType
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.aspectj.lang.JoinPoint
import org.testng.Assert.*
import org.testng.annotations.Test
import java.net.URI
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties

class AnnotationLoggerTest {
    class AOPTestA(val req: Request)
    
    class AOPTestB()
    
    
    @Test
    fun testLogBeforeWithThis() {
        val hooked = AOPTestA(DocumentMockFactory.getRequest(URI("URI"), RequestType.LINK))
        
        val spyLogger = spyk(LoggerController.getLogger("CrawlerTask"))
        val logger = AnnotationLogger(spyLogger)
        val joinPoint: JoinPoint = mockk()
        
        every {
            joinPoint.`this`
        } returns (hooked)
        
        every {
            joinPoint.args
        } returns (arrayOf())
        
        every {
            joinPoint.signature.name
        } returns ("SIG")
        
        every {
            joinPoint.signature.declaringTypeName
        } returns ("DTN")
        
        val annotation = Log(LogLevel.ERROR, "MSG")
        
        logger.logBefore(joinPoint, annotation)
        
        verify {
            spyLogger.error(withArg<String> {
                assert(it.contains("DTN") and it.contains("SIG") and it.contains("BEFORE") and it.contains("URI") and it.contains("MSG"))
            })
        }
    }
    
    @Test
    fun testLogBeforeWithArgs() {
        val hooked = AOPTestB()
        
        val spyLogger = spyk(LoggerController.getLogger("CrawlerTask"))
        val logger = AnnotationLogger(spyLogger)
        val joinPoint: JoinPoint = mockk()
        
        every {
            joinPoint.`this`
        } returns (hooked)
        
        every {
            joinPoint.args
        } returns (arrayOf(DocumentMockFactory.getRequest(URI("URI"), RequestType.LINK)))
        
        every {
            joinPoint.signature.name
        } returns ("SIG")
        
        every {
            joinPoint.signature.declaringTypeName
        } returns ("DTN")
        
        val annotation = Log(LogLevel.ERROR, "MSG")
        
        logger.logBefore(joinPoint, annotation)
        
        verify {
            spyLogger.error(withArg<String> {
                assert(it.contains("DTN") and it.contains("SIG") and it.contains("BEFORE") and it.contains("URI") and it.contains("MSG"))
            })
        }
    }
    
    @Test
    fun testLogAfterReturningValue() {
        val hooked = AOPTestB()
    
        val spyLogger = spyk(LoggerController.getLogger("CrawlerTask"))
        val logger = AnnotationLogger(spyLogger)
        val joinPoint: JoinPoint = mockk()
    
        every {
            joinPoint.`this`
        } returns (hooked)
    
        every {
            joinPoint.args
        } returns (arrayOf())
    
        every {
            joinPoint.signature.name
        } returns ("SIG")
    
        every {
            joinPoint.signature.declaringTypeName
        } returns ("DTN")
    
        val annotation = Log(LogLevel.ERROR, "MSG")
    
        logger.logAfterReturning(joinPoint, annotation, DocumentMockFactory.getRequest(URI("URI"), RequestType.LINK))
    
        verify {
            spyLogger.error(withArg<String> {
                assert(it.contains("DTN") and it.contains("SIG") and it.contains("AFTER_RETURNING") and it.contains("URI") and it.contains("MSG"))
            })
        }
    }
    
    @Test
    fun testLogAfterThrowing() {
        val hooked = AOPTestB()
    
        val spyLogger = spyk(LoggerController.getLogger("CrawlerTask"))
        val logger = AnnotationLogger(spyLogger)
        val joinPoint: JoinPoint = mockk()
    
        every {
            joinPoint.`this`
        } returns (hooked)
    
        every {
            joinPoint.args
        } returns (arrayOf())
    
        every {
            joinPoint.signature.name
        } returns ("SIG")
    
        every {
            joinPoint.signature.declaringTypeName
        } returns ("DTN")
    
        val annotation = Log(LogLevel.ERROR, "MSG")
    
        logger.logAfterThrowing(joinPoint, annotation, NullPointerException())
    
        verify {
            spyLogger.error(withArg<String> {
                assert(it.contains("DTN") and it.contains("SIG") and it.contains("AFTER_THROWING") and it.contains("NullPointerException") and it.contains("MSG"))
            })
        }
    }
    
}