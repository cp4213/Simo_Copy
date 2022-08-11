package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Datos de un nivel o grado
 */
data class GradeLevel(
        @SerializedName("grado") val grade: String?,
        @SerializedName("nivelNombre") val levelName: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(grade)
        parcel.writeString(levelName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GradeLevel> {
        override fun createFromParcel(parcel: Parcel): GradeLevel {
            return GradeLevel(parcel)
        }

        override fun newArray(size: Int): Array<GradeLevel?> {
            return arrayOfNulls(size)
        }
    }
}