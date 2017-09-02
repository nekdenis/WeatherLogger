package com.github.nekdenis.weatherlogger.core.network.json

import java.lang.reflect.Type
import kotlin.reflect.KClass

interface JsonMaker {
    fun <T : Any> toJson(obj: T): String
    fun <T : Any> toJsonMap(obj: T): HashMap<String, Any>
    fun <T : Any> toJsonListMap(obj: T): List<HashMap<String, Any>>

    fun <T : Any> fromJson(json: String, cls: KClass<T>): T

    fun <T : Any> fromJson(json: String, type: Type): T
    fun <T : Any> fromJson(json: Map<*, *>, cls: KClass<T>): T

    fun <T : Any> fromJson(json: Map<*, *>, type: Type): T
}

inline fun <reified T : Any> JsonMaker.fromJson(json: String) = fromJson(json, T::class)
