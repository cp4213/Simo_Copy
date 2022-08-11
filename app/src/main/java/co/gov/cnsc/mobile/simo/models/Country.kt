package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de un pais
 */
data class Country(
        @SerializedName("id") val id: String,
        @SerializedName("codigo") val code: String?,
        @SerializedName("nombre") val name: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString(),
            parcel.readString())

    override fun toString(): String {
        return name!!
    }

    class ListDeserializer : ResponseDeserializable<List<Country>> {
        override fun deserialize(reader: Reader): List<Country> {
            val type = object : TypeToken<List<Country>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(code)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Country> {
        override fun createFromParcel(parcel: Parcel): Country {
            return Country(parcel)
        }

        override fun newArray(size: Int): Array<Country?> {
            return arrayOfNulls(size)
        }
    }
}