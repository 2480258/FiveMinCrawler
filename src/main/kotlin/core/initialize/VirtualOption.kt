package core.initialize

import arrow.core.Option
import arrow.core.none
import core.engine.ConfigController
import core.engine.DirectIO
import core.engine.DirectIOImpl
import core.export.ConfigControllerImpl
import core.initialize.json.JsonParserOptionFactory
import core.request.queue.DequeueOptimizationPolicy
import java.io.File

data class VirtualOption(
    val parseOption: ParseOption,
    val controller: ConfigController,
    val directIO: DirectIO,
    val resumeOption: Option<ResumeOption>,
    val subPolicyCollection: SubPolicyCollection,
    val dequeue: DequeueOptimizationPolicy
)

class StartTaskOption(
    private val mainUriTarget: String,
    private val paramPath: String,
    private val pluginDirectory: Option<String> = none(),
    private val resumeAt: Option<String> = none()
) {
    fun run(){
        //TODO Log

        val crawlerFactory = CrawlerFactory()
    }

    private fun build() : VirtualOption {

        var file = File(paramPath)

        var mef = MEFFactory(pluginDirectory)

        var srtf = SRTFFactory().create()
        var config = ConfigControllerImpl()
        var io = DirectIOImpl(config)
        var fac = JsonParserOptionFactory(file.readText(), mef.)
    }
}