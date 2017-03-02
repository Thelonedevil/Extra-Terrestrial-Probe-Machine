package uk.tldcode.extraterrestrialprobemachine.plugins


import org.pircbotx.hooks.events.MessageEvent
import uk.tldcode.extraterrestrialprobemachine.api.{Command, Plugin, ScalaCommand, UserLevel}

/**
  * Created by Justin on 02/03/2017.
  */
class ScalaTestPlugin(name:String) extends Plugin(name){
  override def PostInit(): Unit = ()

  override def Init(): Unit = {
    getCommands.put("!SCALA",new ScalaCommand(){
      def invoke(event: MessageEvent, respond:String=>kotlin.Unit) {
        respond("SCALA!")
      }

      def UserLevel(event: MessageEvent): UserLevel = uk.tldcode.extraterrestrialprobemachine.api.UserLevel.Caster

      def `match`(message: String): Boolean = message.startsWith("!SCALA")
    })
    ()
  }
}
