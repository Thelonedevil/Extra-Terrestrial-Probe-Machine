package uk.tldcode.extraterrestrialprobemachine.plugins

import org.pircbotx.hooks.events.MessageEvent
import org.simpleframework.http.Request
import org.simpleframework.http.Response
import uk.tldcode.extraterrestrialprobemachine.api.UserLevel
import uk.tldcode.extraterrestrialprobemachine.api.Command
import uk.tldcode.extraterrestrialprobemachine.api.KotlinCommand
import uk.tldcode.extraterrestrialprobemachine.api.Plugin



class HelloWorldPlugin(name:String) : Plugin(name) {
    override fun PostInit() {

    }
    override fun Web(request: Request,response: Response){

    }
    override fun Init() {
        Commands.put("HelloWorld", object: KotlinCommand() {
            override fun invoke(event:MessageEvent,respond: (String) -> Unit) {
                respond("Hello, World!")
            }

            override fun UserLevel(event: MessageEvent): UserLevel = UserLevel.Caster

            override fun match(message: String): Boolean {
                return message.startsWith("?HelloWorld")
            }
        })
    }

    init {

    }
}
