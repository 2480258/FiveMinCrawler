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

package com.fivemin.core.engine.transaction.serialize.postParser

import arrow.core.*
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.InitialTransactionImpl
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class PostParserContentPageImpl<Document : Request>(
    private val pageCondition: PageName,
    private val linkInfoFactory: RequestContentInfoFactory<Document>,
    private val attrInfoFactory: RequestContentInfoFactory<Document>,
    private val inteInfoFactory: InternalContentInfoFactory<Document>,
    private val attributeFactory: DocumentAttributeFactory
) : PostParserContentPage<Document> {

    companion object {
        private val logger = LoggerController.getLogger("PostParserContentPageImpl")
    }

    override suspend fun extract(
        req: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Option<List<DocumentAttribute>>> {
        return coroutineScope {
            async {
                req.previous.ifDocumentAsync({
                    if (it.parseOption.name == pageCondition) {
                        val internals = processIntAttribute(req)
                        val externals = processExtAttributes(req, info, state)
                        val links = processLinks(req, info, state)

                        links.toList().awaitAll() // wait until all child link downloaded

                        val finished = externals.map { y ->
                            y.await()
                        }.filterOption()

                        val ret = internals.fold({ finished }) { x ->
                            finished.plus(x)
                        }

                        ret.toOption()
                    } else {
                        none()
                    }
                }, {
                    none()
                })
            }
        }
    }

    private suspend fun processIntAttribute(req: FinalizeRequestTransaction<Document>): Option<Iterable<DocumentAttribute>> {
        return inteInfoFactory.get(req).map {
            it.map { x ->
                if (!x.data.any()) {
                    logger.warn(req.request.getDebugInfo() + " < " + x.attributeName + " < has no content; ignoring")
                    none()
                } else if (x.data.count() == 1) {
                    attributeFactory.getInternal(DocumentAttributeInfo(x.attributeName), x.data[0]).orNull().toOption()
                } else {
                    attributeFactory.getInternal(DocumentAttributeInfo(x.attributeName), x.data).orNull().toOption()
                }
            }.filterOption()
        }
    }

    private suspend fun processExtAttributes(
        req: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Iterable<Deferred<Option<DocumentAttribute>>> {
        val attr = attrInfoFactory.get(req)

        return attr.linkInfo.map { requestLinkInfo ->
            val ret = requestLinkInfo.requests.map { httpRequest ->
                val task = info.createTask<HttpRequest>().get2<
                    InitialTransaction<HttpRequest>,
                    PrepareTransaction<HttpRequest>,
                    FinalizeRequestTransaction<HttpRequest>>(DocumentType.NATIVE_HTTP)

                state.getChildSession {
                    task.start(InitialTransactionImpl(requestLinkInfo.option, TagRepositoryImpl(), httpRequest), info, it)
                }
            }

            if (ret.any()) {
                finalizeAttribute(requestLinkInfo, ret).toOption()
            } else {
                logger.warn(req.request.getDebugInfo() + " < " + requestLinkInfo.name + " < has no content; ignoring")
                none()
            }
        }.filterOption()
    }

    private suspend fun finalizeAttribute(
        x: RequestLinkInfo,
        ret: Iterable<Deferred<Either<Throwable, FinalizeRequestTransaction<HttpRequest>>>>
    ): Deferred<Option<DocumentAttribute>> {
        return coroutineScope {
            async {
                val finished = ret.toList().awaitAll().map {
                    val downloaded = it

                    downloaded.swap().map {
                        logger.warn(it)
                    }

                    downloaded.orNull().toOption()
                }.filterOption()
                val info = DocumentAttributeInfo(x.name)

                if (!finished.any()) {
                    none()
                } else if (finished.count() == 1) {
                    attributeFactory.getExternal(info, finished[0]).orNull().toOption()
                } else {
                    attributeFactory.getExternal(info, finished).orNull().toOption()
                }
            }
        }
    }

    private suspend fun processLinks(
        request: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Iterable<Deferred<Either<Throwable, ExportTransaction<HttpRequest>>>> {
        val links = linkInfoFactory.get(request)
        return links.linkInfo.map { x ->
            val ret = x.requests.map { y ->
                val task = info.createTask<HttpRequest>().get4<
                    InitialTransaction<HttpRequest>,
                    PrepareTransaction<HttpRequest>,
                    FinalizeRequestTransaction<HttpRequest>,
                    SerializeTransaction<HttpRequest>,
                    ExportTransaction<HttpRequest>>(DocumentType.NATIVE_HTTP)

                state.getChildSession {
                    task.start(InitialTransactionImpl(x.option, TagRepositoryImpl(), y), info, it)
                }
            }

            if (ret.any()) {
                ret.toOption()
            } else {
                logger.warn(request.request.getDebugInfo() + " < " + x.name + " < has no content; ignoring")
                none()
            }
        }.filterOption().flatten()
    }
}
