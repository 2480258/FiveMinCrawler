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

import arrow.core.toOption
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.initialize.CrawlerFactory
import com.fivemin.core.initialize.StartTaskOption
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.InvalidObjectException

class MainKt {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                val options = parseOptions(args)
                CrawlerFactory().get(options).startAndWaitUntilFinish { taskFactory, document, info, state ->
                    val task = taskFactory.getFactory()
                        .get4<
                                InitialTransaction<Request>,
                                PrepareTransaction<Request>,
                                FinalizeRequestTransaction<Request>,
                                SerializeTransaction<Request>,
                                ExportTransaction<Request>>(
                            DocumentType.DEFAULT
                        )
                    
                    runBlocking {
                        task.start(document, info, state)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            kotlin.system.exitProcess(0)
        }
        
        fun parseOptions(args: Array<String>): StartTaskOption {
            val logger = LoggerController.getLogger("MainKt")
            logger.info("Starting crawler")
            
            
            val parser = ArgParser("FiveMinCrawler")
            
            val uri by parser.option(ArgType.String, shortName = "u", description = "crawl uri")
            val paramPath by parser.option(ArgType.String, shortName = "p", description = "parameter path")
            val pluginPath by parser.option(ArgType.String, shortName = "g", description = "(Optional) plugin path")
            val resumeFrom by parser.option(
                ArgType.String,
                shortName = "r",
                description = "(Optional) resume file path"
            )
            val rootPath by parser.option(ArgType.String, shortName = "o", description = "(Optional) path to write")
            val argsText by parser.option(
                ArgType.String,
                shortName = "a",
                description = "(Debug) add argument via text file"
            )
            val useVerbose by parser.option(ArgType.Boolean, shortName = "v", description = "use verbose logging")
                .default(false)
            
            parser.parse(args)
            
            if (argsText != null) {
                return parseOptions(File(argsText).readText().split(' ').toTypedArray())
            } else {
                if (useVerbose) {
                    System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
                    logger.debug("Logging Level = TRACE")
                } else {
                    System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO")
                }
                
                if (uri == null || paramPath == null) {
                    logger.error("please check your arguments.... exiting.")
                    throw InvalidObjectException("can not parse arguments")
                }
                
                return StartTaskOption(
                    uri!!,
                    paramPath!!,
                    pluginPath.toOption(),
                    resumeFrom.toOption(),
                    rootPath.toOption()
                )
            }
        }
    }
}
