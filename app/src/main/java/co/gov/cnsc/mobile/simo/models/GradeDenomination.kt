package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Datos de un nivel o grado
 */
data class GradeDenomination(
    @SerializedName("id") val id: String?,
    @SerializedName("grado") val grade: String?,
    @SerializedName("denominacion") val denomination: IdAndName?

) : Parcelable {

    constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readParcelable<IdAndName>(IdAndName::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(grade)
        parcel.writeParcelable(denomination, 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GradeDenomination> {
        override fun createFromParcel(parcel: Parcel): GradeDenomination {
            return GradeDenomination(parcel)
        }

        override fun newArray(size: Int): Array<GradeDenomination?> {
            return arrayOfNulls(size)
        }
    }
}