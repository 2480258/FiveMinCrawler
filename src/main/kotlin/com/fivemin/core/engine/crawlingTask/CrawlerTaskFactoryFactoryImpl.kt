package com.fivemin.core.engine.crawlingTask

import com.fivemin.core.engine.CrawlerTaskFactory
import com.fivemin.core.engine.CrawlerTaskFactoryFactory
import com.fivemin.core.engine.Request

class CrawlerTaskFactoryFactoryImpl(private val storage: DocumentPolicyStorageFactoryCollector) : CrawlerTaskFactoryFactory {
    override fun <Document : Request> getFactory(): CrawlerTaskFactory<Document> {
        return CrawlerTaskFactory(storage)
    }
}
