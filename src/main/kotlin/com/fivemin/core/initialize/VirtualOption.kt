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

data class StartTaskOption(
    val mainUriTarget: String,
    val paramPath: String,
    val pluginDirectory: Option<String> = none(),
    val resumeAt: Option<String> = none(),
    val rootPath: Option<String> = none()
)
