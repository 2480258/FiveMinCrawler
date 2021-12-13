package fivemin.core.engine

import arrow.core.Option
import arrow.core.none
import fivemin.core.engine.*

interface InitialTransaction<out Document : Request> : Transaction<Document> {
    val option : InitialOption
}

data class InitialOption (val requestOption: Option<RequestOption> = none(),
                          val parseOption: Option<ParseOption> = none(),
                          val containerOption: Option<ContainerOption> = none()){

}
