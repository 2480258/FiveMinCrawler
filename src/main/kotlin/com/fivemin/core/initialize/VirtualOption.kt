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

package com.fivemin.core.initialize

import arrow.core.Option
import arrow.core.flatten
import arrow.core.none
import arrow.core.toOption
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.engine.session.bFilter.SerializedBloomFilterFactoryImpl
import com.fivemin.core.export.ConfigControllerImpl
import com.fivemin.core.initialize.json.JsonParserOptionFactory
import com.fivemin.core.initialize.mef.PluginSelectorImpl
import com.fivemin.core.request.queue.DequeueOptimizationPolicy
import com.fivemin.core.request.queue.srtfQueue.SRTFOptimizationPolicyImpl
import com.fivemin.core.request.queue.srtfQueue.SRTFPageDescriptorFactoryImpl
import com.fivemin.core.request.queue.srtfQueue.SRTFTimingRepositoryImpl
import java.io.File
import java.net.URI

data class VirtualOption(
    val parseOption: ParseOption,
    val controller: ConfigController,
    val directIO: DirectIO,
    val resumeOption: ResumeOption,
    val subPolicyCollection: SubPolicyCollection,
    val obj: CrawlerObjects
)

class StartTaskOption(
    val mainUriTarget: String,
    val paramPath: String,
    val pluginDirectory: Option<String> = none(),
    val resumeAt: Option<String> = none(),
    val rootPath: Option<String> = none()
) {
    
    companion object {
        private val logger = LoggerController.getLogger("StartTaskOption")
    }
    
    private val configFileName = "fivemin.config.json"
    suspend fun run() {
        // TODO Log
        
        var ret = build()
        
        val crawlerFactory = CrawlerFactory(ret)
        
        crawlerFactory.start(URI(mainUriTarget))
        
        var req = crawlerFactory.waitForFinish()
    }
    
    private fun getConfigString(): String {
        if (File(configFileName).exists()) {
            return File(configFileName).readText(Charsets.UTF_8)
        }
        
        return "{}" //empty json
    }
    
    private fun build(): VirtualOption {
        
        var file = File(paramPath)
        
        var mef = pluginDirectory.map {
            PluginSelectorImpl(it).fold().subPolicyCollection
        }.fold({ SubPolicyCollection(listOf(), listOf(), listOf(), listOf()) }, { it })
        
        var srtf = SRTFFactory().create()
        var config = ConfigControllerImpl(getConfigString())
        var io = DirectIOImpl(config, rootPath)
        var fac = JsonParserOptionFactory(file.readText(), listOf(), io) // TODO MEF
        
        return VirtualOption(
            fac.option,
            config,
            io,
            getResumeOption(),
            mef.merge(srtf.policies),
            CrawlerObjects(srtf.deq, srtf.keyEx, srtf.descriptFac)
        )
    }
    
    private fun getResumeOption(): ResumeOption {
        return resumeAt.map {
            ResumeOption(it)
        }.fold({
            ResumeOption(ResumeDataNameGenerator(this).generate())
        }, { it })
    }
}
