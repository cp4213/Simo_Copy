package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Datos de una Notificación de una Alerta
 */
data class Notification(
        @SerializedName("id") val id: String?,
        @SerializedName("descripcion") val description: String?,
        @SerializedName("fechaMostrarCalendario") val dateShowCalendar: String?,
        @SerializedName("fechaEjecucion") val dateExecution: String?,
        @SerializedName("fechaAgenda") val dateSchedule: String?,
        @SerializedName("asunto") val subject: String?,
        @SerializedName("notificacionText") val body: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(description)
        parcel.writeString(dateShowCalendar)
        parcel.writeString(dateExecution)
        parcel.writeString(dateSchedule)
        parcel.writeString(subject)
        parcel.writeString(body)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Notification> {
        override fun createFromParcel(parcel: Parcel): Notification {
            return Notification(parcel)
        }

        override fun newArray(size: Int): Array<Notification?> {
            return arrayOfNulls(size)
        }
    }
}