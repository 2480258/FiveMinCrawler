package com.fivemin.core.request.srtf

import arrow.core.Either
import com.fivemin.core.engine.PageName

class SRTFPageBlock private constructor(private val name: Either<PageName, String>) {
    companion object {
        val blocks: MutableMap<Either<PageName, String>, SRTFPageBlock> = mutableMapOf()

        fun create(name: Either<PageName, String>): SRTFPageBlock {
            if (!blocks.containsKey(name)) {
                blocks.put(name, SRTFPageBlock(name))
            }

            return blocks[name]!!
        }
    }

    private val average: Average
    private val propagation: MutableMap<SRTFPageBlock, Counter>

    init {
        average = Average()
        propagation = mutableMapOf()
    }

    fun getEndpointTime(): Double {
        return average.value + propagation.map {
            it.key.average.value * it.value.count
        }.fold(0.0) { x, y ->
            x + y
        }
    }

    fun addTimeSample(time: Double) {
        average.addSample(time)
    }

    fun addSample(name: SRTFPageBlock) {
        if (!propagation.containsKey(name)) {
            propagation[name] = Counter()
        }

        propagation[name]!!.increase()
    }
}
