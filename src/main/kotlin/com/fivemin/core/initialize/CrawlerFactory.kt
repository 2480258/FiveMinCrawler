package com.fivemin.core.initialize

import arrow.core.Either
import arrow.core.none
import com.fivemin.core.engine.*
import com.fivemin.core.engine.crawlingTask.*
import com.fivemin.core.engine.session.ArchivedSessionSet
import com.fivemin.core.engine.session.SessionRepositoryImpl
import com.fivemin.core.engine.session.UniqueKeyRepositoryImpl
import com.fivemin.core.engine.transaction.*
import com.fivemin.core.engine.transaction.export.ExportParser
import com.fivemin.core.engine.transaction.export.ExportTransactionPolicy
import com.fivemin.core.engine.transaction.finalizeRequest.*
import com.fivemin.core.engine.transaction.prepareRequest.DetachableSubPolicy
import com.fivemin.core.engine.transaction.prepareRequest.PreParser
import com.fivemin.core.engine.transaction.prepareRequest.PrepareRequestTransactionPolicy
import com.fivemin.core.engine.transaction.serialize.PostParser
import com.fivemin.core.engine.transaction.serialize.SerializeTransactionPolicy
import com.fivemin.core.export.ExportStateImpl
import com.fivemin.core.request.*
import com.fivemin.core.request.queue.DequeueOptimizationPolicy
import com.fivemin.core.request.queue.RequestQueueImpl
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import java.net.URI

@Serializable
data class ResumeOption(
    val archivedSessionSet: ArchivedSessionSet
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

class CrawlerFactory(private val virtualOption: VirtualOption) {
    private val provider = KeyProvider(UriUniqueKeyProvider(), StringUniqueKeyProvider())
    private val controller: ConfigController = virtualOption.controller
    private val directIO = virtualOption.directIO
    private val exportState = ExportStateImpl(directIO, none())
    private val sessionRepository = SessionRepositoryImpl()
    private val uniqueKeyRepository = UniqueKeyRepositoryImpl(virtualOption.resumeOption.map { it.archivedSessionSet })
    
    private val taskFactory: CrawlerTaskFactoryFactory =
        createFactory(virtualOption.dequeue, virtualOption.subPolicyCollection)
    
    
    suspend fun start(uri: URI): Either<Throwable, ExportTransaction<Request>> {
        var task = taskFactory.getFactory<Request>()
            .get4<
                    InitialTransaction<Request>,
                    PrepareTransaction<Request>,
                    FinalizeRequestTransaction<Request>,
                    SerializeTransaction<Request>,
                    ExportTransaction<Request>>(
                DocumentType.DEFAULT
            )
        
        return coroutineScope {
            task.start(
                InitialTransactionImpl<Request>(
                    InitialOption(),
                    TagRepositoryImpl(),
                    DefaultRequest(TagRepositoryImpl(), none(), uri, RequestType.LINK)
                ),
                TaskInfo(provider, taskFactory),
                SessionInitStateImpl(
                    SessionInfo(sessionRepository, none()),
                    SessionData(uniqueKeyRepository, sessionRepository, 0)
                )
            ).await()
            
        }
    }
    
    fun waitForFinish(): ResumeOption {
        sessionRepository.waitFinish()
        return ResumeOption(uniqueKeyRepository.export(sessionRepository.getDetachables()))
    }
    
    private fun createFactory(
        dequeue: DequeueOptimizationPolicy,
        additional: SubPolicyCollection
    ): CrawlerTaskFactoryFactory {
        var def = getDefaultSubPolicyCollection()
        
        var merged = SubPolicyCollection(
            def.preprocess.plus(additional.preprocess),
            def.request.plus(additional.request),
            def.serialize.plus(additional.serialize),
            def.export.plus(additional.export)
        )
        
        return CrawlerTaskFactoryFactoryImpl(DocumentPolicyStorageFactoryCollector(getDefaultPolicy(dequeue, merged)))
    }
    
    private fun getDefaultPolicy(
        deq: DequeueOptimizationPolicy,
        subpol: SubPolicyCollection
    ): DocumentPolicyStorageFactory {
        val movefac = getDefaultMovementFactory(deq)
        
        val prepare = PrepareRequestTransactionPolicy(AbstractPolicyOption(subpol.preprocess), movefac)
        val request = FinalizeRequestTransactionPolicy(AbstractPolicyOption(subpol.request), movefac)
        val serialize = SerializeTransactionPolicy(AbstractPolicyOption(subpol.serialize), movefac)
        val export = ExportTransactionPolicy(AbstractPolicyOption(subpol.export), movefac)
        
        return DocumentPolicyStorageFactory(prepare, request, serialize, export)
    }
    
    private fun getDefaultMovementFactory(deq: DequeueOptimizationPolicy): MovementFactory<Request> {
        return MovementFactoryImpl(
            virtualOption.parseOption.preParser,
            RequestWaiter(getRequestTaskFactory(deq)),
            virtualOption.parseOption.exportParser,
            exportState,
            virtualOption.parseOption.postParser
        )
    }
    
    private fun getRequestQueue(deq: DequeueOptimizationPolicy): RequestQueue {
        return RequestQueueImpl(
            deq,
            controller.getSettings<Int>("MaxRequestThread").fold({ 1 }, { it })
        )
    }
    
    private fun getRequestTaskFactory(deq: DequeueOptimizationPolicy): RequesterTaskFactory {
        return RequesterTaskFactoryImpl(
            RequestTaskOption(
                virtualOption.parseOption.requesterSelector,
                getRequestQueue(deq)
            )
        )
    }
    
    private fun getDefaultSubPolicyCollection(): SubPolicyCollection {
        return SubPolicyCollection(
            listOf(MarkDetachablePolicy(), DetachableSubPolicy(), AddTagAliasSubPolicy()),
            listOf(RedirectSubPolicy(), RetrySubPolicy(), ResponseDisposeSubPolicy()),
            listOf(),
            listOf()
        )
    }
}