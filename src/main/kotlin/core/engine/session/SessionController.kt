package core.engine.session

import core.engine.UniqueKey

interface SessionDuplicateChecker{
    fun isDuplicateWithOtherSession(exclude : SessionController, key : UniqueKey)
}

class SessionController {
}