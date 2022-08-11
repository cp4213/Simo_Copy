package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Datos de un Estudio o Experiencia
 */
data class StudyExperience(
        @SerializedName("id") val id: String?,
        @SerializedName("estudio") val study: String?,
        @SerializedName("experiencia") val experience: String?
) : Parcelable {

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(study)
        writeString(experience)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<StudyExperience> = object : Parcelable.Creator<StudyExperience> {
            override fun createFromParcel(source: Parcel): StudyExperience = StudyExperience(source)
            override fun newArray(size: Int): Array<StudyExperience?> = arrayOfNulls(size)
        }
    }
}