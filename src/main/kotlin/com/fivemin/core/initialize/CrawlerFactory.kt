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

package com.fivemin.core.initialize

import arrow.core.*
import com.fivemin.core.engine.*
import com.fivemin.core.engine.crawlingTask.*
import com.fivemin.core.engine.session.*
import com.fivemin.core.engine.session.bFilter.BloomFilterFactoryImpl
import com.fivemin.core.engine.session.database.DatabaseAdapterFactoryImpl
import com.fivemin.core.engine.transaction.*
import com.fivemin.core.engine.transaction.export.ExportParser
import com.fivemin.core.engine.transaction.export.ExportTransactionMovement
import com.fivemin.core.engine.transaction.export.ExportTransactionPolicy
import com.fivemin.core.engine.transaction.finalizeRequest.*
import com.fivemin.core.engine.transaction.prepareRequest.DetachableSubPolicy
import com.fivemin.core.engine.transaction.prepareRequest.PreParser
import com.fivemin.core.engine.transaction.prepareRequest.PrepareRequestTransactionMovement
import com.fivemin.core.engine.transaction.prepareRequest.PrepareRequestTransactionPolicy
import com.fivemin.core.engine.transaction.serialize.PostParser
import com.fivemin.core.engine.transaction.serialize.SerializeTransactionMovementImpl
import com.fivemin.core.engine.transaction.serialize.SerializeTransactionPolicy
import com.fivemin.core.export.ConfigControllerImpl
import com.fivemin.core.export.ExportStateImpl
import com.fivemin.core.initialize.json.JsonParserOptionFactory
import com.fivemin.core.initialize.mef.PluginSelectorImpl
import com.fivemin.core.request.RequestTaskOption
import com.fivemin.core.request.RequesterSelector
import com.fivemin.core.request.queue.srtfQueue.*
import kotlinx.serialization.Serializable
import java.io.File
import java.io.InvalidObjectException
import java.net.URI

@Serializable
data class ResumeOption(
    val jdbcUrl: String
)

data class SubPolicyCollection(
    val preprocess: Iterable<TransactionSubPolicy<InitialTransaction<Request>, PrepareTransaction<Request>, Request>>,
    val request: Iterable<TransactionSubPolicy<PrepareTransaction<Request>, FinalizeRequestTransaction<Request>, Request>>,
    val serialize: Iterable<TransactionSubPolicy<FinalizeRequestTransaction<Request>, SerializeTransaction<Request>, Request>>,
    val export: Iterable<TransactionSubPolicy<SerializeTransaction<Request>, ExportTransaction<Request>, Request>>
) {
    fun merge(collection: SubPolicyCollection): SubPolicyCollection {
        return SubPolicyCollection(
            preprocess.plus(collection.preprocess),
            request.plus(collection.request),
            serialize.plus(collection.serialize),
            export.plus((collection.export))
        )
    }
}

data class ParseOption(
    val preParser: PreParser,
    val postParser: PostParser<Request>,
    val exportParser: ExportParser,
    val requesterSelector: RequesterSelector
)

data class CrawlerObjects constructor(
    val deq: SRTFOptimizationPolicy, val keyEx: SRTFKeyExtractor, val descriptFac: SRTFPageDescriptorFactory
) {

}


enum class DocumentTransaction {
    Prepare, Request, Serialize, Export
}


class CrawlerBuilder constructor(private val option: StartTaskOption, private val policyMap: Map<DocumentTransaction, Any>) {
    companion object {
        fun <Document : Request> startPolicyBuild(option: StartTaskOption): Builder1<Document, InitialTransaction<Document>> {
            return Builder1(mutableMapOf(), option)
        }
    }
    
    class Builder1<Document : Request, Src : Transaction<Document>> constructor(private val policy: MutableMap<DocumentTransaction, Any>, private val option: StartTaskOption) {
        fun <Dst : StrictTransaction<Src, Document>> addPolicy(
            obj: TransactionMovementFactory<Src, Dst, Document>,
            subPolicies: Iterable<TransactionSubPolicy<Src, Dst, Document>>,
            loc: DocumentTransaction
        ): Builder1<Document, Dst> {
            val psObj = when (loc) {
                DocumentTransaction.Prepare -> PrepareRequestTransactionPolicy(
                    AbstractPolicyOption(subPolicies) as AbstractPolicyOption<InitialTransaction<Document>, PrepareTransaction<Document>, Document>,
                    obj as TransactionMovementFactory<InitialTransaction<Document>, PrepareTransaction<Document>, Document>
                )
                DocumentTransaction.Request -> FinalizeRequestTransactionPolicy(
                    AbstractPolicyOption(subPolicies) as AbstractPolicyOption<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document>,
                    obj as TransactionMovementFactory<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document>
                )
                DocumentTransaction.Serialize -> SerializeTransactionPolicy(
                    AbstractPolicyOption(subPolicies) as AbstractPolicyOption<FinalizeRequestTransaction<Document>, SerializeTransaction<Document>, Document>,
                    obj as TransactionMovementFactory<FinalizeRequestTransaction<Document>, SerializeTransaction<Document>, Document>
                )
                DocumentTransaction.Export -> ExportTransactionPolicy(
                    AbstractPolicyOption(subPolicies) as AbstractPolicyOption<SerializeTransaction<Document>, ExportTransaction<Document>, Document>,
                    obj as TransactionMovementFactory<SerializeTransaction<Document>, ExportTransaction<Document>, Document>
                )
            }
            
            policy[loc] = psObj
            return Builder1(policy, option)
        }
        
        fun endPolicyBuild(): CrawlerBuilder {
            return CrawlerBuilder(option, policy)
        }
    }
    
    fun finalize(keyProvider: KeyProvider, state: SessionInitState, finishObserver: FinishObserver): CrawlerStarter {
        val taskFactory = CrawlerTaskFactoryFactoryImpl(DocumentPolicyStorageFactoryCollector(policyMap))
        
        return CrawlerStarter(
            URI(option.mainUriTarget), taskFactory, TaskInfo(keyProvider, taskFactory), state, finishObserver
        )
    }
}

class CrawlerStarter constructor(
    private val uri: URI,
    private val factory: CrawlerTaskFactoryFactoryImpl,
    private val taskInfo: TaskInfo,
    private val state: SessionInitState,
    private val finishObserver: FinishObserver
) {
    private var isAlreadyStarted: Boolean = false
    
    fun startAndWaitUntilFinish(func: (taskFactory: CrawlerTaskFactoryFactoryImpl, document: InitialTransaction<Request>, info: TaskInfo, state: SessionInitState) -> Unit) {
        if(isAlreadyStarted) {
            throw InvalidObjectException("can be called only once")
        }
        
        try {
            isAlreadyStarted = true
            func(
                factory, InitialTransactionImpl<Request>(
                    InitialOption(), TagRepositoryImpl(), HttpRequestImpl(
                        none(),
                        uri,
                        RequestType.LINK,
                        PerRequestHeaderProfile(none(), none(), none(), uri),
                        TagRepositoryImpl()
                    )
                ), taskInfo, state
            )
        } finally {
            finishObserver.waitFinish()
        }
    }
}

data class CrawlerOption constructor(
    val preParser: PreParser,
    val requestWaiter: RequestWaiter,
    val postParser: PostParser<Request>,
    val exportParser: ExportParser,
    val exportState: ExportState,
    val keyProvider: KeyProvider,
    val state: SessionInitStateImpl,
    val subPolicies: SubPolicyCollection,
    val finishObserver: FinishObserver
)

class CrawlerFactory {
    class PrepareFactory constructor(val preParser: PreParser) :
        TransactionMovementFactory<InitialTransaction<Request>, PrepareTransaction<Request>, Request> {
        override fun getMovement(): TransactionMovement<InitialTransaction<Request>, PrepareTransaction<Request>, Request> {
            return PrepareRequestTransactionMovement(preParser)
        }
    }
    
    class FinalizeReqFactory constructor(val requestWaiter: RequestWaiter) :
        TransactionMovementFactory<PrepareTransaction<Request>, FinalizeRequestTransaction<Request>, Request> {
        override fun getMovement(): TransactionMovement<PrepareTransaction<Request>, FinalizeRequestTransaction<Request>, Request> {
            return FinalizeRequestTransactionMovement(requestWaiter)
        }
    }
    
    class SerializeFactory constructor(val postParser: PostParser<Request>) :
        TransactionMovementFactory<FinalizeRequestTransaction<Request>, SerializeTransaction<Request>, Request> {
        override fun getMovement(): TransactionMovement<FinalizeRequestTransaction<Request>, SerializeTransaction<Request>, Request> {
            return SerializeTransactionMovementImpl(postParser)
        }
    }
    
    class ExportFactory constructor(val ep: ExportParser, val es: ExportState) :
        TransactionMovementFactory<SerializeTransaction<Request>, ExportTransaction<Request>, Request> {
        override fun getMovement(): TransactionMovement<SerializeTransaction<Request>, ExportTransaction<Request>, Request> {
            return ExportTransactionMovement(ep, es)
        }
    }
    
    fun get(startOption: StartTaskOption): CrawlerStarter {
        val options = CrawlerOptionFactory().get(startOption)
        
        return CrawlerBuilder.startPolicyBuild<Request>(startOption)
            .addPolicy(PrepareFactory(options.preParser), options.subPolicies.preprocess, DocumentTransaction.Prepare)
            .addPolicy(
                FinalizeReqFactory(options.requestWaiter), options.subPolicies.request, DocumentTransaction.Request
            ).addPolicy(
                SerializeFactory(options.postParser), options.subPolicies.serialize, DocumentTransaction.Serialize
            ).addPolicy(
                ExportFactory(options.exportParser, options.exportState),
                options.subPolicies.export,
                DocumentTransaction.Export
            ).endPolicyBuild().finalize(options.keyProvider, options.state, options.finishObserver)
    }
    
    class CrawlerOptionFactory {
        val CONFIG_FILE_NAME = "fivemin.config.json"
        val MAX_PAGE_LIMIT_KEY = "MaxPageLimit"
        val MAX_REQUEST_THREAD = "MaxRequestThread"
        
        fun get(option: StartTaskOption): CrawlerOption {
            val configController = getConfigController()
            val directIO = getDirectIO(configController, option.rootPath)
            val finishObserver = FinishObserverImpl()
            val sessionUniqueKeyRepository = getSessionUniqueKeyFilter(option.resumeAt, option.mainUriTarget)
            val jsonOptionFactory = getParseOptionFactory(option.paramPath, directIO)
            val srtfOption = getSRTFOption()
            
            return CrawlerOption(
                getPreparser(jsonOptionFactory),
                getRequestWaiter(configController, jsonOptionFactory, srtfOption),
                getPostParser(jsonOptionFactory),
                getExportParser(jsonOptionFactory),
                getExportState(directIO),
                getKeyProvider(),
                getSessionInitStateImpl(finishObserver, sessionUniqueKeyRepository, sessionUniqueKeyRepository),
                getDefaultSubPolicyCollection(configController).merge(getSRTFSubPolicyCollection(srtfOption))
                    .merge(getPluginSubPolicyCollection(option.pluginDirectory)),
                finishObserver
            )
        }
        
        private fun getKeyProvider(): KeyProvider {
            return KeyProvider(UriUniqueKeyProvider(), StringUniqueKeyProvider())
        }
        
        private fun getConfigController(): ConfigController {
            val configString = if (File(CONFIG_FILE_NAME).exists()) {
                File(CONFIG_FILE_NAME).readText(Charsets.UTF_8)
            } else "{}"
            
            return ConfigControllerImpl(configString)
        }
        
        private fun getDirectIO(config: ConfigController, rootPath: Option<String> = none()): DirectIO {
            return DirectIOImpl(config, rootPath)
        }
        
        private fun getSessionUniqueKeyFilter(resumeAt: Option<String>, target: String): CompositeUniqueKeyRepository {
            val jdbcUrl = ResumeDataNameGenerator(target).generate(resumeAt)
            val persister = UniqueKeyPersisterImpl(DatabaseAdapterFactoryImpl(jdbcUrl).get())
            
            return CompositeUniqueKeyRepository(
                persister,
                BloomFilterCache(BloomFilterFactoryImpl()),
                TemporaryUniqueKeyRepository(),
                UniqueKeyTokenFactory()
            )
        }
        
        private fun getParseOptionFactory(paramPath: String, io: DirectIO): JsonParserOptionFactory {
            return JsonParserOptionFactory(File(paramPath).readText(Charsets.UTF_8), listOf(), io)
        }
        
        private fun getPreparser(factory: JsonParserOptionFactory): PreParser {
            return factory.option.preParser
        }
        
        private fun getPostParser(factory: JsonParserOptionFactory): PostParser<Request> {
            return factory.option.postParser
        }
        
        private fun getExportState(directIO: DirectIO): ExportState {
            return ExportStateImpl(directIO, none())
        }
        
        private fun getExportParser(factory: JsonParserOptionFactory): ExportParser {
            return factory.option.exportParser
        }
        
        private fun getRequestWaiter(
            controller: ConfigController, factory: JsonParserOptionFactory, srtf: SRTFOption
        ): RequestWaiter {
            val requestThread = controller.getSettings(MAX_REQUEST_THREAD).map { it.toInt() }.fold({ 1 }, { it })
            
            if (requestThread < 1) {
                throw IllegalArgumentException("MaxRequestThread is below 1")
            }
            
            val queue = WSQueue(srtf.deq, srtf.keyEx, srtf.descriptFac, requestThread)
            
            val taskFactory = RequesterTaskFactoryImpl(
                RequestTaskOption(
                    factory.option.requesterSelector, queue
                )
            )
            
            return RequestWaiter(taskFactory)
        }
        
        private fun getSRTFOption(): SRTFOption {
            val timing = SRTFTimingRepositoryImpl()
            val opt = SRTFOptimizationPolicyImpl(timing)
            val keyEx = opt
            val descript = SRTFPageDescriptorFactoryImpl()
            
            return SRTFOption(opt, keyEx, descript, timing)
        }
        
        private fun getSessionInitStateImpl(
            finishObserver: FinishObserver, detachObserver: DetachObserver, uniqueKeyRepository: UniqueKeyRepository
        ): SessionInitStateImpl {
            return SessionInitStateImpl(
                SessionInfo(finishObserver, detachObserver),
                SessionData(uniqueKeyRepository, SessionRepositoryImpl(detachObserver, finishObserver)),
                SessionContext(LocalUniqueKeyTokenRepo(), none())
            )
        }
        
        private fun getPluginSubPolicyCollection(pluginDirectory: Option<String>): SubPolicyCollection {
            return pluginDirectory.map {
                PluginSelectorImpl(it).fold().subPolicyCollection
            }.fold({ SubPolicyCollection(listOf(), listOf(), listOf(), listOf()) }, { it })
        }
        
        private fun getSRTFSubPolicyCollection(option: SRTFOption): SubPolicyCollection {
            return SubPolicyCollection(
                listOf(),
                listOf(SRTFLogSubPolicy(option.timing, option.descriptFac, option.deq)),
                listOf(),
                listOf()
            )
        }
        
        private fun getDefaultSubPolicyCollection(controller: ConfigController): SubPolicyCollection {
            val maxPages = controller.getSettings(MAX_PAGE_LIMIT_KEY).map {
                it.toIntOrNull().toOption().map {
                    LimitMaxPageSubPolicy<Request>(it)
                }
            }.flatten()
            
            val additionalPrepareSubPolicy = listOf(maxPages).filterOption()
            
            return SubPolicyCollection(
                listOf<TransactionSubPolicy<InitialTransaction<Request>, PrepareTransaction<Request>, Request>>(
                    MarkDetachablePolicy(), DetachableSubPolicy(), AddTagAliasSubPolicy()
                ).plus(additionalPrepareSubPolicy),
                listOf(RedirectSubPolicy(), RetrySubPolicy()),
                listOf(),
                listOf()
            )
        }
        
        data class SRTFOption constructor(
            val deq: SRTFOptimizationPolicy,
            val keyEx: SRTFKeyExtractor,
            val descriptFac: SRTFPageDescriptorFactory,
            val timing: SRTFTimingRepository
        )
    }
}
