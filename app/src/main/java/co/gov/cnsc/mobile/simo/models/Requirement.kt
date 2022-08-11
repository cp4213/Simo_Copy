package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Datos de un requisito como experiencia o estudio
 */
data class Requirement(
        @SerializedName("id") val id: String?,
        @SerializedName("estudio") val study: String?,
        @SerializedName("experiencia") val experience: String?,
        @SerializedName("alternativas") val alternatives: List<StudyExperience>?,
        @SerializedName("equivalencias") val equivalents: List<StudyExperience>?
) : Parcelable {

    val allAlternativesStudy: String?
        get() {
            val string = StringBuilder()
            alternatives?.forEach { alternative ->
                string.append(alternative.study)
            }
            return string.toString()
        }

    val allAlternativesExperience: String?
        get() {
            val string = StringBuilder()
            alternatives?.forEach { alternative ->
                string.append(alternative.experience)
            }
            return string.toString()
        }

    val allEquivalencesStudy: String?
        get() {
            val string = StringBuilder()
            equivalents?.forEach { equivalence ->
                string.append(equivalence.study)
            }
            return string.toString()
        }

    val allEquivalencesExperience: String?
        get() {
            val string = StringBuilder()
            equivalents?.forEach { equivalence ->
                string.append(equivalence.experience)
            }
            return string.toString()
        }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            ArrayList<StudyExperience>().apply { source.readList(this, StudyExperience::class.java.classLoader) },
            ArrayList<StudyExperience>().apply { source.readList(this, StudyExperience::class.java.classLoader) }
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(study)
        writeString(experience)
        writeList(alternatives)
        writeList(equivalents)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Requirement> = object : Parcelable.Creator<Requirement> {
            override fun createFromParcel(source: Parcel): Requirement = Requirement(source)
            override fun newArray(size: Int): Array<Requirement?> = arrayOfNulls(size)
        }
    }
}