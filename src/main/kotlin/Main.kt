import arrow.core.toOption
import fivemin.core.initialize.StartTaskOption
import kotlinx.cli.*
import java.io.File

class MainKt {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            start(args)
        }

        fun start(args: Array<String>) {
            val parser = ArgParser("example")
            val uri by parser.option(ArgType.String, shortName = "u", description = "crawl uri").required()
            val paramPath by parser.option(ArgType.String, shortName = "p", description = "parameter path").required()

            val pluginPath by parser.option(ArgType.String, shortName = "g", description = "(Optional) plugin path")
            val resumeFrom by parser.option(ArgType.String, shortName = "r", description = "(Optional) resume file path")
            val argsText by parser.option(ArgType.String, shortName = "a", description = "(Debug) resume file path")

            parser.parse(args)

            if(argsText != null) {
                start(File(argsText).readText().split(' ').toTypedArray())
            } else {
                val opt = StartTaskOption(uri, paramPath, pluginPath.toOption(), resumeFrom.toOption())

                opt.run()
            }
        }
    }
}