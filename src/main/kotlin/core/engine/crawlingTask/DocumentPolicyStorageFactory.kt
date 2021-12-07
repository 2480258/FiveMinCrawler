package core.engine.crawlingTask

import core.engine.*
import core.engine.transaction.AbstractPolicy
import core.engine.transaction.finalizeRequest.FinalizeRequestTransactionPolicy
import core.engine.transaction.prepareRequest.PrepareRequestTransactionPolicy
import core.engine.transaction.serialize.SerializeTransactionPolicy
import java.security.PrivateKey

interface DocumentPolicyStorageFactory {
    fun <Document : Request> create(): DocumentTypePolicyStorage<Document>
}

class DocumentPolicyStorageFactoryImpl(private val policies: Iterable<TransactionPolicy<Transaction<Request>, Transaction<Request>, Request, Request>>) :
    DocumentPolicyStorageFactory {
    override fun <Document : Request> create(): DocumentTypePolicyStorage<Document> {
        return DocumentTypePolicyStorage<Document>(policies.map {
            it as? TransactionPolicy<Transaction<Document>, Transaction<Document>, Document, Document>
        }.filter {
            it != null
        }
    }
}

class DocumentTypePolicyStorage<Document : Request>(
    private val preprocPolicy: AbstractPolicy<InitialTransaction<Document>, PrepareTransaction<Document>, Document>,
    private val finishPolicy: AbstractPolicy<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document>,
    private val serializePolicy: AbstractPolicy<FinalizeRequestTransaction<Document>, SerializeTransaction<Document>, Document>,
    private val exportPolicy: AbstractPolicy<FinalizeRequestTransaction<Document>, SerializeTransaction<Document>, Document>
) {
    inline fun <reified SrcTrans: Transaction<Document>,reified  DstTrans : StrictTransaction<SrcTrans, Document>> find(): TransactionPolicy<SrcTrans, DstTrans, Document, Document> {
        return policies.filterIsInstance<TransactionPolicy<SrcTrans, DstTrans, Document, Document>>().single()!!
    }
}

class DocumentPolicyStorageFactoryCollector(private val dictionary: DocumentPolicyStorageFactory) {
    fun getFactory(type: DocumentType): DocumentPolicyStorageFactory {
        return dictionary
    }
}