package dev.dani.velar.common.util


/*
 * Project: velar
 * Created at: 25/05/2025 19:28
 * Created by: Dani-error
 */
object ClassHelper {

    fun classExists(className: String): Boolean {
        try {
            val classLoader = ClassHelper::class.java.classLoader
            Class.forName(className, false, classLoader)
            return true
        } catch (exception: ClassNotFoundException) {
            return false
        }
    }

}