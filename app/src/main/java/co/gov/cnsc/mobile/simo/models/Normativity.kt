package co.gov.cnsc.mobile.simo.models

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de una Normatividad
 */
class Normativity(
        @SerializedName("id") val id: String,
        @SerializedName("numero") val number: String?,
        @SerializedName("descripcion") val description: String?,
        @SerializedName("fecha") val date: String?,
        @SerializedName("fechaPublicacion") val datePublish: String?,
        @SerializedName("documento") val document: Document
) {

    val nameDocument: String?
        get() {
            return "${document.name}.pdf"
        }

    val urlDocument: String?
        get() {
            return document.urlFile
        }

    class Deserializer : ResponseDeserializable<Normativity> {
        override fun deserialize(reader: Reader) = Gson().fromJson(reader, Normativity::class.java)
    }

    class ListDeserializer : ResponseDeserializable<List<Normativity>> {
        override fun deserialize(reader: Reader): List<Normativity> {
            val type = object : TypeToken<List<Normativity>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }
}