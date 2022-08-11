package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos Total Inscritos a un Empleo
 */

data class TotalCitizensReg(
        @SerializedName("total") var totalCitizensEnrolled: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString()) {
    }

    class ListDeserializer : ResponseDeserializable<List<TotalCitizensReg>> {
        override fun deserialize(reader: Reader): List<TotalCitizensReg> {
            val type = object : TypeToken<List<TotalCitizensReg>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.readString()

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TotalCitizensReg> {
        override fun createFromParcel(parcel: Parcel): TotalCitizensReg {
            return TotalCitizensReg(parcel)
        }

        override fun newArray(size: Int): Array<TotalCitizensReg?> {
            return arrayOfNulls(size)
        }
    }
}