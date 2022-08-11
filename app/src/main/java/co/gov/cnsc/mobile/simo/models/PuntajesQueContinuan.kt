package co.gov.cnsc.mobile.simo.models

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos del objeto con atributos id (número de inscripción) y calificación (puntaje)
 * para el listado de aspirantes inscritos en un empleo quienes continúan en concurso
 */
data class PuntajesQueContinuan (
        @SerializedName ("id") val idInscripcion: String?,
        @SerializedName ("calificacion") val puntaje: String?) {

    class ListDeserializer : ResponseDeserializable<List<PuntajesQueContinuan>> {
        override fun deserialize(reader: Reader): List<PuntajesQueContinuan> {
            val type = object : TypeToken<List<PuntajesQueContinuan>> () {}.type
            //return super.deserialize(reader)
            return Gson().fromJson(reader, type)
        }
    }
}

