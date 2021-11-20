package core.engine

import arrow.core.Option
import core.engine.*

interface InitialTransaction<out Document : Request> : Transaction<Document> {
    val option : InitialOption
}

data class InitialOption (val requestOption: Option<RequestOption>,
                          val parseOption: Option<ParseOption>,
                          val containerOption: Option<ContainerOption>){

}
