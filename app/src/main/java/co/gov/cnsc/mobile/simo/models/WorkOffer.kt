package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import co.gov.cnsc.mobile.simo.network.RestAPI
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de una oferta laboral
 */
data class WorkOffer(
        @SerializedName("id") val id: String?,
        @SerializedName("empleo") var job: Job? = null,
        @SerializedName("estadoInscripcion") var statusInscription: String? = null,
        @SerializedName("favorito") var favorite: Boolean,
        @SerializedName("inscripcionId") var inscriptionId: String? = null,
        @SerializedName("fechaInscripcion") val dateInscription: String? = null
) : Parcelable {

    val isFavoriteEnable: Boolean
        get() = SIMO.instance.isLogged &&
                (statusInscription == null || statusInscription?.contentEquals("F") == true)

    val isInscriptionEnable: Boolean
        get() = statusInscription?.contentEquals("I") == true

    val isPreInscriptionEnable: Boolean
        get() = statusInscription?.contentEquals("PI") == true

    /**
     * Variables para descargar manual de funciones (idFunctionsDocument, urlFileFunctionsDocument, nameFileFunctionsDocument)
     */

    val idFunctionsDocument: String?
        get() {

            val idFunctionsDocument = job?.document?.id.toString()
            return idFunctionsDocument

        }

    val urlFileFunctionsDocument: String?
        get() {
            return "${RestAPI.HOST}documents/get-document?docId=$idFunctionsDocument&contentType=application/pdf"
        }

    val nameFileFunctionsDocument: String?
        get() {
            return "$idFunctionsDocument.pdf"
        }


    class Deserializer : ResponseDeserializable<WorkOffer> {
        override fun deserialize(reader: Reader) = Gson().fromJson(reader, WorkOffer::class.java)
    }

    class ListDeserializer : ResponseDeserializable<List<WorkOffer>> {
        override fun deserialize(reader: Reader): List<WorkOffer> {
            val type = object : TypeToken<List<WorkOffer>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readParcelable<Job>(Job::class.java.classLoader),
            source.readString(),
            1 == source.readInt(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeParcelable(job, 0)
        writeString(statusInscription)
        writeInt((if (favorite) 1 else 0))
        writeString(inscriptionId)
        writeString(dateInscription)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<WorkOffer> = object : Parcelable.Creator<WorkOffer> {
            override fun createFromParcel(source: Parcel): WorkOffer = WorkOffer(source)
            override fun newArray(size: Int): Array<WorkOffer?> = arrayOfNulls(size)
        }
    }
}