package core.initialize

import core.engine.DocumentUniqueKeyProvider
import core.engine.Request
import core.engine.UniqueKey

class DocumentUniqueKeyProviderImpl : DocumentUniqueKeyProvider{
    override fun <Document : Request> create(doc: Document): UniqueKey {
        TODO("Not yet implemented")
    }
}