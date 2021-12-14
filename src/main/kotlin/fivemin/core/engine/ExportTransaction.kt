package fivemin.core.engine

import arrow.core.Validated
import fivemin.core.engine.*

interface ExportTransaction<out Document : Request> : StrictTransaction<SerializeTransaction<Request>, Document>
{
    val exportHandles : Iterable<Validated<Throwable, ExportResultToken>>
}
