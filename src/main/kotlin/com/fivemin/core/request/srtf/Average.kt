package com.fivemin.core.request.srtf

class Average {
    var value : Double = 0.0
    var count : Int = 0

    fun addSample(sample : Double){
        value = (value * count + sample) / (count + 1)
        count++
    }
}