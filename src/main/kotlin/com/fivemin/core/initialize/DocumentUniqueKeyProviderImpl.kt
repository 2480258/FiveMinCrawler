package com.fivemin.core.initialize

import com.fivemin.core.engine.DocumentUniqueKeyProvider
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.UniqueKey

class DocumentUniqueKeyProviderImpl : DocumentUniqueKeyProvider{
    override fun <Document : Request> create(doc: Document): UniqueKey {
        TODO("Not yet implemented")
    }
}