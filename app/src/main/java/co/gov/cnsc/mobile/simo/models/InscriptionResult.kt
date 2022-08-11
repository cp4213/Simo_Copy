package co.gov.cnsc.mobile.simo.models

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de un resultado de inscripción
 */
data class InscriptionResult(
        @SerializedName("id") val id: String?,
        @SerializedName("idInscripcion") val idInscription: String?,
        @SerializedName("idPrueba") val idTest: String?,
        @SerializedName("idEvaluaPrueba") val idCheckTest: String?,
        @SerializedName("prueba") val test: String?,
        @SerializedName("resultado") val result: String?,
        @SerializedName("puesto") val place: String?,
        @SerializedName("fechaPublicacion") val datePublication: String?,
        @SerializedName("calificacion") val calification: String?,
        @SerializedName("ponderacion") val ponderation: Int?,
        @SerializedName("valorAprobatorio") val valueProbative: String?
) {

    val isResultTest: Boolean
        get() = ponderation == null

    val isSummatoryScore: Boolean
        get() = ponderation != null

    class ListDeserializer : ResponseDeserializable<List<InscriptionResult>> {
        override fun deserialize(reader: Reader): List<InscriptionResult> {
            val type = object : TypeToken<List<InscriptionResult>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }
}