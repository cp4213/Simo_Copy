package co.gov.cnsc.mobile.simo.models

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de una categoría de trabajo
 */
data class CategoryJob(
        @SerializedName("id") val id: String,
        @SerializedName("descripcion") val description: String,
        @SerializedName("valor") val value: Int
) {

    class ListDeserializer : ResponseDeserializable<List<CategoryJob>> {
        override fun deserialize(reader: Reader): List<CategoryJob> {
            val type = object : TypeToken<List<CategoryJob>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

}