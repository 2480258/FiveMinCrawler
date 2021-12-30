package com.fivemin.core.export

import arrow.core.Option
import arrow.core.none
import arrow.core.toOption
import com.fivemin.core.engine.ConfigController
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

class ConfigControllerImpl : ConfigController {
    private var dataObj: Map<String, JsonElement>

    private val configFileName: String = "UniCrawlerConfig.json"

    init {
        dataObj = try {
            Json.parseToJsonElement(File(configFileName).readText(Charsets.UTF_8)).jsonObject
        } catch (e: Exception) {
            mutableMapOf()
        }
    }

    override fun <T> getSettings(settingName: String): Option<T> {
        if (dataObj.containsKey(settingName)) {
            return (dataObj[settingName]?.jsonPrimitive?.content as? T).toOption()
        } else {
            return none()
        }
    }
}
