package core.engine.crawlingTask

import core.engine.CrawlerTaskFactory
import core.engine.CrawlerTaskFactoryFactory
import core.engine.Request

class CrawlerTaskFactoryFactoryImpl(private val storage : DocumentPolicyStorageFactoryCollector) : CrawlerTaskFactoryFactory{
    override fun <Document : Request> getFactory(): CrawlerTaskFactory<Document> {
        return CrawlerTaskFactory(storage)
    }
}