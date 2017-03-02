package uk.tldcode.extraterrestrialprobemachine

import org.pircbotx.User
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.MessageEvent
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.LoggerFactory
import uk.tldcode.extraterrestrialprobemachine.api.PluginRegistry
import uk.tldcode.extraterrestrialprobemachine.api.UserLevel


class IRCListener : ListenerAdapter() {
    override fun onMessage(event: MessageEvent) {
        //When someone says ?helloworld respond with "Hello World"
        val tags = event.tags
        val caster = event.user?.nick.equals(event.channel.name.substring(1))
        val mod = tags["mod"].equals("1")
        val subscriber = tags["subscriber"].equals("1")
        val userlevel = if (caster) UserLevel.Caster else if (mod) UserLevel.Mod else if (subscriber) UserLevel.Subscriber else UserLevel.Viewer
        try {
            val commands = PluginRegistry.Plugins().values.flatMap { it.Commands.values.filter { it.UserLevel(event) <= userlevel } }.filter { it match event.message }
            commands.forEach { it(event) }
        }catch (t:Throwable){
            LoggerFactory.getLogger(this::class.java).error("Urgh!",t)
        }

    }

}