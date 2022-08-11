package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.Reader

/**
 * Datos de un Resultado General de Inscripción
 */
data class TotalResult(
        @SerializedName("id") val id: String?,
        @SerializedName("calificacion") val qualification: Float = 0f,
        @SerializedName("sigueConcurso") val continueCompetition: Boolean = false,
        @SerializedName("estado") val status: String?,
        @SerializedName("puesto") val place: String?,
        @SerializedName("claseReclamacionId") val classReclamation: String?
) : Parcelable {

    class Deserializer : ResponseDeserializable<TotalResult> {
        override fun deserialize(reader: Reader) = Gson().fromJson(reader, TotalResult::class.java)
    }


    constructor(source: Parcel) : this(
            source.readString(),
            source.readFloat(),
            1 == source.readInt(),
            source.readString(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeFloat(qualification)
        writeInt((if (continueCompetition) 1 else 0))
        writeString(status)
        writeString(place)
        writeString(classReclamation)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<TotalResult> = object : Parcelable.Creator<TotalResult> {
            override fun createFromParcel(source: Parcel): TotalResult = TotalResult(source)
            override fun newArray(size: Int): Array<TotalResult?> = arrayOfNulls(size)
        }
    }
}