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
import com.fivemin.core.engine.session.BloomFilterUniqueKeyRepository
import com.fivemin.core.engine.session.bFilter.BloomFilterFactoryImpl
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
import com.fivemin.core.request.queue.srtfQueue.SRTFKeyExtractor
import com.fivemin.core.request.queue.srtfQueue.SRTFOptimizationPolicy
import com.fivemin.core.request.queue.srtfQueue.SRTFPageDescriptorFactory
import com.fivemin.core.request.queue.srtfQueue.WSQueue
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import java.net.URI

@Serializable
data class ResumeOption(
    val archivedSessionSet: SerializableAMQ
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

data class CrawlerObjects constructor(val deq: SRTFOptimizationPolicy, val keyEx: SRTFKeyExtractor, val descriptFac: SRTFPageDescriptorFactory){

}

class CrawlerFactory(private val virtualOption: VirtualOption) {
    private val MAX_PAGE_LIMIT_KEY = "MaxPageLimit"
    
    
    private val provider = KeyProvider(UriUniqueKeyProvider(), StringUniqueKeyProvider())
    private val controller: ConfigController = virtualOption.controller
    private val directIO = virtualOption.directIO
    private val exportState = ExportStateImpl(directIO, none())
    private val sessionUniqueKeyFilter = BloomFilterUniqueKeyRepository(BloomFilterFactoryImpl(), virtualOption.resumeOption.map { it.archivedSessionSet })
    
    private val taskFactory: CrawlerTaskFactoryFactory =
        createFactory(virtualOption.obj, virtualOption.subPolicyCollection)

    suspend fun start(uri: URI): Either<Throwable, ExportTransaction<Request>> {
        val task = taskFactory.getFactory<Request>()
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
                    HttpRequestImpl(
                        none(),
                        uri,
                        RequestType.LINK,
                        PerRequestHeaderProfile(none(), none(), none(), uri),
                        TagRepositoryImpl()
                    )
                ),
                TaskInfo(provider, taskFactory),
                SessionInitStateImpl(
                    SessionInfo(sessionUniqueKeyFilter, sessionUniqueKeyFilter),
                    SessionData(sessionUniqueKeyFilter, sessionUniqueKeyFilter),
                    SessionContext(LocalUniqueKeyTokenRepo(), none())
                )
            ).await()
        }
    }

    fun waitForFinish(): ResumeOption {
        sessionUniqueKeyFilter.waitFinish()
        return ResumeOption(sessionUniqueKeyFilter.export())
    }

    private fun createFactory(
        obj: CrawlerObjects,
        additional: SubPolicyCollection
    ): CrawlerTaskFactoryFactory {
        val def = getDefaultSubPolicyCollection()

        val merged = SubPolicyCollection(
            def.preprocess.plus(additional.preprocess),
            def.request.plus(additional.request),
            def.serialize.plus(additional.serialize),
            def.export.plus(additional.export)
        )

        return CrawlerTaskFactoryFactoryImpl(DocumentPolicyStorageFactoryCollector(getDefaultPolicy(obj, merged)))
    }

    private fun getDefaultPolicy(
        obj: CrawlerObjects,
        subpol: SubPolicyCollection
    ): DocumentPolicyStorageFactory {
        val movefac = getDefaultMovementFactory(obj.deq, obj.keyEx, obj.descriptFac)

        val prepare = PrepareRequestTransactionPolicy(AbstractPolicyOption(subpol.preprocess), movefac)
        val request = FinalizeRequestTransactionPolicy(AbstractPolicyOption(subpol.request), movefac)
        val serialize = SerializeTransactionPolicy(AbstractPolicyOption(subpol.serialize), movefac)
        val export = ExportTransactionPolicy(AbstractPolicyOption(subpol.export), movefac)

        return DocumentPolicyStorageFactory(prepare, request, serialize, export)
    }

    private fun getDefaultMovementFactory(deq: SRTFOptimizationPolicy, keyEx: SRTFKeyExtractor, descriptFac: SRTFPageDescriptorFactory): MovementFactory<Request> {
        return MovementFactoryImpl(
            virtualOption.parseOption.preParser,
            RequestWaiter(getRequestTaskFactory(deq, keyEx, descriptFac)),
            virtualOption.parseOption.exportParser,
            exportState,
            virtualOption.parseOption.postParser
        )
    }

    private fun getRequestQueue(deq: SRTFOptimizationPolicy, keyEx: SRTFKeyExtractor, descriptFac: SRTFPageDescriptorFactory): RequestQueue {
        val count = controller.getSettings("MaxRequestThread").map { it.toInt() }.fold({ 1 }, { it })

        if (count < 1) {
            throw IllegalArgumentException("MaxRequestThread is below 1")
        }

        return WSQueue(deq, keyEx, descriptFac, count)
    }

    private fun getRequestTaskFactory(deq: SRTFOptimizationPolicy, keyEx: SRTFKeyExtractor, descriptFac: SRTFPageDescriptorFactory): RequesterTaskFactory {
        return RequesterTaskFactoryImpl(
            RequestTaskOption(
                virtualOption.parseOption.requesterSelector,
                getRequestQueue(deq, keyEx, descriptFac)
            )
        )
    }

    private fun getDefaultSubPolicyCollection(): SubPolicyCollection {
        val maxPages = controller.getSettings(MAX_PAGE_LIMIT_KEY).map {
            it.toIntOrNull().toOption().map {
                LimitMaxPageSubPolicy<Request>(it)
            }
        }.flatten()
        
        val additionalPrepareSubPolicy = listOf(maxPages).filterOption()
        
        return SubPolicyCollection(
            listOf<TransactionSubPolicy<InitialTransaction<Request>, PrepareTransaction<Request>, Request>>(MarkDetachablePolicy(), DetachableSubPolicy(), AddTagAliasSubPolicy()).plus(additionalPrepareSubPolicy),
            listOf(RedirectSubPolicy(), RetrySubPolicy(), ResponseDisposeSubPolicy()),
            listOf(),
            listOf()
        )
    }
}
