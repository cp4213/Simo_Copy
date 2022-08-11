package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de los Rangos Salaríales
 */
data class SalarialRange(
        @SerializedName("id") val id: String?,
        @SerializedName("descripcion") val description: String?,
        @SerializedName("valor") val value: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun toString(): String {
        return description!!
    }

    class ListDeserializer : ResponseDeserializable<List<SalarialRange>> {
        override fun deserialize(reader: Reader): List<SalarialRange> {
            val type = object : TypeToken<List<SalarialRange>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(description)
        parcel.writeString(value)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SalarialRange> {
        override fun createFromParcel(parcel: Parcel): SalarialRange {
            return SalarialRange(parcel)
        }

        override fun newArray(size: Int): Array<SalarialRange?> {
            return arrayOfNulls(size)
        }
    }
}