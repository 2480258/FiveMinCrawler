package fivemin.core

import arrow.core.*

class DuplicateKeyException : Exception(){

}
fun <T> Iterable<T>.exclusiveSingleOrNone() : Option<T> {
    if(this.count() > 1){
        throw DuplicateKeyException()
    }

    return this.singleOrNone()
}

fun <T> Iterable<T>.exclusiveSingleOrNone(filterFunc : (T) -> Boolean) : Option<T> {
    if(this.count() > 1){
        throw DuplicateKeyException()
    }

    return this.singleOrNone(filterFunc)
}