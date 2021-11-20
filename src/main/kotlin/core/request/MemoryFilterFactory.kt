package core.request

import arrow.core.Option
import core.engine.MemoryFilter
import core.engine.RequestToken

interface MemoryFilterFactory {
    fun createByteFilter(expectSize : Option<Long>, handle : RequestToken) : MemoryFilter
    fun createStringFilter(expectSize : Option<Long>, handle : RequestToken) : MemoryFilter
    fun createHtmlFilter(expectSize : Option<Long>, handle : RequestToken): MemoryFilter
}
