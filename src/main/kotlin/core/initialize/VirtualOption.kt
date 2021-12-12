package core.initialize

import arrow.core.Option
import arrow.core.flatten
import arrow.core.none
import arrow.core.toOption
import core.engine.*
import core.export.ConfigControllerImpl
import core.initialize.json.JsonParserOptionFactory
import core.request.queue.DequeueOptimizationPolicy
import java.io.File
import java.net.URI

data class VirtualOption(
    val parseOption: ParseOption,
    val controller: ConfigController,
    val directIO: DirectIO,
    val resumeOption: Option<ResumeOption>,
    val subPolicyCollection: SubPolicyCollection,
    val dequeue: DequeueOptimizationPolicy
)

class StartTaskOption(
    val mainUriTarget: String,
    val paramPath: String,
    val pluginDirectory: Option<String> = none(),
    val resumeAt: Option<String> = none()
) {

    private val resume: ResumeDataFactory = ResumeDataFactory()

    fun run() {
        //TODO Log

        var ret = build()

        val crawlerFactory = CrawlerFactory(ret)

        crawlerFactory.start(URI(mainUriTarget))

        var req = crawlerFactory.waitForFinish()

        var tkn =
            ret.directIO.getToken(UsingPath.RESUME).withAdditionalPathFile(ResumeDataNameGenerator(this).generate())

        resume.save(tkn, req)
    }

    private fun build(): VirtualOption {

        var file = File(paramPath)

        //var mef = MEFFactory(pluginDirectory)

        var srtf = SRTFFactory().create()
        var config = ConfigControllerImpl()
        var io = DirectIOImpl(config)
        var fac = JsonParserOptionFactory(file.readText(), listOf()) //TODO MEF


        return VirtualOption(fac.option, config, io, getResumeOption(), srtf.policies, srtf.scheduler)
    }

    private fun getResumeOption(): Option<ResumeOption> {
        return resumeAt.map {
            resume.get(it).toOption() // TODO
        }.flatten()
    }
}