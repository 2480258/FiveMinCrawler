package fivemin.core.engine

import arrow.core.Either
import fivemin.core.engine.*

interface ExportTransaction<out Document : Request> : StrictTransaction<SerializeTransaction<Request>, Document>
{
    val exportHandles : Iterable<Either<Throwable, ExportResultToken>>
}
