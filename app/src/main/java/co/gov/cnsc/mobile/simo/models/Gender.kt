package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable

/**
 * Datos de un genero/sexo
 */
data class Gender(
        val id: String?,
        val name: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString())


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return name!!
    }

    companion object CREATOR : Parcelable.Creator<Gender> {

        val genders: List<Gender>
            get() = listOf(
                    Gender(id = "M", name = "Masculino"),
                    Gender(id = "F", name = "Femenino")
            )

        override fun createFromParcel(parcel: Parcel): Gender {
            return Gender(parcel)
        }

        override fun newArray(size: Int): Array<Gender?> {
            return arrayOfNulls(size)
        }
    }

}