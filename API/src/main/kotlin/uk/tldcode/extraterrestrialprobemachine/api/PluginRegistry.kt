package uk.tldcode.extraterrestrialprobemachine.api

import com.google.common.collect.ImmutableMap
import groovy.lang.Closure
import org.codehaus.groovy.runtime.MethodClosure
import org.pircbotx.Configuration
import org.pircbotx.PircBotX
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.MessageEvent
import org.simpleframework.http.Request
import org.simpleframework.http.Response
import org.simpleframework.http.socket.service.Service
import org.slf4j.LoggerFactory
import uk.tldcode.extraterrestrialprobemachine.api.dependencies.Dependency
import java.io.File
import java.util.concurrent.Executors
import java.util.function.Consumer


object PluginRegistry {
    var folder: String = ""
    private val prePlugins: HashMap<String, (String) -> Plugin> = HashMap()
    private val plugins: HashMap<String, Plugin> = HashMap()
    private val Logger = LoggerFactory.getLogger(this::class.java)
    fun Plugins(): ImmutableMap<String, Plugin> {
        return ImmutableMap.copyOf(plugins)
    }

    fun PluginPreInit(configuration: Configuration.Builder) {
        for (pluginCon in ImmutableMap.copyOf(prePlugins)) {
            val plugin = pluginCon.value(pluginCon.key)
            if (!prePlugins.keys.containsAll(plugin.Dependencies)) {
                Logger.warn("Missing dependencies for ${plugin.name}: ${plugin.Dependencies.dropWhile { prePlugins.keys.contains(it) }}")
                prePlugins.remove(plugin.name)
            }
        }
        val temp = (prePlugins.map { Pair(it.key, it.value(it.key)) }.toMap())
        val order = Dependency(temp.mapValues { it.value.Dependencies.toList() })
        while (order.size > 0) {
            val key = order.pop()
            val plugin = temp[key]!!
            configuration.addListener(plugin)
            plugins.put(key, plugin)

        }
    }

    fun PluginInit(bot: PircBotX) {
        plugins.entries.parallelStream().forEach {
            it.value.bot = bot
            it.value.Init()
            Logger.info("${it.key} initialized")
        }
    }

    fun PluginPostInit() {
        plugins.values.parallelStream().forEach { it.PostInit() }
    }

    fun Register(name: String, pluginCon: (String) -> Plugin) {
        prePlugins.put(name, pluginCon)
    }
}

abstract class Plugin(val name: String) : ListenerAdapter() {
    val Dependencies: LinkedHashSet<String> = LinkedHashSet()
    val Commands: HashMap<String, Command> = HashMap()
    var WebSocket : Service? = null
    var bot: PircBotX? = null
    val Logger = LoggerFactory.getLogger(this::class.java)
    fun getUsers(): List<String> {
        return bot!!.userChannelDao.getUsers(bot!!.userChannelDao.getChannel("#the_lone_devil")).map { it.nick }
    }
    open fun Web(request: Request, response: Response){

    }
    abstract fun Init()
    abstract fun PostInit()
    var Scheduler = Executors.newScheduledThreadPool(10)!!
    val ConnectionString = "jdbc:h2:" + PluginRegistry.folder + File.separator + "data" + File.separator + "plugins" + File.separator + "$name.db;AUTO_SERVER=TRUE"
    val ConnectionDriver = "org.h2.Driver"
    fun readConfigValue(key: String, default: String): String {
        val file = File(PluginRegistry.folder + File.separator + "config" + File.separator + "plugins" + File.separator + "$name.txt")
        if (!file.exists()) {
            file.createNewFile()
        }
        val result = file.readLines().associateBy({ it.splitToSequence(":", ignoreCase = true, limit = 2).first() }, { it.splitToSequence(":", ignoreCase = true, limit = 2).last() })[key]
        if (result != null) {
            return result
        }
        file.appendText("$key:$default\r\n")
        return default
    }


}

interface Command {
    operator fun invoke(event: MessageEvent)
    fun UserLevel(event: MessageEvent): UserLevel
    infix fun match(message: String): Boolean
}

abstract class KotlinCommand : Command {
    override operator fun invoke(event: MessageEvent) {
        this(event, { event.respondChannel(it) })
    }

    abstract operator fun invoke(event: MessageEvent, respond: (String) -> Unit)
}
abstract class JavaCommand : Command {
    override operator fun invoke(event: MessageEvent) {
        this(event, Consumer<String> { t -> event.respondChannel(t) })
    }

    abstract operator fun invoke(event: MessageEvent, respond: Consumer<String>)
}

abstract class ScalaCommand : Command {
    override operator fun invoke(event: MessageEvent) {
        this(event,
            scala.Function1<String, Unit>({
                event.respondChannel(it)
            })
        )
    }

    abstract operator fun invoke(event: MessageEvent, respond: scala.Function1<String, Unit>)
}
abstract class GroovyCommand:Command{
    override operator fun invoke(event: MessageEvent) {
        this(event,object:Closure<Unit>(null){
            override fun call(args: Any): Unit {
                return event.respondChannel(args.toString())
            }
        })
    }

    abstract operator fun invoke(event: MessageEvent, respond: Closure<Unit>)
}