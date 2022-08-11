package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de una Función
 */
data class Function(
        @SerializedName("id") val id: String?,
        @SerializedName("descripcion") val description: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString())

    class Deserializer : ResponseDeserializable<Function> {
        override fun deserialize(reader: Reader) = Gson().fromJson(reader, Function::class.java)
    }

    class ListDeserializer : ResponseDeserializable<List<Function>> {
        override fun deserialize(reader: Reader): List<Function> {
            val type = object : TypeToken<List<Function>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Function> {
        override fun createFromParcel(parcel: Parcel): Function {
            return Function(parcel)
        }

        override fun newArray(size: Int): Array<Function?> {
            return arrayOfNulls(size)
        }
    }
}