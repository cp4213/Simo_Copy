package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.Reader

/**
 * Datos de un usuario logeado en la aplicación
 */
data class User(
        @SerializedName("id") val id: String? = null,
        @SerializedName("login") val username: String?,
        @SerializedName("identificacion") val identifier: String?,
        @SerializedName("tipoDocumento") val documentType: DocumentType?,
        @SerializedName("nombre") var name: String? = null,
        @SerializedName("telefono") val telephone: String? = null,
        @SerializedName("email") val email: String?,
        @SerializedName("docFoto") val docPhoto: Document? = null,
        @SerializedName("apellido") var lastName: String? = null,
        @SerializedName("direccion") val address: String? = null,
        @SerializedName("nacidoExtranjero") val birthAbroad: Boolean? = null,
        @SerializedName("municipioNacimiento") val cityBirth: City? = null,
        @SerializedName("paisNacimiento") val countryBirth: Country? = null,
        @SerializedName("zipcodenacimiento") val zipCodeBirth: String? = null,
        @SerializedName("fechaExpedicion") val dateExpedition: String? = null,
        @SerializedName("fechaNacimiento") val dateBirth: String? = null,
        @SerializedName("residenteExtranjero") val residentAbroad: Boolean? = null,
        @SerializedName("municipioResidencia") val cityResident: City? = null,
        @SerializedName("paisResidencia") val countryResident: Country? = null,
        @SerializedName("zipcoderesidencia") val zipCodeResident: String? = null,
        @SerializedName("genero") val gender: String? = null,
        @SerializedName("envioCorreo") val sendEmail: Boolean = false,
        @SerializedName("docIdentificacion") val documentDni: Document? = null,
        @SerializedName("rolesUsuario") val rolesString: String? = null,
        @SerializedName("fechaCreacion") val dateCreation: String? = null,
        var verificationCode: String? = null,
        @SerializedName("nivelEducacionFormal") val educationalLevel: EducationalLevel? = null,
        @SerializedName("discapacidades") var disabilities: List<Disability>? = null
) : Parcelable {

    val hasDisabilities: Boolean
        get() {
            return disabilities == null || disabilities?.isNotEmpty() == true
        }

    val disabilitiesString: String
        get() {
            val disString = StringBuilder()
            disabilities?.forEach {
                if (!disString.isEmpty()) {
                    disString.append(", ")
                }
                disString.append(it.toString())
            }
            return disString.toString()
        }


    val urlPhoto: String?
        get() {
            return docPhoto?.urlFile
        }

    val urlDni: String?
        get() {
            return documentDni?.urlFile
        }

    val nameDni: String?
        get() {
            return "${documentDni?.name}.pdf"
        }


    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readParcelable(DocumentType::class.java.classLoader),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readParcelable(Document::class.java.classLoader),
            parcel.readString(),
            parcel.readString(),
            parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
            parcel.readParcelable(City::class.java.classLoader),
            parcel.readParcelable(Country::class.java.classLoader),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
            parcel.readParcelable(City::class.java.classLoader),
            parcel.readParcelable(Country::class.java.classLoader),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readParcelable(Document::class.java.classLoader),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readParcelable(EducationalLevel::class.java.classLoader))

    override fun describeContents(): Int {
        return 0
    }

    data class Session(
            @SerializedName("id") val idUser: String?,
            @SerializedName("username") val username: String?,
            @SerializedName("nombre") var name: String?,
            @SerializedName("cookie") val cookie: String?,
            @SerializedName("imageUrl") var imageUrl: String?

    ) : Parcelable {

        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString())

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Session> {
            override fun createFromParcel(parcel: Parcel): Session {
                return Session(parcel)
            }

            override fun newArray(size: Int): Array<Session?> {
                return arrayOfNulls(size)
            }
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(idUser)
            parcel.writeString(username)
            parcel.writeString(name)
            parcel.writeString(cookie)
            parcel.writeString(imageUrl)
        }

    }

    class Deserializer : ResponseDeserializable<User> {
        override fun deserialize(reader: Reader) = Gson().fromJson(reader, User::class.java)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(username)
        parcel.writeString(identifier)
        parcel.writeParcelable(documentType, flags)
        parcel.writeString(name)
        parcel.writeString(telephone)
        parcel.writeString(email)
        parcel.writeParcelable(docPhoto, flags)
        parcel.writeString(lastName)
        parcel.writeString(address)
        parcel.writeValue(birthAbroad)
        parcel.writeParcelable(cityBirth, flags)
        parcel.writeParcelable(countryBirth, flags)
        parcel.writeString(zipCodeBirth)
        parcel.writeString(dateExpedition)
        parcel.writeString(dateBirth)
        parcel.writeValue(residentAbroad)
        parcel.writeParcelable(cityResident, flags)
        parcel.writeParcelable(countryResident, flags)
        parcel.writeString(zipCodeResident)
        parcel.writeString(gender)
        parcel.writeByte(if (sendEmail) 1 else 0)
        parcel.writeParcelable(documentDni, flags)
        parcel.writeString(rolesString)
        parcel.writeString(dateCreation)
        parcel.writeString(verificationCode)
        parcel.writeParcelable(educationalLevel, flags)
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }


}