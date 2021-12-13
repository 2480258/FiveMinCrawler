package fivemin.core.engine

import fivemin.core.engine.*

interface ExportTransaction<out Document : Request> : StrictTransaction<SerializeTransaction<Request>, Document>
{
    val exportHandles : Iterable<Result<ExportResultToken>>
}
