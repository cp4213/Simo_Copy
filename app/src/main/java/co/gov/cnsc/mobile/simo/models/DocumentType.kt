package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Datos de un tipo de documento
 */
data class DocumentType(
        @SerializedName("id") val id: String?,
        @SerializedName("descripcion") val description: String?,
        @SerializedName("sigla") val nom: String? = null,
        @SerializedName("siglaPSE") val nomPSE: String? = null
) : Parcelable {

    override fun toString(): String {
        return description!!
    }


    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(description)
        parcel.writeString(nom)
        parcel.writeString(nomPSE)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DocumentType> {
        override fun createFromParcel(parcel: Parcel): DocumentType {
            return DocumentType(parcel)
        }

        override fun newArray(size: Int): Array<DocumentType?> {
            return arrayOfNulls(size)
        }

        val documentTypes: List<DocumentType>
            get() = listOf(
                    DocumentType(id = "1", description = "Cédula"),
                    DocumentType(id = "2", description = "Tarjeta de Identidad"))
    }
}
