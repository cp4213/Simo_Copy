package co.gov.cnsc.mobile.simo.models

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.Reader

/**
 * Datos de un Archivo SIMO
 */
data class File(
        @SerializedName("file") val file: String,
        @SerializedName("name") val name: String,
        @SerializedName("contentType") val contentType: String,
        @SerializedName("stageId") val stageId: String
) {

    class Deserializer : ResponseDeserializable<File> {
        override fun deserialize(reader: Reader) = Gson().fromJson(reader, File::class.java)
    }
}