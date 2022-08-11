package co.gov.cnsc.mobile.simo.models

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de un municipio
 */
open class City(
        @SerializedName("id") open val id: String?,
        @SerializedName("codDane") open val codDane: String?,
        @SerializedName("nombre") open val name: String?,
        @SerializedName("departamento") open val department: Department?
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readParcelable(Department::class.java.classLoader))

    //muestra el nombre de la ciudad en "Búsqueda Avanzada" y en "Datos Básicos"
    override fun toString(): String {
        //return "${department?.realName} - ${name}"
        return "${name}"
        //return "${name} - ${department?.realName}"
    }

    val nameDepartment: String
        get() = "${name} - ${department?.realName}"

    class ListDeserializer : ResponseDeserializable<List<City>> {
        override fun deserialize(reader: Reader): List<City> {
            val type = object : TypeToken<List<City>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(codDane)
        parcel.writeString(name)
        parcel.writeParcelable(department, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    @SuppressLint("ParcelCreator")
    companion object CREATOR : Parcelable.Creator<City> {
        override fun createFromParcel(parcel: Parcel): City {
            return City(parcel)
        }

        override fun newArray(size: Int): Array<City?> {
            return arrayOfNulls(size)
        }
    }

    //muestra el nombre de la ciudad en "Home", 
    class Complete(id: String, codDane: String?, name: String?, department: Department?) : City(id, codDane, name, department), Parcelable {

        override fun toString(): String {
            return this.nameDepartment
        }
    }

}