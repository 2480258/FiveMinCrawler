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

package io.fivemin.server

import arrow.core.*
import arrow.core.continuations.either
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.TransactionSubPolicy
import com.fivemin.core.initialize.PluginObject
import com.fivemin.core.initialize.SubPolicyCollection
import com.fivemin.core.initialize.mef.MEFPlugin
import org.pf4j.Extension
import org.pf4j.ExtensionPoint
import org.pf4j.Plugin
import org.pf4j.PluginWrapper
import java.io.File
import java.net.URI
import java.util.concurrent.atomic.AtomicInteger

@Extension
class NotifyServerExtensions : ExtensionPoint, MEFPlugin {
    companion object {
        private val logger = LoggerController.getLogger("NotifyServer")
        
        
    }
    
    override val pluginName: String
        get() = "NotifyServer"
    override val priority: Int
        get() = 99
    
    override fun get(): PluginObject {
        logger.info("NotifyServer loaded")
        return PluginObject(SubPolicyCollection(listOf(), listOf(), listOf(), listOf()))
    }
}

class NotifyServerPlugin constructor(wrapper: PluginWrapper) : Plugin(wrapper) {

}
