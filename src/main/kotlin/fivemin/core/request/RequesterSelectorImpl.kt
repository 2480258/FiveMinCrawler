package fivemin.core.request

import arrow.core.Either
import arrow.core.right
import arrow.core.valid
import fivemin.core.engine.*
import fivemin.core.engine.transaction.finalizeRequest.DocumentRequest
import java.util.*

class RequesterSelectorImpl(val requesterMap: Map<RequesterEngineInfo, RequesterEngine<ResponseData>>) :
    RequesterSelector {
    var rd = Random(System.currentTimeMillis())

    override fun <Document : Request, Resp : ResponseData> schedule(req: DocumentRequest<Document>): Either<Throwable, RequesterSelected<Resp>> {
        var pref = req.request.requestOption.preference
        var engine = getEngine<Resp>(pref.engine)

        return engine.map {
            var idx = pref.slot.fold({ randomSelect(it) }, {
                it
            })

            var ret = getCore(it, idx)

            RequesterSelected(ret, PerformedRequesterInfo(pref.engine, idx))
        }
    }

    private fun <Resp : ResponseData> getEngine(info: RequesterEngineInfo): Either<Throwable, RequesterEngine<Resp>> {
        return (requesterMap[info]!! as RequesterEngine<Resp>).right() //we wouldn't check type; if wrong restart is required anyway
    }

    private fun <Resp : ResponseData> getCore(
        engine: RequesterEngine<Resp>,
        info: RequesterSlotInfo
    ): RequesterCore<Resp> {
        return engine.get(info)
    }

    private fun <Resp : ResponseData> randomSelect(req: RequesterEngine<Resp>): RequesterSlotInfo {
        var idx = rd.nextInt() % req.count
        return RequesterSlotInfo(idx)
    }
}