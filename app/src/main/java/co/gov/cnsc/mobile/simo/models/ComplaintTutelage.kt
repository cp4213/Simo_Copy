package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de una queja o tutela
 */
data class ComplaintTutelage(
        @SerializedName("id") val id: String?,
        @SerializedName("asunto") val subject: String?,
        @SerializedName("detalle") val detail: String?,
        @SerializedName("radicado") val radicate: String?,
        @SerializedName("fecha") val date: String?,
        @SerializedName("estadoReclamacion") val status: IdStatus?,
        @SerializedName("claseReclamacion") val typecomplaint: IdName?,
        @SerializedName("anexos") val attachments: List<Document>?
) : Parcelable {


    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readParcelable(IdName::class.java.classLoader),
            parcel.readParcelable(IdStatus::class.java.classLoader),
            parcel.createTypedArrayList(Document))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(subject)
        parcel.writeString(detail)
        parcel.writeString(radicate)
        parcel.writeString(date)
        parcel.writeParcelable(status, flags)
        parcel.writeParcelable(typecomplaint, flags)
        parcel.writeTypedList(attachments)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ComplaintTutelage> {
        override fun createFromParcel(parcel: Parcel): ComplaintTutelage {
            return ComplaintTutelage(parcel)
        }

        override fun newArray(size: Int): Array<ComplaintTutelage?> {
            return arrayOfNulls(size)
        }
    }

    class ListDeserializer : ResponseDeserializable<List<ComplaintTutelage>> {
        override fun deserialize(reader: Reader): List<ComplaintTutelage> {
            val type = object : TypeToken<List<ComplaintTutelage>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }
    class Deserializer : ResponseDeserializable<ComplaintTutelage> {
        override fun deserialize(reader: Reader): ComplaintTutelage {
            val type = object : TypeToken<ComplaintTutelage>() {}.type
            return Gson().fromJson(reader, type)
        }
    }
}