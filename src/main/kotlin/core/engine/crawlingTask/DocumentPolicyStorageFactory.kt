package core.engine.crawlingTask

import arrow.core.Validated
import core.engine.*

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

class DocumentTypePolicyStorage<Document : Request>(private val policies: Iterable<TransactionPolicy<*, *, *, *>>) {
    fun <SrcTrans : Transaction<Document>, DstTrans : StrictTransaction<SrcTrans, Document>> find(): TransactionPolicy<SrcTrans, DstTrans, Document, Document> {
        return policies.filterIsInstance<TransactionPolicy<SrcTrans, DstTrans, Document, Document>>().single()!!

    }
}

class DocumentPolicyStorageFactoryCollector(private val dictionary: DocumentPolicyStorageFactory) {
    fun getFactory(type: DocumentType): DocumentPolicyStorageFactory {
        return dictionary
    }
}