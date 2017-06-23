package rest

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import org.apache.http.impl.client.AbstractHttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.PoolingClientConnectionManager
import org.apache.http.params.CoreConnectionPNames
import org.apache.http.params.HttpParams

class Phase6Service extends RESTClient {

    def authToken = ""
    def userName = ""
    def clientId = "8bb2e7eb-6f94-4a86-b848-3369698ab57d"

    public Phase6Service(def url) {
        this.setUri(url)
        headers = [
                "Content-Type": "application/json;charset=utf-8",
                "Accept": "text/plain",
                "X-CLIENTID": clientId,
                "User-Agent": "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0"
        ]
        defaultRequestContentType = ContentType.JSON
        this.ignoreSSLIssues()
    }

    public AbstractHttpClient createClient(HttpParams params) {
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000)
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000)
        PoolingClientConnectionManager pcm = new PoolingClientConnectionManager()
        pcm.setMaxTotal(500)
        pcm.setDefaultMaxPerRoute(500);
        return new DefaultHttpClient(pcm, params)
    }

    public def login(def userName, def password) {
        def loginRequest = "{\"username\":\"" + userName + "\",\"password\":\"" + password + "\",\"remember\":false}"
        headers.put('X-LBTOKEN', userName)
        def response =  post(path: "/server.integration/login", body: loginRequest, requestContentType: ContentType.JSON, contentType: ContentType.JSON).data
        //print "login response: " + response
        authToken = response.replyContent.p6pSessionToken
        headers.put('X-JAUTHTOKEN', authToken)

        return response.replyContent
    }

    public def requestSubjectIds() {
        def subjectsRequest="{\"units\": [],\"subjectId\": null,\"filterMode\": \"LIBRARY\",\"cards\": []}"
        return post(path: "/server.integration/subjects", body: subjectsRequest).data.replyContent.ids
    }

    public def requestSubject(def ownerId, def subjectId) {
        def response =  get(path: "/server.integration/" + ownerId + "/subjects/" + subjectId).data
        return response.replyContent
    }

    public def createOrUpdateSubject(def ownerId, def subjectId, def createSubjectRequest) {
        def response =  post(path: "/server.integration/" + ownerId + "/subjects/" + subjectId, body: createSubjectRequest, requestContentType: ContentType.JSON, contentType: ContentType.JSON).data
        println response
    }

    public def requestUnitIds(def subjectId) {
        def unitRequest = "{\"units\":[],\"subjectId\":\"" + subjectId + "\",\"filterMode\":\"LIBRARY\",\"cards\":[]}"
        def response =  post(path: "/server.integration/subject/" + subjectId + "/units", body: unitRequest, requestContentType: ContentType.JSON, contentType: ContentType.JSON).data
        return response.replyContent.ids
    }

    public def requestUnit(def ownerId, def unitId) {
        def response =  get(path: "/server.integration/" + ownerId + "/units/" + unitId).data
        return response.replyContent
    }

    public def createOrUpdateUnit(def ownerId, def unitId, def createUnitRequest) {
        def response =  post(path: "/server.integration/" + ownerId + "/units/" + unitId, body: createUnitRequest, requestContentType: ContentType.JSON, contentType: ContentType.JSON).data
        // println response
    }

    public def requestCardList(def subjectId, def unitId) {
        def cardListRequest = "{\"units\":[\"" + unitId + "\"],\"subjectId\":\"" + subjectId + "\",\"filterMode\":\"LIBRARY\",\"cards\":[]}"
        def response =  post(path: "/server.integration/cardList", body: cardListRequest, requestContentType: ContentType.JSON, contentType: ContentType.JSON).data
        return response.replyContent.cards
    }

    public def createCard(def ownerId, def cardId, def createCardRequest) {
        def response =  post(path: "/server.integration/" + ownerId + "/cards/" + cardId, body: createCardRequest, requestContentType: ContentType.JSON, contentType: ContentType.JSON).data
        //println response
        return response
    }
}