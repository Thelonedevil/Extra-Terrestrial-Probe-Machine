package uk.tldcode.extraterrestrialprobemachine

import com.google.common.reflect.ClassPath
import groovy.lang.GroovyShell
import org.apache.bcel.classfile.ClassParser
import org.pircbotx.Configuration
import org.pircbotx.PircBotX
import org.pircbotx.cap.EnableCapHandler
import org.pircbotx.cap.TLSCapHandler
import uk.tldcode.extraterrestrialprobemachine.api.Plugin
import uk.tldcode.extraterrestrialprobemachine.api.PluginRegistry
import java.io.File
import java.net.URLClassLoader
import javax.net.ssl.SSLSocketFactory
import java.util.zip.ZipEntry
import java.io.FileInputStream
import java.util.zip.ZipInputStream
import java.util.ArrayList
import java.util.zip.ZipFile


fun main(args: Array<String>) {
    PluginRegistry.folder = System.getProperty("user.home") + File.separator + ".ExtraTerrestrialProbeMachine" + File.separator
    val folder=File(PluginRegistry.folder)
    if(!folder.exists()){
        folder.mkdirs()
    }
    val pluginFolder=File(PluginRegistry.folder+"plugins"+File.separator)
    if(!pluginFolder.exists()){
        pluginFolder.mkdirs()
    }
    val configFolder=File(PluginRegistry.folder+"config"+File.separator+"plugins"+File.separator)
    if(!configFolder.exists()){
        configFolder.mkdirs()
    }
    val dataFolder=File(PluginRegistry.folder+"data"+File.separator+"plugins"+File.separator)
    if(!dataFolder.exists()){
        dataFolder.mkdirs()
    }
    val configuration = Configuration.Builder()
            .setName(readConfigValue("BotName")) //Set the nick of the bot. CHANGE IN YOUR CODE
            .setServerPassword(readConfigValue("Oauth"))
            .addServer("irc.chat.twitch.tv", 443) //Join the freenode network
            .setSocketFactory(SSLSocketFactory.getDefault())
            .addCapHandler(TLSCapHandler(SSLSocketFactory.getDefault() as SSLSocketFactory?, true))
            .addCapHandler(EnableCapHandler("twitch.tv/membership"))
            .addCapHandler(EnableCapHandler("twitch.tv/tags"))
            .addCapHandler(EnableCapHandler("twitch.tv/commands"))
            .addAutoJoinChannel("#${readConfigValue("Channel")}") //Join the official #pircbotx channel
            .addListener(IRCListener()) //Add our listener that will be called on Events
    //Create our bot with the configuration
    loadPlugins()
    PluginRegistry.PluginPreInit(configuration)
    val bot = PircBotX(configuration.buildConfiguration())
    PluginRegistry.PluginInit(bot)
    bot.startBot()
    PluginRegistry.PluginPostInit()

}
fun readConfigValue(key: String): String {
    val file = File(PluginRegistry.folder+"config"+File.separator+"general.txt")
    if(!file.exists()){
        file.createNewFile()
        file.writeText("BotName:example\r\nOauth:oauth:example\r\nChannel:example\r\n")
    }
    return file.readLines().associateBy({ it.splitToSequence(":", ignoreCase = true, limit = 2).first() }, { it.splitToSequence(":", ignoreCase = true, limit = 2).last() })[key]!!
}
fun loadPlugins() {
    val groovyShell = GroovyShell()
    val jarFiles = HashSet<File>()
    val plugins = HashSet<String>()
    File(PluginRegistry.folder+"plugins"+File.separator).walk().maxDepth(2).forEach {
        if (it.isFile) {
            if (it.extension == "groovy") {
                val script = groovyShell.parse(it)
                val met = script.invokeMethod("GetConstructor", null)
                val con: (String) -> Plugin = {
                    ( met as org.codehaus.groovy.runtime.MethodClosure).call(it) as Plugin }
                PluginRegistry.Register(it.nameWithoutExtension, con)
            } else if (it.extension == "jar") {
                val zip = ZipFile(it)
                for (entry in zip.entries()){
                    if (!entry.isDirectory && entry.name.endsWith(".class")) {
                        val clazz = ClassParser(zip.getInputStream(entry),it.absolutePath+"!"+entry.name).parse()
                        if(clazz.superclassName == Plugin::class.java.canonicalName){
                            plugins.add(clazz.className)
                            jarFiles.add(it)
                        }
                    }
                }
            }
        }
    }

    val systemClassLoader = URLClassLoader(jarFiles.map { it.toURI().toURL() }.toTypedArray(), ClassLoader.getSystemClassLoader())
    ClassPath.from(systemClassLoader).topLevelClasses.filter { plugins.contains(it.name) }.forEach {
        val clazz = it.load()
        val con: (String) -> Plugin = {
            val con = clazz.getConstructor(String::class.java)
            con.newInstance(it) as Plugin
        }
        PluginRegistry.Register(it.simpleName.removeSuffix("Plugin"), con)
    }

}

