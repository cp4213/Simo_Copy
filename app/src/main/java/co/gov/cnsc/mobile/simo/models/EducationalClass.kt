package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de un Nivel Educativo
 */
data class EducationalClass(
        @SerializedName("id") val id: String?,
        @SerializedName("activo") val active: Boolean,
        @SerializedName("nombre") val name: String?

) : Parcelable {

    override fun toString(): String {
        return name!!
    }

    constructor(source: Parcel) : this(
            source.readString(),
            1 == source.readInt(),
            source.readString()

    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeInt((if (active) 1 else 0))
        writeString(name)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<EducationalClass> = object : Parcelable.Creator<EducationalClass> {
            override fun createFromParcel(source: Parcel): EducationalClass = EducationalClass(source)
            override fun newArray(size: Int): Array<EducationalClass?> = arrayOfNulls(size)
        }
    }

    class ListDeserializer : ResponseDeserializable<List<EducationalClass>> {
        override fun deserialize(reader: Reader): List<EducationalClass> {
            val type = object : TypeToken<List<EducationalClass>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }
}