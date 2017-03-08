package uk.tldcode.extraterrestrialprobemachine

import org.simpleframework.http.Request
import org.simpleframework.http.Response
import org.simpleframework.http.core.Container
import uk.tldcode.extraterrestrialprobemachine.api.PluginRegistry

class HttpServer : Container {
    override fun handle(request: Request, response: Response) {
        val time = System.currentTimeMillis()
        response.setValue("Server", "ExtraTerrestrialProbeMachine/0.0.1 (Simple 6.0)")
        response.setDate("Date", time)
        response.setDate("Last-Modified", time)
        val parts = request.address.path.path.split("/").filterNot(String::isNullOrBlank)
        PluginRegistry.Plugins()[parts[0]]?.Web(request,response)
        response.printStream.close()

    }

}
