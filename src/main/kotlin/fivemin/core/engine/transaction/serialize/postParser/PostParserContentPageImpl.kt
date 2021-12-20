package fivemin.core.engine.transaction.serialize.postParser

import arrow.core.*
import fivemin.core.engine.*
import fivemin.core.engine.transaction.InitialTransactionImpl
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
    override suspend fun extract(
        req: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<List<DocumentAttribute>> {
        return coroutineScope {
            async {
                req.previous.ifDocumentAsync({
                    if (it.parseOption.name == pageCondition) {
                        var links = processLinks(req, info, state)
                        var attrs = processExtAttributes(req, info, state)
                        var intes = processIntAttribute(req)

                        links.toList().awaitAll() //wait until all child link downloaded

                        var finished = attrs.map { y ->
                            y.await()
                        }.filterOption()

                        var ret = intes.fold({ finished }) { x ->
                            finished.plus(x)
                        }

                        ret
                    } else {
                        listOf()
                    }
                }, {
                    listOf()
                })
            }
        }
    }

    private suspend fun processIntAttribute(req: FinalizeRequestTransaction<Document>): Option<Iterable<DocumentAttribute>> {
        return inteInfoFactory.get(req).map {
            it.map { x ->
                    if (!x.data.any()) {
                        none()
                    } else if (x.data.count() == 1) {
                        attributeFactory.getInternal(DocumentAttributeInfo(x.attributeName), x.data[0]).toOption() //TODO Log
                    } else {
                        attributeFactory.getInternal(DocumentAttributeInfo(x.attributeName), x.data).toOption()
                    }
            }.filterOption()
        }
    }

    private suspend fun processExtAttributes(
        req: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Iterable<Deferred<Option<DocumentAttribute>>> {
        var attr = attrInfoFactory.get(req)

        return attr.linkInfo.map { x ->
            var ret = x.requests.map { y ->
                var task = info.createTask<HttpRequest>().get2<
                        InitialTransaction<HttpRequest>,
                        PrepareTransaction<HttpRequest>,
                        FinalizeRequestTransaction<HttpRequest>>(DocumentType.NATIVE_HTTP)

                state.getChildSession {
                    task.start(InitialTransactionImpl(x.option, TagRepositoryImpl(), y), info, it)
                }
            }

            finalizeAttribute(x, ret)
        }
    }

    private suspend fun finalizeAttribute(
        x: RequestLinkInfo,
        ret: Iterable<Deferred<Validated<Throwable, FinalizeRequestTransaction<HttpRequest>>>>
    ): Deferred<Option<DocumentAttribute>> {
        return coroutineScope {
            async {
                var finished = ret.toList().awaitAll().map {
                    it.toOption() //TODO Log
                }.filterOption()
                val info = DocumentAttributeInfo(x.name)

                if (!finished.any()) {
                    none()
                } else if (finished.count() == 1) {
                    attributeFactory.getExternal(info, finished[0]).toOption()
                } else {
                    attributeFactory.getExternal(info, finished).toOption() //TODO Log
                }
            }
        }

    }

    private suspend fun processLinks(
        request: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Iterable<Deferred<Validated<Throwable, ExportTransaction<HttpRequest>>>> {
        var links = linkInfoFactory.get(request)
        return links.linkInfo.flatMap { x ->
            x.requests.map { y ->
                var task = info.createTask<HttpRequest>().get4<
                        InitialTransaction<HttpRequest>,
                        PrepareTransaction<HttpRequest>,
                        FinalizeRequestTransaction<HttpRequest>,
                        SerializeTransaction<HttpRequest>,
                        ExportTransaction<HttpRequest>>(DocumentType.NATIVE_HTTP)

                state.getChildSession {
                    task.start(InitialTransactionImpl(x.option, TagRepositoryImpl(), y), info, it)
                }
            }
        }
    }

}