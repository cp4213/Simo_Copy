package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de cualquier objeto con estos atributos id, estado
 */
data class IdStatus(
        @SerializedName("id") val id: String?,
        @SerializedName("estado") val status: String?
) : Parcelable {
    override fun toString(): String {
        return status ?: ""
    }

    class ListDeserializer : ResponseDeserializable<List<IdStatus>> {
        override fun deserialize(reader: Reader): List<IdStatus> {
            val type = object : TypeToken<List<IdStatus>>() {}.type
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
        writeString(status)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<IdStatus> = object : Parcelable.Creator<IdStatus> {
            override fun createFromParcel(source: Parcel): IdStatus = IdStatus(source)
            override fun newArray(size: Int): Array<IdStatus?> = arrayOfNulls(size)
        }
    }
}