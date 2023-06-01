package com.test.vapdemo

import java.lang.reflect.Method

/**
 * @Description 反射获取对象里面的方法或变量
 */
object FieldUtils {

    /**
     * 获取某个私有变量的值
     */
    fun <T> T.getFieldValue(fieldName: String): Any? {
        try {
            val fieldValue = (this as Any).javaClass.getDeclaredField(fieldName)
            fieldValue.isAccessible = true
            return fieldValue[this]
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 执行某个方法
     * @param methodName 方法名
     * @param param 对应参数
     */
    fun <T> T.invokeMethod(methodName: String, vararg param: Param?): Any? {
        try {
            val paramClazz: Array<Class<*>?> = arrayOfNulls(param.size)
            val paramValue: Array<Any?> = arrayOfNulls(param.size)
            for (i in param.indices) {
                param[i]?.apply {
                    paramClazz[i] = this.paramClazz
                    paramValue[i] = this.value
                }
            }
            val method: Method = (this as Any).javaClass.getMethod(methodName, *paramClazz)
            return method.invoke(this, *paramValue)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 参数类型和值
     */
    data class Param(
        var paramClazz: Class<*>? = null,
        var value: Any? = null
    )
}