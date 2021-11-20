package core.request

import core.request.cookie.CookiedRequester

class HttpRequestProcedure : CookiedRequester {
}

class HttpRequesterConfig (val requesterConfig: RequesterConfig, val requestHeaderProfile: RequestHeaderProfile){

}


