package core.engine

interface TaskInfo {
    fun <Document : Request> createTask() : CrawlerTaskFactory<Document>

    val uniqueKeyProvider : KeyProvider
}