package core.request

import arrow.core.Option
import com.github.kittinunf.fuel.core.Encoding
import core.engine.*
import java.nio.charset.Charset

interface MemoryFilterFactory {
    fun createByteFilter(expectSize : Option<Long>, handle : RequestToken) : MemoryFilter
    fun createStringFilter(expectSize : Option<Long>, handle : RequestToken, enc : Option<Charset>) : MemoryFilter
    fun createHtmlFilter(expectSize : Option<Long>, handle : RequestToken, enc : Option<Charset>): MemoryFilter
}

class MemoryFilterFactoryImpl (
    io : DirectIO,
    val factory : HtmlDocumentFactory
        ): MemoryFilterFactory{

    val token : DirectoryIOToken

    init {
        token = io.getToken(UsingPath.TEMP)
    }

    override fun createByteFilter(expectSize: Option<Long>, handle: RequestToken): MemoryFilter {
        return TranslatableFilter(expectSize, handle, token)
    }

    override fun createStringFilter(expectSize: Option<Long>, handle: RequestToken, enc : Option<Charset>): MemoryFilter {
        return StringFilterImpl(TranslatableFilter(expectSize, handle, token), enc)
    }

    override fun createHtmlFilter(expectSize: Option<Long>, handle: RequestToken, enc : Option<Charset>): MemoryFilter {
        return HtmlFilterImpl(StringFilterImpl(TranslatableFilter(expectSize, handle, token), enc), factory)
    }

}