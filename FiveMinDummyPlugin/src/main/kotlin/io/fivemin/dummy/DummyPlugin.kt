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

package io.fivemin.dummy

import com.fivemin.core.LoggerController
import com.fivemin.core.initialize.PluginObject
import com.fivemin.core.initialize.SubPolicyCollection
import com.fivemin.core.initialize.mef.MEFPlugin
import org.pf4j.Extension
import org.pf4j.ExtensionPoint

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

@Extension
class DummyPlugin : ExtensionPoint, MEFPlugin{
    companion object {
        private val logger = LoggerController.getLogger("DummyPlugin")
    }
    
    override val pluginName: String
        get() = "DummyPlugin"
    override val priority: Int
        get() = 99
    
    override fun get(): PluginObject {
        logger.info("DummyPlugin loaded")
        return PluginObject(SubPolicyCollection(listOf(), listOf(), listOf(), listOf()))
    }
}