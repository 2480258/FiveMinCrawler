/*
 *
 *     FiveMinCrawler
 *     Copyright (C) 2022  2480258
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

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
            logger.info("get setting failed: [$settingName]")
            return none()
        }
    }
}
