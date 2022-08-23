package co.gov.cnsc.mobile.simo.models


import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

data class AudienceVacancy (
    @SerializedName("id") val id: String?,
    @SerializedName("idPublicaElegible") val id_PublicaE: String?,
    @SerializedName("numeroOPEC") val numeroOPEC: String?,
    @SerializedName("idVacante") val idVacante: String?,
    @SerializedName("numeroPlazas") val numeroPlazas: String?,
    @SerializedName("dependencia") val dependencia: String?,
    @SerializedName("denominacion") val denominacion: String?,
    @SerializedName("municipio") val municipio: String?,
    @SerializedName("departamento") val departamento: String?,
    @SerializedName("descripcionEmpleo") val descripcionEmpleo: String?


    //Nuevas cosas
): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        // parcel.readParcelable<Job>(Job::class.java.classLoader),
        parcel.readString()
    )

    class ListDeserializer : ResponseDeserializable<List<AudienceVacancy>> {
        override fun deserialize(reader: Reader): List<AudienceVacancy> {
            val type = object : TypeToken<List<AudienceVacancy>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(id_PublicaE)
        parcel.writeString(numeroOPEC)
        parcel.writeString(idVacante)
        parcel.writeString(numeroPlazas)
        // parcel.readParcelable<Job>(Job::class.java.classLoader)
        parcel.writeString(dependencia)
        parcel.writeString(denominacion)
        parcel.writeString(municipio)
        parcel.writeString(departamento)
        parcel.writeString(descripcionEmpleo)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AudienceVacancy> {
        override fun createFromParcel(parcel: Parcel): AudienceVacancy {
            return AudienceVacancy(parcel)
        }

        override fun newArray(size: Int): Array<AudienceVacancy?> {
            return arrayOfNulls(size)
        }
    }
}