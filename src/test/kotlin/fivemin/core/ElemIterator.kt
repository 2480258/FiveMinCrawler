package fivemin.core

import java.net.URI

class ElemIterator<T>(val factory : IteratorElemFactory<T>) {
    val list = mutableListOf<T>()

    fun gen() : T {
        var t = factory.getNext()
        list.add(t)

        return t
    }

    operator fun get(index : Int) : T?{
        return list?.get(index)
    }
}

interface IteratorElemFactory<T> {
    fun getNext() : T
}

class UriIterator : IteratorElemFactory<URI> {
    val strFac = StringIterator()

    override fun getNext(): URI {
        return URI("http://" + strFac.getNext() + ".com/")
    }
}

class StringIterator : IteratorElemFactory<String> {
    val Str = "abcdefghijklmnopqrstuvwxyz0123456789"

    val len : Int = 8
    var calledCount = 0

    override fun getNext(): String {
        var cnt = calledCount
        var ret = ""

        while(cnt >= Str.length) {
            ret += Str[cnt % Str.length]
            cnt %= Str.length
        }

        ret += Str[cnt]
        calledCount++

        return ret
    }
}