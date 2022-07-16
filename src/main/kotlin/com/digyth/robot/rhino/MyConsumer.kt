package com.digyth.robot.rhino

import com.digyth.robot.rhino.exception.RestrictException
import com.digyth.robot.rhino.plugin.InitializationFunctions
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.content
import java.util.concurrent.LinkedBlockingQueue
import java.util.function.Consumer

object MyConsumer : Consumer<GroupMessageEvent>, InitializationFunctions.Callback {


    private val queue = LinkedBlockingQueue<GroupMessageEvent>()

    private var lastErr: String = "No Error"
    var validToPrint = 0
    var hasReturn = true

    private lateinit var event: MessageEvent
    private lateinit var engine: MyRhinoEngine

    override fun accept(e: GroupMessageEvent) {
        queue.put(e)
    }

    private fun parse(e: GroupMessageEvent) {
        reset(e)
        when (val msg = e.message.content) {
            "#err" -> e.reply(lastErr)
            else -> execute(msg, e)
        }
    }

    private fun reset(e: GroupMessageEvent) {
        event = e
        validToPrint = Configs.PRINT_LIMIT
        hasReturn = true
    }


    init {
        // merge all messages into one thread
        Thread {
            engine = MyRhinoEngine(this)
            while (true) parse(queue.take())
        }.apply {
            isDaemon = true
            start()
        }
    }

    private fun execute(script: String, event: GroupMessageEvent) {
        try {
            return runBlocking {
                suspendCancellableCoroutine {
                    engine.eval(script)?.let {
                        if (hasReturn)
                            runBlocking {
                                event.subject.sendMessage(event.message.quote() + it)
                            }
                    }
                    it.resumeWith(Result.success(Unit))
                }
            }
        } catch (e: RestrictException) {
            event.reply(e.message!!)
        } catch (e: StackOverflowError) {
            event.reply("Stack Overflow")
        } catch (e: Exception) {
            lastErr = e.message ?: run {
                e.printStackTrace()
                "Unknown Error"
            }
        }
    }

    override fun print(str: String) {
        if (validToPrint > 0) {
            event.reply(str)
            validToPrint--
        } else {
            forceStop("Maximum number of print is ${Configs.PRINT_LIMIT}")
        }
    }

    override fun forceStop(msg: String) {
        throw RestrictException(msg)
    }

}