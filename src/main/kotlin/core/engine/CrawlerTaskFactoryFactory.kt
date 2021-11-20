package core.engine

interface CrawlerTaskFactoryFactory {
    fun <Document : Request> getFactory() : CrawlerTaskFactory<Document>
}