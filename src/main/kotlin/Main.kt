import arrow.core.toOption
import com.fivemin.core.LoggerController
import com.fivemin.core.initialize.StartTaskOption
import kotlinx.cli.*
import kotlinx.coroutines.runBlocking
import java.io.File

class MainKt {
    companion object {
        private val logger = LoggerController.getLogger("MainKt")


        @JvmStatic
        fun main(args: Array<String>) {
            logger.debug("Logging Level = Debug")
            System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
    
            runBlocking {
                try {
                    logger.info("Starting crawler")
        
                    start(args)
                } catch (e : Exception) {
                    println(e.printStackTrace())
                }
    
                logger.info("Finished")
    
                kotlin.system.exitProcess(0)
            }
        }

        suspend fun start(args: Array<String>) {


            val parser = ArgParser("example")
            val uri by parser.option(ArgType.String, shortName = "u", description = "crawl uri")
            val paramPath by parser.option(ArgType.String, shortName = "p", description = "parameter path")

            val pluginPath by parser.option(ArgType.String, shortName = "g", description = "(Optional) plugin path")
            val resumeFrom by parser.option(ArgType.String, shortName = "r", description = "(Optional) resume file path")
            val rootPath by parser.option(ArgType.String, shortName = "o", description = "(Optional) path to write")
            val argsText by parser.option(ArgType.String, shortName = "a", description = "(Debug) resume file path")

            parser.parse(args)

            if(argsText != null) {
                start(File(argsText).readText().split(' ').toTypedArray())
            } else {
                val opt = StartTaskOption(uri!!, paramPath!!, pluginPath.toOption(), resumeFrom.toOption(), rootPath.toOption())

                opt.run()
            }
        }
    }
}