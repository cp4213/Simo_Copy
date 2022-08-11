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
data class EducationalLevel(
        @SerializedName("id") val id: String?,
        @SerializedName("activo") val active: Boolean,
        @SerializedName("nombre") val name: String?,
        @SerializedName("tipoEducacionFormal") val type: IdName?,
        @SerializedName("claseEducacion") val classE: EducationalClass?
      //  @SerializedName("orden") val orden: String,


) : Parcelable {

    override fun toString(): String {
        return name!!
    }

    constructor(source: Parcel) : this(
            source.readString(),
            1 == source.readInt(),
            source.readString(),
            source.readParcelable<IdName>(IdName::class.java.classLoader),
          //  source.readString(),
            source.readParcelable<EducationalClass>(EducationalClass::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeInt((if (active) 1 else 0))
        writeString(name)
        writeParcelable(type, 0)
       // writeString(orden)
        writeParcelable(classE, 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<EducationalLevel> = object : Parcelable.Creator<EducationalLevel> {
            override fun createFromParcel(source: Parcel): EducationalLevel = EducationalLevel(source)
            override fun newArray(size: Int): Array<EducationalLevel?> = arrayOfNulls(size)
        }
    }

    class ListDeserializer : ResponseDeserializable<List<EducationalLevel>> {
        override fun deserialize(reader: Reader): List<EducationalLevel> {
            val type = object : TypeToken<List<EducationalLevel>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }
}