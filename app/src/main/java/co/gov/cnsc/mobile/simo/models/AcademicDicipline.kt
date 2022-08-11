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
data class AcademicDicipline(
        @SerializedName("id") val id: String?,
        @SerializedName("nombre") val name: String?,
        @SerializedName("institucion") val institution: Institution?,
        @SerializedName("nucleoBasico") val basicCore: IdName?,
        @SerializedName("nombreSnies") val sniesName: String?,
        @SerializedName("nivelEducacionFormal") val levelEducationLevel: EducationalLevel?


) : Parcelable {

    override fun toString(): String {
        return name ?: ""
    }

    class ListDeserializer : ResponseDeserializable<List<AcademicDicipline>> {
        override fun deserialize(reader: Reader): List<AcademicDicipline> {
            val type = object : TypeToken<List<AcademicDicipline>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readParcelable<Institution>(Institution::class.java.classLoader),
            source.readParcelable<IdName>(IdName::class.java.classLoader),
            source.readString(),
            source.readParcelable<EducationalLevel>(EducationalLevel::class.java.classLoader)

    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(name)
        writeParcelable(institution, 0)
        writeParcelable(basicCore, 0)
        writeString(sniesName)
        writeParcelable(levelEducationLevel, 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<AcademicDicipline> = object : Parcelable.Creator<AcademicDicipline> {
            override fun createFromParcel(source: Parcel): AcademicDicipline = AcademicDicipline(source)
            override fun newArray(size: Int): Array<AcademicDicipline?> = arrayOfNulls(size)
        }
    }
}