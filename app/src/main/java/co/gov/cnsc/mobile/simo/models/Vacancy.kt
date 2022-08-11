package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.google.android.gms.common.internal.safeparcel.SafeParcelWriter.writeTypedList
import com.google.gson.annotations.SerializedName
import java.lang.NumberFormatException

/**
 * Datos de una vacante
 */
data class Vacancy(
        @SerializedName("id") val id: String?,
        @SerializedName("cantidad") val quantity: Int,
        @SerializedName("municipio") val city: IdName?,
        @SerializedName("dependencia") val dependency: IdName?,
        @SerializedName("ocupadasPrePensionados") val ocupadasPrePensionados: Int//,
        //@SerializedName("fechaGenerada") val fechaGenerada: String

        //@SerializedName("cargosVacantes") val charges: List<Charge>
) : Parcelable {

    val vacancyRowString: String?
        get() {
            return "${quantity} ${city?.name} ${dependency?.name}"
        }


    val allCharges_prepensionados: String?
        get() {
             //val i = 1
            val string = StringBuilder()
            if (ocupadasPrePensionados!=0){
                string.append("▪ Cargos ocupados por prepensionados:  $ocupadasPrePensionados \n" )//Mejorar
                //string.append("▪ Fecha de liberación para ésta vacante de Prepensionado: $fechaGenerada \n" )
            }
             /*val string = StringBuilder()
             charges?.forEach { charge ->
                 string.append("▪ Fecha de liberación para ésta vacante de Prepensionado: \n")
                 string.append(charge.liberationDate + "\n\n")
             }
             return string.toString()*/
            return string.toString()
        }

    val totalCharges: Int
        get() {
           // return charges.size
            return 0
        }

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readInt(),
            parcel.readParcelable(IdName::class.java.classLoader),
            parcel.readParcelable(IdName::class.java.classLoader),
            parcel.readInt()//,
            //parcel.readString()
        //parcel.createTypedArrayList(Charge.CREATOR)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeInt(quantity)
        parcel.writeParcelable(city, flags)
        parcel.writeParcelable(dependency, flags)
        parcel.writeInt(ocupadasPrePensionados)
      //  parcel.writeString(fechaGenerada)
        //parcel.writeTypedList(charges)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Vacancy> {
        override fun createFromParcel(parcel: Parcel): Vacancy {
            return Vacancy(parcel) //Original
        }

        override fun newArray(size: Int): Array<Vacancy?> {
            return arrayOfNulls(size)
        }
    }
}