package fivemin.core.engine

import arrow.core.Option
import arrow.core.none
import fivemin.core.engine.*

interface SerializeTransaction<out Document : Request> :
    StrictTransaction<FinalizeRequestTransaction<Request>, Document>
{
    val serializeOption : SerializeOption
    val attributes : Iterable<DocumentAttribute>
}

data class SerializeOption (val requestOption: RequestOption,
                            val parseOption: ParseOption,
                            val containerOption: ContainerOption
){

}

