package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de un Cargo
 */

data class Charge(
        @SerializedName("id") val idCharge: String?,
        @SerializedName("fechaLiberacion") val liberationDate: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString())

    class Deserializer : ResponseDeserializable<Charge>{
        override fun deserialize(reader: Reader): Charge = Gson().fromJson(reader, Charge::class.java)
    }

    class ListDeserializer : ResponseDeserializable<List<Charge>> {
        override fun deserialize(reader: Reader): List<Charge> {
            val type = object : TypeToken<List<Charge>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idCharge)
        parcel.writeString(liberationDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Charge> {
        override fun createFromParcel(parcel: Parcel): Charge {
            return Charge(parcel)
        }

        override fun newArray(size: Int): Array<Charge?> {
            return arrayOfNulls(size)
        }
    }


}