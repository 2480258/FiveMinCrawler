package com.fivemin.core.engine.transaction.export

import com.fivemin.core.engine.TagRepository

class TagExpression(private val originalString: String) {
    private val regexTag: String = "&\\(([0-z]*)\\)"
    private val regex: Regex = Regex(regexTag)

    fun build(tagRepo: TagRepository): String {
        return parse(originalString, tagRepo)
    }

    private fun parse(macroStr: String, tagRepo: TagRepository): String {
        val ret = regex.findAll(macroStr).toList()

        val macroMap = mutableMapOf<String, String>()

        var str: String = macroStr

        ret.forEach {
            if (it.value.length > 3) {
                val qry = it.value.subSequence(2, it.value.length - 1)

                if (tagRepo.contains(qry.toString()) && !qry.contains('\\')) {
                    macroMap[qry.toString()] = tagRepo[qry.toString()].value
                }
            }
        }

        macroMap.forEach {
            str = str.replace("&(" + it.key + ")", tagRepo[it.key].value)
        }

        return str
    }
}