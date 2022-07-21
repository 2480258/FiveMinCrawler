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

package com.fivemin.core.initialize.mef

import arrow.core.filterOption
import arrow.core.toOption
import com.fivemin.core.LoggerController
import com.fivemin.core.initialize.ModifyingObject
import com.fivemin.core.initialize.PluginSelector
import com.fivemin.core.initialize.SubPolicyCollection

import org.pf4j.DefaultPluginManager
import java.nio.file.Paths

class PluginSelectorImpl constructor(private val pluginPath: String) : PluginSelector {
    companion object {
        private val logger = LoggerController.getLogger("PluginSelectorImpl")
    }
    
    private fun loadPlugins(): List<MEFPlugin> {
        val pluginManager = DefaultPluginManager(Paths.get(pluginPath))
        
        pluginManager.loadPlugins()
        pluginManager.startPlugins()
        return pluginManager.getExtensions(MEFPlugin::class.java)
    }
    
    override fun fold(): ModifyingObject {
        val plugins = loadPlugins().sortedByDescending { it.priority }
        
        plugins.forEach {
            logger.info("${it.pluginName} < plugin loaded")
        }
        
        val collections =
            plugins.map { it.get() }.map { it.subPolicyCollection.toOption() }
                .filterOption()
        
        val folded = collections.fold(SubPolicyCollection(listOf(), listOf(), listOf(), listOf())) { source, operands ->
            source.merge(operands)
        }
        
        return ModifyingObject(plugins.map { it.pluginName }, folded)
    }
}