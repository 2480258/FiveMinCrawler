package com.fivemin.core.engine.session

import com.fivemin.core.engine.UniqueKey

interface SessionDuplicateChecker{
    fun isDuplicateWithOtherSession(exclude : SessionController, key : UniqueKey)
}

class SessionController