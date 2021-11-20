package core.engine

import core.engine.*

interface SerializeTransaction<out Document : Request> :
    StrictTransaction<FinalizeRequestTransaction<Request>, Document>
{
    val attributes : Iterable<DocumentAttribute>
}

