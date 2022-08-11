package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de una convocatoria
 */
data class Convocatory(
        @SerializedName("id") val id: String,
        @SerializedName("nombre") val name: String,
        @SerializedName("agno") val year: Int?,
        @SerializedName("codigo") val code: String?,
        @SerializedName("descripcion") val description: String?,
        @SerializedName("convocatoriaPadre") val convocatoryParent: IdName?,
        @SerializedName("entidad") val entity: Entity?,
        @SerializedName("cartaIntencion") val intentionLetter: Document?,
        @SerializedName("tipoAplicacion") val publicationType: String?,
        @SerializedName("publicarOpec") val toPublishOpec: Boolean?,
        @SerializedName("deltaPagoPSE") val deltaPSEPay: Int?,
        @SerializedName("deltaPagoBanco") val deltaBankPay: Int?,
        @SerializedName("tipoProceso") val procesType: String? //Indica si el proceso es abierto o de asenso
) : Parcelable {

    val fullNameConvocatory: String?
        get() {
            return "$name"
            //return "CONVOCATORIA $code de $year $name"
        }


    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readString(),
            parcel.readString(),
            parcel.readParcelable(IdName::class.java.classLoader),
            parcel.readParcelable(Entity::class.java.classLoader),
            parcel.readParcelable(Document::class.java.classLoader),
            parcel.readString(),
            parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readString())

    override fun toString(): String {
        return name
    }

    class Deserializer : ResponseDeserializable<Convocatory> {
        override fun deserialize(reader: Reader) = Gson().fromJson(reader, Convocatory::class.java)
    }

    class ListDeserializer : ResponseDeserializable<List<Convocatory>> {
        override fun deserialize(reader: Reader): List<Convocatory> {
            val type = object : TypeToken<List<Convocatory>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }


    class Stage(
            @SerializedName("id") val id: String,
            @SerializedName("descripcion") val description: String,
            @SerializedName("fechaFin") val dateEnd: String,
            @SerializedName("fechaInicio") val dateStart: String
    ) {


        class Deserializer : ResponseDeserializable<Stage> {
            override fun deserialize(reader: Reader) = Gson().fromJson(reader, Stage::class.java)
        }

        class ListDeserializer : ResponseDeserializable<List<Stage>> {
            override fun deserialize(reader: Reader): List<Stage> {
                val type = object : TypeToken<List<Stage>>() {}.type
                return Gson().fromJson(reader, type)
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeValue(year)
        parcel.writeString(code)
        parcel.writeString(description)
        parcel.writeParcelable(convocatoryParent, flags)
        parcel.writeParcelable(entity, flags)
        parcel.writeParcelable(intentionLetter, flags)
        parcel.writeString(publicationType)
        parcel.writeValue(toPublishOpec)
        parcel.writeValue(deltaPSEPay)
        parcel.writeValue(deltaBankPay)
        parcel.writeString(procesType)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Convocatory> {
        override fun createFromParcel(parcel: Parcel): Convocatory {
            return Convocatory(parcel)
        }

        override fun newArray(size: Int): Array<Convocatory?> {
            return arrayOfNulls(size)
        }
    }

}