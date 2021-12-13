package fivemin.core.engine.crawlingTask

import fivemin.core.engine.CrawlerTaskFactory
import fivemin.core.engine.CrawlerTaskFactoryFactory
import fivemin.core.engine.Request

class CrawlerTaskFactoryFactoryImpl(private val storage : DocumentPolicyStorageFactoryCollector) : CrawlerTaskFactoryFactory{
    override fun <Document : Request> getFactory(): CrawlerTaskFactory<Document> {
        return CrawlerTaskFactory(storage)
    }
}