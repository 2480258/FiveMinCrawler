package com.fivemin.core.engine

class TaskInfo(val uniqueKeyProvider: KeyProvider, private val factory : CrawlerTaskFactoryFactory) {
    fun <Document : Request> createTask() : CrawlerTaskFactory<Document> {
        return factory.getFactory<Document>()
    }
}