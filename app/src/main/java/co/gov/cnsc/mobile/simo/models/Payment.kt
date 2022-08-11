package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import co.gov.cnsc.mobile.simo.extensions.toFormatCurrency
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de pago del usuario
 */
data class Payment(
        @SerializedName("id") val id: String?,
        @SerializedName("fechaPago") val datePay: String?,
        @SerializedName("fecha") val date: String?,
        @SerializedName("inscripcionConvocatoria") val workOffer: WorkOffer?,
        @SerializedName("medioPago") val type: IdName?,
        @SerializedName("valor") val value: Double = 0.0,
        @SerializedName("estado") val status: IdName?,
        @SerializedName("banco") val bank: String?,
        @SerializedName("codigoTrazabilidad") val trackCode: String?

) : Parcelable {

    val valueCurrency: String?
        get() {
            return value.toFormatCurrency()
        }

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readParcelable(WorkOffer::class.java.classLoader),
            parcel.readParcelable(IdName::class.java.classLoader),
            parcel.readDouble(),
            parcel.readParcelable(IdName::class.java.classLoader),
            parcel.readString(),
            parcel.readString())

    class ListDeserializer : ResponseDeserializable<List<Payment>> {
        override fun deserialize(reader: Reader): List<Payment> {
            val type = object : TypeToken<List<Payment>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(datePay)
        parcel.writeString(date)
        parcel.writeParcelable(workOffer, flags)
        parcel.writeParcelable(type, flags)
        parcel.writeValue(value)
        parcel.writeParcelable(status, flags)
        parcel.writeString(bank)
        parcel.writeString(trackCode)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Payment> {
        override fun createFromParcel(parcel: Parcel): Payment {
            return Payment(parcel)
        }

        override fun newArray(size: Int): Array<Payment?> {
            return arrayOfNulls(size)
        }
    }
}