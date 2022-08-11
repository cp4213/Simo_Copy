package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de cualquier objeto con estos atributos id, nombre
 */
data class IdName(
        @SerializedName("id") val id: String?,
        @SerializedName("nivel") val nivel: String?,
        @SerializedName("nombre") val name: String?
) : Parcelable {
    override fun toString(): String {
        return name ?: ""
    }

    class ListDeserializer : ResponseDeserializable<List<IdName>> {
        override fun deserialize(reader: Reader): List<IdName> {
            val type = object : TypeToken<List<IdName>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(nivel)
        writeString(name)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<IdName> = object : Parcelable.Creator<IdName> {
            override fun createFromParcel(source: Parcel): IdName = IdName(source)
            override fun newArray(size: Int): Array<IdName?> = arrayOfNulls(size)
        }
    }
}