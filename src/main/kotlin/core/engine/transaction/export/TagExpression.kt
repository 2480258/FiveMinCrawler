package core.engine.transaction.export

import arrow.core.toOption
import core.engine.TagRepository

class TagExpression (private val originalString : String){
    private val regexTag : String = "\\\$\\(([0-z]*)\\)"
    private val regex : Regex = Regex(regexTag)

    fun build(tagRepo : TagRepository) : String{
        return parse(originalString, tagRepo)
    }

    private fun parse(macroStr : String, tagRepo : TagRepository) : String{
        val ret = regex.findAll(macroStr)

        var str : String = macroStr

        ret.forEach {
            if(it.value.length > 3){
                val qry = it.value.subSequence(2, it.value.length - 1)

                if(tagRepo.contains(qry.toString()) && !qry.contains('\\')){
                    str = macroStr.replace("&(" + qry.toString() + ")", tagRepo[qry.toString()].value)
                }
            }
        }

        return str
    }

}