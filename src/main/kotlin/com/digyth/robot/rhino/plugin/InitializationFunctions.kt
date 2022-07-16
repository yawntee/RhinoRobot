package com.digyth.robot.rhino.plugin

import org.mozilla.javascript.*

class InitializationFunctions(private val callback: Callback) : IdFunctionCall {

    companion object {
        private const val arity = 1
        private const val FTAG = "BaseEnv"

        //ID
        private enum class ID {
            print, importClass, forceStop,
        }

        private val ids = ID.values()

        fun init(callback: Callback, scope: Scriptable, sealed: Boolean = false) {
            val obj = InitializationFunctions(callback)
            for (value in ID.values()) {
                val f = IdFunctionObject(obj, FTAG, value.ordinal + 1, value.name, arity, scope)
                if (sealed) f.sealObject()
                ScriptableObject.defineProperty(
                    scope, f.functionName, f,
                    ScriptableObject.DONTENUM or ScriptableObject.CONST
                )
            }
        }
    }

    override fun execIdCall(
        f: IdFunctionObject,
        cx: Context,
        scope: Scriptable,
        thisObj: Scriptable,
        args: Array<out Any>
    ): Any {
        if (f.hasTag(FTAG)) {
            val id = f.methodId()
            if (id > 0 && id <= ids.size)
                return when (ID.values()[id - 1]) {
                    ID.print -> {
                        callback.print(args.joinToString("\t") { ScriptRuntime.toString(it) })
                        Undefined.instance
                    }
                    ID.importClass -> {
                        if (args.isNotEmpty() && args[0] is NativeJavaClass) {
                            val clazz = args[0] as NativeJavaClass
                            scope.put(clazz.classObject.simpleName, scope, clazz)
                        }
                        Undefined.instance
                    }
                    ID.forceStop -> {
                        callback.forceStop(if (args.isNotEmpty()) ScriptRuntime.toString(args[0]) else "Restrict Triggered")
                        Undefined.instance
                    }
                }
        }
        throw f.unknown()
    }

    interface Callback {
        fun print(str: String)
        fun forceStop(msg: String)
    }
}