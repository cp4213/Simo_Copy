package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de una alerta
 */
data class Alert(
        @SerializedName("id") val id: String?,
        @SerializedName("notificacion") val notification: Notification?,
        @SerializedName("estadoNotificacion") var status: String?,
        @SerializedName("fechaNotificacion") val date: String?,
        @SerializedName("access") val access: AlertAccess?
) : Parcelable {

    var isRead: Boolean
        get() {
            return status == "leida"
        }
        set(value) {
            if (value) {
                status = "leida"
            } else {
                status = "Agendado"
            }
        }

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readParcelable(Notification::class.java.classLoader),
            parcel.readString(),
            parcel.readString(),
            parcel.readParcelable(AlertAccess::class.java.classLoader))


    class ListDeserializer : ResponseDeserializable<List<Alert>> {
        override fun deserialize(reader: Reader): List<Alert> {
            val type = object : TypeToken<List<Alert>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeParcelable(notification, flags)
        parcel.writeString(status)
        parcel.writeString(date)
        parcel.writeParcelable(access, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Alert> {
        override fun createFromParcel(parcel: Parcel): Alert {
            return Alert(parcel)
        }

        override fun newArray(size: Int): Array<Alert?> {
            return arrayOfNulls(size)
        }
    }
}