package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de una institución
 */
data class Institution(
        @SerializedName("id") val id: String?,
        @SerializedName("descripcioncaracteracademicoies") val description: String?,
        @SerializedName("nombre") val name: String?,
        @SerializedName("tipoies") val type: String?,
        @SerializedName("nities") val nit: String?,
        @SerializedName("descripciondepartamentoies") val department: String?,
        @SerializedName("descripcionmunicipioies") val city: String?
) : Parcelable {

    override fun toString(): String {
        return name!!
    }

    class ListDeserializer : ResponseDeserializable<List<Institution>> {
        override fun deserialize(reader: Reader): List<Institution> {
            val type = object : TypeToken<List<Institution>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(description)
        writeString(name)
        writeString(type)
        writeString(nit)
        writeString(department)
        writeString(city)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Institution> = object : Parcelable.Creator<Institution> {
            override fun createFromParcel(source: Parcel): Institution = Institution(source)
            override fun newArray(size: Int): Array<Institution?> = arrayOfNulls(size)
        }
    }
}