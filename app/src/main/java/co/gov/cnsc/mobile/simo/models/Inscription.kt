package co.gov.cnsc.mobile.simo.models

import co.gov.cnsc.mobile.simo.network.RestAPI
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de un empleo Inscrito, Preinscrito o Favorito
 */
data class Inscription(
        @SerializedName("id") val id: String,
        @SerializedName("convocatoriaNombre") val convocatoryName: String,
        @SerializedName("entidadNombre") val entityName: String,
        @SerializedName("empleoId") val jobId: String,
        @SerializedName("denominacionNombre") val denominationName: String,
        @SerializedName("estado") val status: IdName?,
        @SerializedName("convocatoriaId") val convocatoryId: String,
        @SerializedName("codigoEmpleo") val codeJob: String?,
        @SerializedName("grado") val grade: Int?,
        @SerializedName("reporteInscripcionId") val reportInscriptionId: String?,
        @SerializedName("totalCiudadanosInscritos") val totalPeople: Int?,
        @SerializedName("sigueConcurso") val followCouncourse: Boolean,
        @SerializedName("nivel") val level: Any?,
        @SerializedName("estadoPago") val statusPay: Any?
) {

    val statusInscription: String?
        get() = status?.id

    val isFavorite: Boolean
        get() {
            return status?.id == "F"
        }

    val isPreApplicant: Boolean
        get() {
            return status?.id == "PI"
        }

    val isApplicant: Boolean
        get() {
            return status?.id == "I"
        }

    val urlFileConstancy: String?
        get() {
            return "${RestAPI.HOST}documents/get-document?docId=$reportInscriptionId&contentType=application/pdf"
        }

    val nameFileConstancy: String?
        get() {
            return "$reportInscriptionId.pdf"
        }

    class ListDeserializer : ResponseDeserializable<List<Inscription>> {
        override fun deserialize(reader: Reader): List<Inscription> {
            val type = object : TypeToken<List<Inscription>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }
}