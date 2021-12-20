import arrow.core.toOption
import fivemin.core.LoggerController
import fivemin.core.initialize.StartTaskOption
import kotlinx.cli.*
import java.io.File

class MainKt {
    companion object {
        private val logger = LoggerController.getLogger("MainKt")


        @JvmStatic
        fun main(args: Array<String>) {
            try {
                logger.debug("Starting crawler")

                start(args)
            } catch (e : Exception) {
                println(e.printStackTrace())
            }

            logger.info("Finished")
        }

        fun start(args: Array<String>) {


            val parser = ArgParser("example")
            val uri by parser.option(ArgType.String, shortName = "u", description = "crawl uri")
            val paramPath by parser.option(ArgType.String, shortName = "p", description = "parameter path")

            val pluginPath by parser.option(ArgType.String, shortName = "g", description = "(Optional) plugin path")
            val resumeFrom by parser.option(ArgType.String, shortName = "r", description = "(Optional) resume file path")
            val argsText by parser.option(ArgType.String, shortName = "a", description = "(Debug) resume file path")

            parser.parse(args)

            if(argsText != null) {
                start(File(argsText).readText().split(' ').toTypedArray())
            } else {
                val opt = StartTaskOption(uri!!, paramPath!!, pluginPath.toOption(), resumeFrom.toOption())

                opt.run()
            }
        }
    }
}