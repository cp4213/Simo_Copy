package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de cualquier objeto con estos atributos id, descripción
 */
data class IdDescription(
        @SerializedName("id") val id: String?,
        @SerializedName("descripcion") val description: String?
) : Parcelable {

    override fun toString(): String {
        return description!!
    }

    class ListDeserializer : ResponseDeserializable<List<IdDescription>> {
        override fun deserialize(reader: Reader): List<IdDescription> {
            val type = object : TypeToken<List<IdDescription>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(description)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<IdDescription> = object : Parcelable.Creator<IdDescription> {
            override fun createFromParcel(source: Parcel): IdDescription = IdDescription(source)
            override fun newArray(size: Int): Array<IdDescription?> = arrayOfNulls(size)
        }
    }
}