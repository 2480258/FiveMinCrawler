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

package com.fivemin.core.request.srtf

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.none
import com.fivemin.core.engine.*
import com.fivemin.core.request.PreprocessedRequest
import com.fivemin.core.request.queue.DequeueOptimizationPolicy
import java.net.URI
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class SRTFScheduler : DequeueOptimizationPolicy {
    private val sync: Any = Any()
    private val lock = ReentrantLock()

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

    private val memorization: MutableMap<RequestToken, Pair<Double, Int>> = mutableMapOf()

    override fun getScore(req: PreprocessedRequest<Request>): Double {
        lock.withLock {
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

            var nonEndpoint = lst.sumOf { // for non-Endpoint time (uncle page)
                it.key.getEndpointTime() * it.value.count
            }

            var endpoint = pageBlockSet.get(convertTo(req.request.request)).getEndpointTime() // for endpoint time (children page)

            var expectedTime = nonEndpoint + endpoint

            if (!(memorization.containsKey(wshandle) && memorization[wshandle]?.second == fullcount)) {
                if (!memorization.containsKey(wshandle)) {
                    memorization.put(wshandle, Pair(expectedTime, fullcount))
                } else {
                    memorization[wshandle] = Pair(expectedTime, fullcount)
                }
            }

            return expectedTime
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
            return ""
        }

        return q.substring(q.lastIndexOf('.') + 1)
    }

    fun atPrepareStage(trans: PrepareTransaction<Request>, detachable: Boolean) {
        lock.withLock {
            var handle = trans.request.token
            var parent = if (detachable) none() else trans.request.parent

            var name = convertTo(trans)

            var pageBlock = pageBlockSet.get(name)

            var isUnique = documentBlockSet.tryAddBlock(
                handle,
                parent,
                pageBlock
            ) // handle is unique so if duplicated then preprocess should already have called

            if (parent.isEmpty() && !watchList.contains(handle)) {
                watchList[handle] = WorkingSetWatchList()
            }

            parent.getOrElse { handle }

            if (!detachable) {
                trans.request.parent.map {
                    var parentBlock = documentBlockSet.getBlockBy(it).pageName

                    if (isUnique) {
                        parentBlock.addSample(pageBlock)
                        watchList[documentBlockSet.getBlockBy(parent.getOrElse { handle }).bottomMost]!!.add(pageBlock)
                    }
                }
            }
        }
    }

    fun atFinalizeStage(req: FinalizeRequestTransaction<Request>, detachable: Boolean) {
        lock.withLock {
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
        lock.withLock {
            documentBlockSet.removeIfExistByWorkingSetHandle(token)
            memorization.remove(token)
        }
    }
}
