package com.fivemin.core.export

import arrow.core.Option
import arrow.core.none
import arrow.core.toOption
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.ConfigController
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ConfigControllerImpl(jsonStr: String) : ConfigController {
    companion object {
        private val logger = LoggerController.getLogger("ConfigControllerImpl")
    }

    private var dataObj: Map<String, JsonElement>

    init {
        dataObj = try {
            Json.parseToJsonElement(jsonStr).jsonObject
        } catch (e: Exception) {
            logger.warn(e)
            mutableMapOf()
        }
    }

    override fun getSettings(settingName: String): Option<String> {
        if (dataObj.containsKey(settingName)) {
            var ret = (dataObj[settingName]?.jsonPrimitive?.content).toOption()

            ret.map {
                logger.info("get setting: [$settingName] = $it")
            }
            return ret
        } else {
            return none()
        }
    }
}
