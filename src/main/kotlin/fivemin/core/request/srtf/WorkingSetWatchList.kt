package fivemin.core.request.srtf

class WorkingSetWatchList {
    val blocks : MutableMap<SRTFPageBlock, Counter> = mutableMapOf()
    val count : Int
    get() {
        return _count
    }

    var _count = 0

    fun add(block: SRTFPageBlock){
        if(!blocks.containsKey(block)) {
            blocks[block] = Counter()
        }

        blocks[block]!!.increase()
    }

    fun get() : Iterable<Map.Entry<SRTFPageBlock, Counter>>{
        return blocks.asIterable()
    }

    fun remove(block : SRTFPageBlock){
        if(!blocks.containsKey(block)){
            return
        }

        blocks[block]!!.decrease()

        if(blocks[block]!!.count == 0){
            blocks.remove(block)
        }
    }
}

