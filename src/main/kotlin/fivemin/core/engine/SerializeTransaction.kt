package fivemin.core.engine

import fivemin.core.engine.*

interface SerializeTransaction<out Document : Request> :
    StrictTransaction<FinalizeRequestTransaction<Request>, Document>
{
    val attributes : Iterable<DocumentAttribute>
}

