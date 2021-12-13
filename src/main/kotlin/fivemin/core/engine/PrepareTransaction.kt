package fivemin.core.engine

import fivemin.core.engine.*

fun <Return, Document : Request> PrepareTransaction<Document>.ifDocument(document : (PrepareDocumentTransaction<Document>) -> Return,
                                                                        other : (PrepareTransaction<Document>) -> Return) : Return{
    return if(this is PrepareDocumentTransaction<Document>){
        document(this)
    }
    else{
        other(this)
    }
}
suspend fun <Return, Document : Request> PrepareTransaction<Document>.ifDocumentAsync(document : suspend (PrepareDocumentTransaction<Document>) -> Return,
                                                                         other : suspend (PrepareTransaction<Document>) -> Return) : Return{
    return if(this is PrepareDocumentTransaction<Document>){
        document(this)
    }
    else{
        other(this)
    }
}

interface PrepareTransaction<out Document : Request> : ReverableTransaction<InitialTransaction<Request>, Document>
{
    val requestOption : RequestOption
}

interface PrepareDocumentTransaction<out Document : Request> :
    PrepareTransaction<Document>
{
    val parseOption : ParseOption
    val containerOption : ContainerOption
}

