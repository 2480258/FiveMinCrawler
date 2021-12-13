package fivemin.core.initialize

import fivemin.core.engine.DocumentUniqueKeyProvider
import fivemin.core.engine.Request
import fivemin.core.engine.UniqueKey

class DocumentUniqueKeyProviderImpl : DocumentUniqueKeyProvider{
    override fun <Document : Request> create(doc: Document): UniqueKey {
        TODO("Not yet implemented")
    }
}