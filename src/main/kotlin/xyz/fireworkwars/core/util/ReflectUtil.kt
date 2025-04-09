package xyz.fireworkwars.core.util

import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minecraft.MethodsReturnNonnullByDefault
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.function.Supplier

@MethodsReturnNonnullByDefault
@Suppress("unused", "MemberVisibilityCanBePrivate")
class ReflectUtil {
    private var reflecting = false
    private var logger: ComponentLogger? = null

    private var clazz: Class<*>? = null
    private var instance: Any? = null

    fun useLogger(logger: ComponentLogger) {
        this.logger = logger
    }

    fun getField(clazz: Class<*>, fieldName: String): Field {
        try {
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true
            return field
        } catch (e: NoSuchFieldException) {
            logger!!.error("Failed to get field: {}", e.message)
        }

        throw RuntimeException("Failed to get field!")
    }

    fun getField(fieldName: String): Field {
        check(reflecting) { "Cannot get field without class context outside a reflection function!" }

        return this.getField(clazz!!, fieldName)
    }

    fun <T> getFieldValue(field: Field, instance: Any): T {
        try {
            @Suppress("UNCHECKED_CAST")
            return field[instance] as T
        } catch (e: IllegalAccessException) {
            logger!!.error("Failed to get field value: {}", e.message)
        }

        throw RuntimeException("Failed to get field value!")
    }

    fun <T> getFieldValue(field: Field): T {
        check(reflecting) { "Cannot get field value without class context outside a reflection function!" }

        return this.getFieldValue(field, instance!!)
    }

    fun <T> getFieldValue(clazz: Class<*>, fieldName: String, instance: Any): T {
        val field = this.getField(clazz, fieldName)
        return this.getFieldValue(field, instance)
    }

    fun <T> getFieldValue(fieldName: String): T {
        check(reflecting) { "Cannot get field value without class context outside a reflection function!" }

        return this.getFieldValue(clazz!!, fieldName, instance!!)
    }

    fun setFieldValue(field: Field, instance: Any, value: Any?) {
        try {
            field[instance] = value
        } catch (e: IllegalAccessException) {
            logger!!.error("Failed to set field value: {}", e.message)
        }
    }

    fun setFieldValue(field: Field, value: Any?) {
        check(reflecting) { "Cannot set field value without class context outside a reflection function!" }

        this.setFieldValue(field, instance!!, value)
    }

    fun setFieldValue(clazz: Class<*>, fieldName: String, instance: Any, value: Any?) {
        val field = getField(clazz, fieldName)
        this.setFieldValue(field, instance, value)
    }

    fun setFieldValue(fieldName: String, value: Any?) {
        check(reflecting) { "Cannot set field value without class context outside a reflection function!" }

        this.setFieldValue(clazz!!, fieldName, instance!!, value)
    }

    fun getMethod(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>): Method {
        try {
            val method = clazz.getDeclaredMethod(methodName, *parameterTypes)
            method.isAccessible = true
            return method
        } catch (e: NoSuchMethodException) {
            logger!!.error("Failed to get method: {}", e.message)
        }

        throw RuntimeException("Failed to get method!")
    }

    fun getMethod(methodName: String, vararg parameterTypes: Class<*>): Method {
        check(reflecting) { "Cannot get method without class context outside a reflection function!" }

        return this.getMethod(clazz!!, methodName, *parameterTypes)
    }

    fun <T> invokeMethod(method: Method, instance: Any, vararg args: Any?): T {
        try {
            @Suppress("UNCHECKED_CAST")
            return method.invoke(instance, *args) as T
        } catch (e: Exception) {
            logger!!.error("Failed to invoke method: {}", e.message)
        }

        throw RuntimeException("Failed to invoke method!")
    }

    fun <T> invokeMethod(method: Method, vararg args: Any): T {
        check(reflecting) { "Cannot invoke method without class context outside a reflection function!" }

        return this.invokeMethod(method, instance!!, *args)
    }

    fun <T> invokeMethod(
        clazz: Class<*>,
        methodName: String,
        parameterTypes: Array<Class<*>>,
        instance: Any,
        vararg args: Any?
    ): T {
        val method = getMethod(clazz, methodName, *parameterTypes)
        return this.invokeMethod(method, instance, *args)
    }

    fun <T> invokeMethod(methodName: String, parameterTypes: Array<Class<*>>, vararg args: Any?): T {
        check(reflecting) { "Cannot invoke method without class context outside a reflection function!" }

        return this.invokeMethod(clazz!!, methodName, parameterTypes, instance!!, *args)
    }

    fun reflect(clazz: Class<*>, instance: Any, runnable: Runnable) {
        reflecting = true
        this.clazz = clazz
        this.instance = instance

        runnable.run()

        reflecting = false
        this.clazz = null
        this.instance = null
    }

    fun <T> reflect(clazz: Class<*>, instance: Any, supplier: Supplier<T>): T {
        reflecting = true
        this.clazz = clazz
        this.instance = instance

        val value = supplier.get()

        reflecting = false
        this.clazz = null
        this.instance = null

        return value
    }
}