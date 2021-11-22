package core.request.srtf

import arrow.core.Option
import core.engine.RequestToken

class SRTFDocumentBlockSet{
    val blocks : MutableMap<RequestToken, SRTFDocumentBlock> = mutableMapOf()

    val count : Int
    get() {
        return _count
    }

    var _count = 0

    fun getBlockBy(token : RequestToken) : SRTFDocumentBlock {
        return blocks.asIterable().single {
            it.value.token == token
        }.value
    }

    fun tryAddBlock(token : RequestToken, parent : Option<RequestToken>, name : SRTFPageBlock) : Boolean{
        val wsHandle = parent.fold({token}, {
            getBlockBy(it).bottomMost
        })

        var block = SRTFDocumentBlock(token, wsHandle, name)

        if(blocks.containsKey(token)){
            return false
        }

        blocks[token] = block

        return true
    }

    fun removeIfExistByWorkingSetHandle(token : RequestToken){
        if(!blocks.containsKey(token)){
            return
        }

        var lst = blocks.filter {
            it.value.bottomMost == token
        }.toList()

        lst.forEach {
            blocks.remove(it.first)
        }
    }
}