package core.initialize

import arrow.core.Option
import arrow.core.none
import core.engine.ConfigController
import core.engine.DirectIO
import core.engine.DirectIOImpl
import core.export.ConfigControllerImpl
import core.request.queue.DequeueOptimizationPolicy

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

    private fun build() : VirtualOption{
        var mef = MEFFactory(pluginDirectory)
        var srtf = SRTFFactory().create()
        var config = ConfigControllerImpl()
        var io = DirectIOImpl(config)
        var fac = JsonParserOptionFactory(F)
    }
}