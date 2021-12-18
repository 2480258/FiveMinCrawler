package fivemin.core.request.srtf

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.none
import arrow.core.toOption
import fivemin.core.engine.*
import fivemin.core.request.PreprocessedRequest
import fivemin.core.request.queue.DequeueOptimizationPolicy
import java.net.URI

class SRTFScheduler : DequeueOptimizationPolicy {
    private val sync: Any = Any()

    private val documentBlockSet = SRTFDocumentBlockSet()

    val blockCount: Int
        get() {
            return documentBlockSet.count
        }

    val watchListCount: Int
        get() {
            return watchList.count()
        }

    private val pageBlockSet = SRTFPageBlockSet()

    private val watchList: MutableMap<RequestToken, WorkingSetWatchList> = mutableMapOf()

    private val watchlistCount: Int
        get() {
            return watchList.count()
        }

    private val memorization: MutableMap<RequestToken, Pair<Double, Int>> = mutableMapOf()

    override fun getScore(req: PreprocessedRequest<Request>): Double {
        synchronized(sync) {
            var parent =
                if (req.request.info.detachState == DetachableState.WANT) none() else req.request.request.request.parent

            var wshandle =
                documentBlockSet.getBlockBy(parent.getOrElse { req.request.request.request.token }).bottomMost
            var lst = watchList[wshandle]!!.get()

            var fullcount = lst.sumOf {
                it.value.count
            }


            if (memorization.containsKey(wshandle) && memorization[wshandle]?.second == fullcount) {
                return memorization[wshandle]!!.first
            }

            var ret = lst.sumOf {
                it.key.getEndpointTime() * it.value.count
            }

            if (!(memorization.containsKey(wshandle) && memorization[wshandle]?.second == fullcount)) {
                if (!memorization.containsKey(wshandle)) {
                    memorization.put(wshandle, Pair(ret, fullcount))
                } else {
                    memorization[wshandle] = Pair(ret, fullcount)
                }
            }

            return ret
        }
    }

    private fun convertTo(trans: PrepareTransaction<Request>): Either<PageName, String> {
        return trans.ifDocument({
            Either.Left(it.parseOption.name)
        }, {
            Either.Right(getUriExtension(it.request.target))
        })
    }

    private fun getUriExtension(u: URI): String {
        val q = (u.query ?: "")

        if (!q.contains('.')) {
            return "";
        }

        return q.substring(q.lastIndexOf('.') + 1)
    }

    fun atPrepareStage(trans: PrepareTransaction<Request>, detachable: Boolean) {
        synchronized(sync) {
            var handle = trans.request.token
            var parent = if (detachable) none() else trans.request.parent

            var name = convertTo(trans)

            var pageBlock = pageBlockSet.get(name)

            var isUnique = documentBlockSet.tryAddBlock(
                handle,
                parent,
                pageBlock
            ) //handle is unique so if duplicated then preprocess called twice or retry.

            if (parent.isEmpty() && !watchList.contains(handle)) {
                watchList.put(handle, WorkingSetWatchList())
            }

            trans.request.parent.map {
                var parentBlock = documentBlockSet.getBlockBy(it).pageName

                if (isUnique) {
                    parentBlock.addSample(pageBlock)
                    watchList[documentBlockSet.getBlockBy(parent.getOrElse { handle }).bottomMost]!!.add(pageBlock)
                }
            }
        }
    }

    fun atFinalizeStage(req: FinalizeRequestTransaction<Request>, detachable: Boolean) {
        synchronized(sync) {
            var parent = if (detachable) none() else req.request.parent

            var key = documentBlockSet.getBlockBy(parent.getOrElse { req.request.token }).bottomMost
            var ret = pageBlockSet.get(convertTo(req.previous))

            req.result.map {
                it.responseBody.ifSucc({
                    ret.addTimeSample(it.responseTime.duration.toDouble())
                }, {})
            }

            if (watchList.contains(key)) {
                watchList[key]?.remove(ret)
            }
        }
    }


    fun atExportStage(token: RequestToken) {
        synchronized(sync) {
            documentBlockSet.removeIfExistByWorkingSetHandle(token)
            memorization.remove(token)
        }
    }
}