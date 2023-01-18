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

import arrow.core.Either
import arrow.core.left
import arrow.core.right
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

@Extension
class DummyPluginExtensions : ExtensionPoint, MEFPlugin {
    companion object {
        private val logger = LoggerController.getLogger("DummyPlugin")
    }
    
    override val pluginName: String
        get() = "DummyPlugin"
    override val priority: Int
        get() = 99
    
    override fun get(): PluginObject {
        logger.info("DummyPlugin loaded")
        return PluginObject(SubPolicyCollection(listOf(DummySubPolicy1()), listOf(DummySubPolicy2()), listOf(DummySubPolicy3()), listOf(DummySubPolicy4())))
    }
}

class DummyPlugin constructor(wrapper: PluginWrapper) : Plugin(wrapper) {

}

class DummySubPolicy1 : TransactionSubPolicy<InitialTransaction<Request>, PrepareTransaction<Request>, Request> {
    override suspend fun <Ret> process(
        source: InitialTransaction<Request>,
        dest: PrepareTransaction<Request>,
        
        state: SessionStartedState,
        next: suspend (Either<Throwable, PrepareTransaction<Request>>) -> Either<Throwable, Ret>
    ): Either<Throwable, Ret> {
        
        val result = "${source.request.target}\n${dest.request.target}\n${state.taskInfo.javaClass.name}\n${state.javaClass.name}"
        File("Output/p1.txt").writeText(result)
        
        return next(dest.right())
    }
}

class DummySubPolicy2 : TransactionSubPolicy<PrepareTransaction<Request>, FinalizeRequestTransaction<Request>, Request> {
    override suspend fun <Ret> process(
        source: PrepareTransaction<Request>,
        dest: FinalizeRequestTransaction<Request>,
        
        state: SessionStartedState,
        next: suspend (Either<Throwable, FinalizeRequestTransaction<Request>>) -> Either<Throwable, Ret>
    ): Either<Throwable, Ret> {
    
        val result = "${source.request.target}\n${dest.request.target}\n${state.taskInfo.javaClass.name}\n${state.javaClass.name}"
        File("Output/p2.txt").writeText(result)
        
        return next(dest.right())
    }
}

class DummySubPolicy3 : TransactionSubPolicy<FinalizeRequestTransaction<Request>, SerializeTransaction<Request>, Request> {
    override suspend fun <Ret> process(
        source: FinalizeRequestTransaction<Request>,
        dest: SerializeTransaction<Request>,
        
        state: SessionStartedState,
        next: suspend (Either<Throwable, SerializeTransaction<Request>>) -> Either<Throwable, Ret>
    ): Either<Throwable, Ret> {
    
        val result = "${source.request.target}\n${dest.request.target}\n${state.taskInfo.javaClass.name}\n${state.javaClass.name}"
        File("Output/p3.txt").writeText(result)
        
        return next(dest.right())
    }
}

class DummySubPolicy4 : TransactionSubPolicy<SerializeTransaction<Request>, ExportTransaction<Request>, Request> {
    override suspend fun <Ret> process(
        source: SerializeTransaction<Request>,
        dest: ExportTransaction<Request>,
        
        state: SessionStartedState,
        next: suspend (Either<Throwable, ExportTransaction<Request>>) -> Either<Throwable, Ret>
    ): Either<Throwable, Ret> {
    
        val result = "${source.request.target}\n${dest.request.target}\n${state.taskInfo.javaClass.name}\n${state.javaClass.name}"
        File("Output/p4.txt").writeText(result)
        
        return next(dest.right())
    }
}
