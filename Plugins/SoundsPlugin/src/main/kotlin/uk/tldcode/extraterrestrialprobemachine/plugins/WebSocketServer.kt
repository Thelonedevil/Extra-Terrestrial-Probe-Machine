package uk.tldcode.extraterrestrialprobemachine.plugins

import org.simpleframework.http.socket.Frame
import org.simpleframework.http.socket.FrameListener
import org.simpleframework.http.socket.Reason
import org.simpleframework.http.socket.Session
import java.lang.Exception
import org.simpleframework.http.socket.FrameType
import org.simpleframework.http.socket.service.Service
import org.slf4j.LoggerFactory
import org.simpleframework.http.socket.FrameChannel
import java.util.*


class WebSocketListener(val service: WebSocketServer) : FrameListener {

    override fun onFrame(connection: Session, frame: Frame) {
        if (frame.type == FrameType.TEXT) {
            LoggerFactory.getLogger(this::class.java).info(frame.text)
        }
    }

    override fun onError(connection: Session, cause: Exception) {

    }

    override fun onClose(connection: Session, reason: Reason) {
        val req = connection.request
        val name = req.path.path.removePrefix("/")
        service.sockets.remove(name)
    }

}

class WebSocketServer : Service {
    val sockets = HashMap<String, FrameChannel>()
    val listener = WebSocketListener(this)
    override fun connect(connection: Session) {
        val socket = connection.channel
        val req = connection.request
        val name = req.path.path.removePrefix("/")
        socket.register(listener)
        sockets.put(name, socket)
    }

}
