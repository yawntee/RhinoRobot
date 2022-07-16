package com.digyth.robot.rhino

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content

private const val LETTER_LIMIT = 500

fun MessageEvent.reply(msg: String) {
    reply(PlainText(msg))
}

fun MessageEvent.reply(msg: Message) {
    runBlocking {
        subject.sendMessage(
            message.quote() + msg.let {
                if (it.content.length > LETTER_LIMIT) it.content.substring(0, LETTER_LIMIT) else it.content
            }
        )
    }
}