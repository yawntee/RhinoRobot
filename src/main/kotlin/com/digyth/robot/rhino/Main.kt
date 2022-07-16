package com.digyth.robot.rhino

import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.event.events.GroupMessageEvent
import java.util.Scanner

class Main {
    companion object {

        lateinit var bot: Bot

        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                bot = BotFactory.newBot(args[0].toLong(), args[1]) {
                    fileBasedDeviceInfo()
                }
                bot.eventChannel.subscribeAlways(GroupMessageEvent::class.java, handler = MyConsumer)
                bot.alsoLogin()
            }
            val sc = Scanner(System.`in`)
            while (sc.hasNext()) {
                when (sc.next()) {
                    "exit" -> {
                        bot.cancel()
                        return
                    }
                }
            }
        }
    }
}