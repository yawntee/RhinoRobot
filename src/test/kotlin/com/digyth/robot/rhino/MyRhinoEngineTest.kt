package com.digyth.robot.rhino

import org.mozilla.javascript.Parser
import org.mozilla.javascript.ast.ExpressionStatement

internal class MyRhinoEngineTest {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println((Parser().parse("1",null,1).firstChild as ExpressionStatement).expression.javaClass)
        }
    }

}