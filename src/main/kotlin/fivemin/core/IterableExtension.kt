package fivemin.core

import arrow.core.*

class DuplicateKeyException : Exception(){

}
fun <T> Iterable<T>.exclusiveSingleOrNone() : Validated<Throwable, Option<T>> {
    if(this.count() > 1){
        return DuplicateKeyException().invalid()
    }

    return this.singleOrNone().valid()
}

fun <T> Iterable<T>.exclusiveSingleOrNone(filterFunc : (T) -> Boolean) : Validated<Throwable, Option<T>> {
    if(this.count() > 1){
        return DuplicateKeyException().invalid()
    }

    return this.singleOrNone(filterFunc).valid()
}