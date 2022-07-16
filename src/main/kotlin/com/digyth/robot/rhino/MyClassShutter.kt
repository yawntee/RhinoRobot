package com.digyth.robot.rhino

import org.mozilla.javascript.ClassShutter

object MyClassShutter : ClassShutter {

    private val blacklist = arrayOf(
        "java.lang.SecurityManager.*".toRegex(),
        // forbidden shell
        "java.lang.Runtime.*".toRegex(),
        "java.lang.Process.*".toRegex(),
        // forbidden reflect
        "java.lang.reflect.*".toRegex(),
        // forbidden io
        "java.io.*".toRegex(),
        // forbidden System eliminate currentTimeMillis()
        "java.lang.System.*".toRegex(),
        // forbidden printStackTrace
        ".*(Throwable|Exception).*".toRegex(),
    )

    override fun visibleToScripts(fullClassName: String): Boolean {

        for (regex in blacklist) {
            if (regex.matches(fullClassName)) {
                return false
            }
        }
        return true
    }

}