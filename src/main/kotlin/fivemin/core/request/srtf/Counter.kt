package fivemin.core.request.srtf

class Counter{
    val count : Int
    get() {
        return _count
    }

    var _count = 0

    fun increase(){
        _count++
    }

    fun decrease(){
        _count--
    }
}