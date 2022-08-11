package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de un Tipo de Dato
 */
data class TypeData(
        @SerializedName("id") val id: String?,
        @SerializedName("activo") val active: Boolean?,
        @SerializedName("descripcion") val description: String?,
        @SerializedName("nombre") val name: String?
) : Parcelable {

    override fun toString(): String {
        return name ?: ""
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readValue(Boolean::class.java.classLoader) as Boolean?,
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeValue(active)
        writeString(description)
        writeString(name)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<TypeData> = object : Parcelable.Creator<TypeData> {
            override fun createFromParcel(source: Parcel): TypeData = TypeData(source)
            override fun newArray(size: Int): Array<TypeData?> = arrayOfNulls(size)
        }
    }

    class ListDeserializer : ResponseDeserializable<List<TypeData>> {
        override fun deserialize(reader: Reader): List<TypeData> {
            val type = object : TypeToken<List<TypeData>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }
}