package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class AlertAccess(
@SerializedName("view") val view: String?,
@SerializedName("edit") val edit: String?,
@SerializedName("create") var create: String?,
@SerializedName("delete") val delete: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(view)
        parcel.writeString(edit)
        parcel.writeString(create)
        parcel.writeString(delete)
    }
    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AlertAccess> {
        override fun createFromParcel(parcel: Parcel): AlertAccess {
            return AlertAccess(parcel)
        }

        override fun newArray(size: Int): Array<AlertAccess?> {
            return arrayOfNulls(size)
        }
    }
}