package fivemin.core.request.srtf

import arrow.core.Either
import fivemin.core.engine.PageName

class SRTFPageBlockSet{
    fun get(name : Either<PageName, String>) : SRTFPageBlock {
        return SRTFPageBlock.create(name)
    }
}