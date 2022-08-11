package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import co.gov.cnsc.mobile.simo.network.RestAPI
import com.google.gson.annotations.SerializedName

/**
 * Datos de un documento cédula/imagen/archivo
 */
class Document(
        @SerializedName("id") val id: String,
        @SerializedName("nombre") val name: String?,
        @SerializedName("rutaArchivo") val pathFile: String?,
        @SerializedName("contentType") val contentType: String?,
        @SerializedName("documentoOrigenId") val documentOriginId: String?
) : Parcelable {

    val urlFile: String?
        get() {
            return "${RestAPI.HOST}documents/get-document?docId=${id}&contentType=image/jpg"
        }

    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(pathFile)
        parcel.writeString(contentType)
        parcel.writeString(documentOriginId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Document> {
        override fun createFromParcel(parcel: Parcel): Document {
            return Document(parcel)
        }

        override fun newArray(size: Int): Array<Document?> {
            return arrayOfNulls(size)
        }
    }
}