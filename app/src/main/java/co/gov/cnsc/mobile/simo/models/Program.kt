package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de un programa educativo
 */
data class Program(
        @SerializedName("id") val id: String?,
        @SerializedName("nombre") val name: String?,
        @SerializedName("institucion") val institution: Institution?,
        @SerializedName("nucleoBasico") val basicCore: IdName?
) : Parcelable {

    override fun toString(): String {
        return name ?: ""
    }

    class ListDeserializer : ResponseDeserializable<List<Program>> {
        override fun deserialize(reader: Reader): List<Program> {
            val type = object : TypeToken<List<Program>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readParcelable<Institution>(Institution::class.java.classLoader),
            source.readParcelable<IdName>(IdName::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(name)
        writeParcelable(institution, 0)
        writeParcelable(basicCore, 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Program> = object : Parcelable.Creator<Program> {
            override fun createFromParcel(source: Parcel): Program = Program(source)
            override fun newArray(size: Int): Array<Program?> = arrayOfNulls(size)
        }
    }
}