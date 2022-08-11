package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Datos de una discapacidad
 */
data class Disability(
        @SerializedName("id") val id: String,
        @SerializedName("descripcion") val name: String) : Parcelable {

    constructor(source: Parcel) : this(
            source.readString()!!,
            source.readString()!!
    )

    override fun toString(): String {
        return name
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(name)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Disability> = object : Parcelable.Creator<Disability> {
            override fun createFromParcel(source: Parcel): Disability = Disability(source)
            override fun newArray(size: Int): Array<Disability?> = arrayOfNulls(size)
        }

        val disabilities: List<Disability>
            get() {
                val disabs = mutableListOf<Disability>()
                disabs.add(Disability("1", "Psicosocial"))
                disabs.add(Disability("3", "Intelectual"))
                disabs.add(Disability("4", "Auditiva"))
                disabs.add(Disability("5", "Visual"))
                disabs.add(Disability("6", "Física"))
                disabs.add(Disability("7", "Multiples"))
                disabs.add(Disability("8", "Sordoceguera"))
                return disabs
            }

        val disabilitiesArray: Array<String?>
            get() {
                val disabs = arrayOfNulls<String>(disabilities.size)
                disabilities.forEachIndexed { index, disability ->
                    disabs[index] = disability.toString()
                }
                return disabs
            }

        fun getCheckedDisabilities(allDisabilities: List<Disability>, currenDisabilities: List<Disability>?): BooleanArray {
            val checkedDebilities = BooleanArray(allDisabilities.size)
            allDisabilities.forEachIndexed { index, disability ->
                val conDisability = currenDisabilities?.find {
                    it.id == disability.id
                }
                checkedDebilities[index] = conDisability != null
            }
            return checkedDebilities
        }
    }
}