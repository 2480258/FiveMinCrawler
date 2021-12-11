package core.engine.crawlingTask

import core.engine.*
import kotlin.reflect.*


class DocumentPolicyStorageFactory(val policies: Iterable<TransactionPolicy<*, *, *, *>>) {
    inline fun <reified Document : Request> create(): DocumentTypePolicyStorage<Document> {
        return DocumentTypePolicyStorage(policies.map {
            it as? TransactionPolicy<Transaction<Document>, Transaction<Document>, Document, Document>
        }.filterNotNull())
    }
}

class DocumentTypePolicyStorage<Document : Request>(val policies: List<TransactionPolicy<Transaction<Document>, Transaction<Document>, Document, Document>?>) {
    inline fun <reified SrcTrans : Transaction<Document>, reified DstTrans : StrictTransaction<SrcTrans, Document>> find(): TransactionPolicy<SrcTrans, DstTrans, Document, Document> {
        return policies
            .filter {
                it.inType
            }
    }
}

class DocumentPolicyStorageFactoryCollector(private val dictionary: DocumentPolicyStorageFactory) {
    fun getFactory(type: DocumentType): DocumentPolicyStorageFactory {
        return dictionary
    }
}