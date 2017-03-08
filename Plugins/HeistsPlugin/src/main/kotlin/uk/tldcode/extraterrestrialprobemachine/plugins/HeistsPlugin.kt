package uk.tldcode.extraterrestrialprobemachine.plugins

import org.pircbotx.hooks.events.MessageEvent
import uk.tldcode.extraterrestrialprobemachine.api.*

class HeistsPlugin(name:String) : Plugin(name) {
    override fun PostInit() {

    }

    var Points: Plugin? = null
    override fun Init() {
        Points = PluginRegistry.Plugins()["Points"]
        val HeistsName = readConfigValue("name","heist")
        Commands.put(HeistsName,object: KotlinCommand() {
            override fun invoke(event: MessageEvent, respond: (String) -> Unit) {

            }

            override fun UserLevel(event: MessageEvent): UserLevel {
                return UserLevel.Caster
            }

            override fun match(message: String): Boolean {
                return message.startsWith("!$name")
            }

        })
    }

    init{
        Dependencies.add("Points")


    }
}