package com.github.nekdenis.weatherlogger.core.network.json

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import kotlin.reflect.KClass

typealias JsonMap = HashMap<String, Any>

class GsonMaker(val gson: Gson) : JsonMaker {

    val type: Type = object : TypeToken<JsonMap>() {}.type
    val listType: Type = object : TypeToken<List<JsonMap>>() {}.type
    
    override fun <T : Any> toJson(obj: T): String
            = gson.toJson(obj)

    override fun <T : Any> toJsonMap(obj: T): JsonMap {
        val json = gson.toJson(obj)
        return gson.fromJson<JsonMap>(json, type)
    }

    override fun <T : Any> toJsonListMap(obj: T): List<JsonMap> {
        val json = gson.toJson(obj)
        return gson.fromJson<List<JsonMap>>(json, listType)
    }

    override fun <T : Any> fromJson(json: String, cls: KClass<T>): T
            = gson.fromJson(json, cls.java)

    override fun <T : Any> fromJson(json: String, type: Type): T
            = gson.fromJson(json, type)

    override fun <T : Any> fromJson(json: Map<*, *>, cls: KClass<T>): T
            = gson.toJsonTree(json).let { gson.fromJson(it, cls.java) }

    override fun <T : Any> fromJson(json: Map<*, *>, type: Type): T
            = gson.toJsonTree(json).let { gson.fromJson(it, type) }

}