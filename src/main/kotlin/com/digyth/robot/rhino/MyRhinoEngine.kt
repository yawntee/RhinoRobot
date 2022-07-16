package com.digyth.robot.rhino

import com.digyth.robot.rhino.plugin.InitializationFunctions
import org.mozilla.javascript.*
import org.mozilla.javascript.ast.*

class MyRhinoEngine(callback: InitializationFunctions.Callback) {

    private val ctx = Context.enter()
    private val scope = ctx.initStandardObjects()
    private val env = CompilerEnvirons()

    init {
        ctx.optimizationLevel = -1
        ctx.languageVersion = Context.VERSION_ES6
        ctx.setClassShutter(MyClassShutter)
        ctx.maximumInterpreterStackDepth = Configs.MAX_DEPTH
        env.languageVersion = Context.VERSION_ES6
        InitializationFunctions.init(callback, scope, true)
    }

    fun eval(script: String): String? {
        val ast = Parser(env).parse(script, null, 1)
        if (ast.firstChild == ast.lastChild) {
            val node = ast.firstChild
            if (node is Name) {
                if (!node.identifier.matches("_?\\w+".toRegex())) {
                    return null
                }
            }
        }
        checkValid(ast)
        checkLoop(ast)
        val rs = ctx.evaluateString(scope, ast.toSource(), "", 1, null)
        return if (rs is Undefined || rs is Callable) null else Context.jsToJava(rs, String::class.java).toString()
    }

    private fun checkValid(ast: AstRoot) {
        val lastStmt = ast.lastChild
        if (lastStmt is ExpressionStatement) {
            val lastExpr = lastStmt.expression
            when (lastExpr) {
                is Assignment, is NumberLiteral, is KeywordLiteral -> MyConsumer.hasReturn = false
            }
        }
    }

    private fun checkLoop(ast: AstRoot) {
        val loops = ast.collect<Loop>()
        for (loop in loops) {
            val checker = buildChecker("_" + System.currentTimeMillis())
            loop.parent.addChildBefore(checker.first, loop)
            if (loop.body !is Block) {
                val block = Block()
                if (loop.body !is EmptyStatement) block.addChild(loop.body)
                loop.body = block
            }
            loop.body.addChildToFront(checker.second)
        }
    }

    private fun buildChecker(name: String): Pair<VariableDeclaration, IfStatement> {
        val ast = Parser(env).parse(
            """
            let $name=0;
            if(++$name>${Configs.LOOP_COUNT}){
                forceStop("Maximum loop count is ${Configs.LOOP_COUNT}");
            }
        """.trimIndent(), null, 1
        )
        val first = ast.firstChild as VariableDeclaration
        val second = ast.firstChild.next as IfStatement
        ast.removeChild(second)
        return Pair(first, second)
    }

    private inline fun <reified T> AstRoot.collect(): List<T> {
        val list = mutableListOf<T>()
        this.visitAll { node ->
            if (node is T) {
                list.add(node)
            }
            true
        }
        return list
    }

}