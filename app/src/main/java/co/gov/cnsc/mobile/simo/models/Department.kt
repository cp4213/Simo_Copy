package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de un Departamento
 */
data class Department(
        @SerializedName("id") val id: String,
        @SerializedName("nombre") private val name: String?,
        @SerializedName("descripcion") val description: String?,
        @SerializedName("valor") val value: String?
) : Parcelable {

    val realName: String
        get() {
            if (name != null) {
                return name
            } else {
                return description!!
            }
        }

    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun toString(): String {
        return realName
    }

    class ListDeserializer : ResponseDeserializable<List<Department>> {
        override fun deserialize(reader: Reader): List<Department> {
            val type = object : TypeToken<List<Department>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(value)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Department> {
        override fun createFromParcel(parcel: Parcel): Department {
            return Department(parcel)
        }

        override fun newArray(size: Int): Array<Department?> {
            return arrayOfNulls(size)
        }
    }

}