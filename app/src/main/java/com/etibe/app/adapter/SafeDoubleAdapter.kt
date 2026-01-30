package com.etibe.app.adapter

import com.google.gson.JsonDeserializer
import com.google.gson.JsonDeserializationContext
import java.lang.reflect.Type
import com.google.gson.JsonElement


class SafeDoubleAdapter : JsonDeserializer<Double> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Double {
        return try {
            when {
                json.isJsonNull -> 0.0
                json.isJsonPrimitive && json.asJsonPrimitive.isString ->
                    json.asString.replace(",", "").toDouble()

                else ->
                    json.asDouble
            }
        } catch (e: Exception) {
            0.0
        }
    }
}