package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable

/**
 * Datos correspondientes a un filtro de búsqueda
 */
data class Filter(
        val keyWord: String?,
        val entity: Entity?,
        val department: Department? = null,
        val city: City? = null,
        val convocatory: Convocatory? = null,
        val salarialRange: SalarialRange? = null,
        val lowerLimitSR: String? = null,
        val upperLimitSR: String? = null,
        val level: IdName? = null,
        val numberOPEC: String? = null
) : Parcelable {


    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readParcelable(Entity::class.java.classLoader),
            parcel.readParcelable(Department::class.java.classLoader),
            parcel.readParcelable(City::class.java.classLoader),
            parcel.readParcelable(Convocatory::class.java.classLoader),
            parcel.readParcelable(SalarialRange::class.java.classLoader),
            parcel.readString(),
            parcel.readString(),
            parcel.readParcelable(IdName::class.java.classLoader),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(keyWord)
        parcel.writeParcelable(entity, flags)
        parcel.writeParcelable(department, flags)
        parcel.writeParcelable(city, flags)
        parcel.writeParcelable(convocatory, flags)
        parcel.writeParcelable(salarialRange, flags)
        parcel.writeString(lowerLimitSR)
        parcel.writeString(upperLimitSR)
        parcel.writeParcelable(level, flags)
        parcel.writeString(numberOPEC)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Filter> {
        override fun createFromParcel(parcel: Parcel): Filter {
            return Filter(parcel)
        }

        override fun newArray(size: Int): Array<Filter?> {
            return arrayOfNulls(size)
        }
    }


}