package com.fivemin.core.engine

interface Transaction<out Document : Request> : Taggable
{
    val request : Document
}

interface StrictTransaction<in InTrans : Transaction<Request>,
                            out Document : Request> : Transaction<Document>

interface ReverableTransaction<in InTrans : Transaction<Request>, out Document : Request> : StrictTransaction<InTrans, Document>
{
    val previous : Transaction<Document>
}

