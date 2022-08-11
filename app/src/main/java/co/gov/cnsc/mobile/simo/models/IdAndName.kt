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
data class IdAndName(
        @SerializedName("id") val id: String?,
        @SerializedName("nivel") val level: IdName?,
        @SerializedName("nombre") val name: String?
) : Parcelable {
    override fun toString(): String {
        return name ?: ""
    }

    class ListDeserializer : ResponseDeserializable<List<IdAndName>> {
        override fun deserialize(reader: Reader): List<IdAndName> {
            val type = object : TypeToken<List<IdAndName>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readParcelable<IdName>(IdName::class.java.classLoader),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeParcelable(level, 0)
        writeString(name)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<IdAndName> = object : Parcelable.Creator<IdAndName> {
            override fun createFromParcel(source: Parcel): IdAndName = IdAndName(source)
            override fun newArray(size: Int): Array<IdAndName?> = arrayOfNulls(size)
        }
    }
}