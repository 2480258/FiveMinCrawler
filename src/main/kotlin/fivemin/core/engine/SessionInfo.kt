package fivemin.core.engine

import arrow.core.Option
import fivemin.core.LoggerController

class SessionInfo
constructor(
    private val finish: FinishObserver,
    val parent: Option<SessionToken>
) {
    
    companion object {
        private val logger = LoggerController.getLogger("SessionInfo")
    }
    
    val token: SessionToken = SessionToken.create()
    val isDetachable: DetachableState
        get() {
            return detachable
        }
    private var detachable = DetachableState.NOTMODIFIED
    private var progress = ProgressState.STARTED
    
    private var reenterent = 0
    
    init {
        finish.onStart()
    }
    
    suspend fun <T> doRegisteredTask(func: suspend () -> T): T {
        try {
            reenterent++
            return func()
        }
        finally {
            reenterent--
            
            if (reenterent == 0) {
                setFinished()
            }
        }
    }
    
    private fun setFinished() {
        if (progress != ProgressState.STARTED) {
            throw IllegalStateException()
        }
        
        progress = ProgressState.FINISHED
        
        if (detachable == DetachableState.WANT) {
            finish.onExportableFinish(token)
        } else {
            finish.onFinish(token)
        }
    }
    
    fun setDetachable() {
        if (progress != ProgressState.STARTED) {
            throw IllegalStateException()
        }
        
        detachable = DetachableState.WANT
    }
    
    fun setNonDetachable() {
        
        if (progress != ProgressState.STARTED) {
            throw IllegalStateException()
        }
        
        detachable = DetachableState.HATE
    }
}