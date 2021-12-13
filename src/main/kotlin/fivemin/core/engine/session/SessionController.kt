package fivemin.core.engine.session

import fivemin.core.engine.UniqueKey

interface SessionDuplicateChecker{
    fun isDuplicateWithOtherSession(exclude : SessionController, key : UniqueKey)
}

class SessionController {
}