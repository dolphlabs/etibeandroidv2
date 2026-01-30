package com.etibe.app.adapter

import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonDeserializationContext
import java.lang.reflect.Type


class SafeIntAdapter : JsonDeserializer<Int> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Int {
        return try {
            when {
                json.isJsonNull -> 0
                json.asString.contains(".") ->
                    json.asDouble.toInt()

                else ->
                    json.asInt
            }
        } catch (e: Exception) {
            0
        }
    }
}