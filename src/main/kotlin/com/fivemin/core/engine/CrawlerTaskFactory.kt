package com.fivemin.core.engine

import com.fivemin.core.engine.crawlingTask.DocumentPolicyStorageFactoryCollector

class CrawlerTaskFactory<Document : Request>(val policySet: DocumentPolicyStorageFactoryCollector) {
    inline fun <reified S1 : Transaction<Document>, reified S2 : StrictTransaction<S1, Document>> get1(type: DocumentType): CrawlerTask1<S1, S2, Document, Document> {
        var ps = policySet.getFactory(type).create<Document>()
        return CrawlerTask1(ps.find())
    }

    inline fun <reified S1 : Transaction<Document>, reified S2 : StrictTransaction<S1, Document>, reified S3 : StrictTransaction<S2, Document>> get2(
        type: DocumentType
    ): CrawlerTask2<S1, S2, S3, Document, Document, Document> {
        var ps = policySet.getFactory(type).create<Document>()
        return CrawlerTask2(ps.find(), ps.find())
    }


    inline fun <reified S1 : Transaction<Document>, reified S2 : StrictTransaction<S1, Document>, reified S3 : StrictTransaction<S2, Document>, reified S4 : StrictTransaction<S3, Document>> get3(
        type: DocumentType
    ): CrawlerTask3<S1, S2, S3, S4, Document, Document, Document, Document> {
        var ps = policySet.getFactory(type).create<Document>()
        return CrawlerTask3(ps.find(), ps.find(), ps.find())
    }

    inline fun <reified S1 : Transaction<Document>, reified S2 : StrictTransaction<S1, Document>, reified S3 : StrictTransaction<S2, Document>, reified S4 : StrictTransaction<S3, Document>, reified S5 : StrictTransaction<S4, Document>> get4(
        type: DocumentType
    ): CrawlerTask4<S1, S2, S3, S4, S5, Document, Document, Document, Document, Document> {
        var ps = policySet.getFactory(type).create<Document>()
        return CrawlerTask4(ps.find(), ps.find(), ps.find(), ps.find())
    }
}