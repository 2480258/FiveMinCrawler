package core.request.cookie

interface CookieResolveTarget{
    fun <Ret> sync(func : () -> Ret) : Ret
}