package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

data class Audience (
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("convocatory") val selectionProces: String?,
    @SerializedName("dateIni") val dateIni: String?,
    @SerializedName("dateEnd") val dateEnd: String?,
    @SerializedName("empleo") val job: Job? = null

    //Nuevas cosas
        ): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable<Job>(Job::class.java.classLoader))

    class ListDeserializer : ResponseDeserializable<List<Audience>> {
        override fun deserialize(reader: Reader): List<Audience> {
            val type = object : TypeToken<List<Audience>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(selectionProces)
        parcel.writeString(dateIni)
        parcel.writeString(dateEnd)
        parcel.readParcelable<Job>(Job::class.java.classLoader)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Audience> {
        override fun createFromParcel(parcel: Parcel): Audience {
            return Audience(parcel)
        }

        override fun newArray(size: Int): Array<Audience?> {
            return arrayOfNulls(size)
        }
    }
}