package chat

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.concurrent.thread
import kotlin.system.exitProcess

var port = 9999
var host = "127.0.0.1"

if (args.isNotEmpty()) {
    host = args[0]
    if (args.size == 2) {
        port = args[1].toInt()
    }
}
val startMsgIndex = 6
val maxSize = 1024
val socket = Socket(host, port)
println("Connect to Server on address: $host, port: $port")
println("Enter your name: ")

val bufferedReader = BufferedReader(InputStreamReader(System.`in`))
val username: String = bufferedReader.readLine()
println("Welcome to the chat, $username!")
send(username)

thread { receive() }
thread { write() }

fun receive() {
    try {
        while (true) {
            val buf = ByteArray(maxSize)
            socket.getInputStream().read(buf)
            val msgLength = buf.first()
            val msg = String(buf.slice(startMsgIndex until startMsgIndex + msgLength).toByteArray())
            val time = String(buf.slice(1 until startMsgIndex).toByteArray())
            val nameLength = buf[startMsgIndex + msgLength]
            val name =
                String(buf.slice(startMsgIndex + msgLength + 1..startMsgIndex + msgLength + nameLength).toByteArray())
            println("$time [$name]: $msg")
        }
    } catch (e: java.net.SocketException) {
        println("Server shutdown")
        exitProcess(0)
    }
}

fun write() {
    val reader = BufferedReader(InputStreamReader(System.`in`))
    while (true) {
        val msg = reader.readLine()
        if (msg.isNotEmpty()) {
            send(msg)
        }
    }
}

fun send(msg: String) {
    var mMsg = msg
    val msgLength = mMsg.length

    if (msgLength > Byte.MAX_VALUE) {
        mMsg = mMsg.dropLast(msgLength - Byte.MAX_VALUE)
        println(mMsg)
    }

    val msgLengthByte = mMsg.length.toByte()
    val timeString = SimpleDateFormat("HH:mm").format(Date())

    val resByteArray = byteArrayOf(msgLengthByte) + timeString.toByteArray() + mMsg.toByteArray()
    socket.getOutputStream().write(resByteArray)
}