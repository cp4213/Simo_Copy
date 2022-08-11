package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de una Entidad
 */
data class Entity(
        @SerializedName("id") val id: String?,
        @SerializedName("nit") val nit: String?,
        @SerializedName("nombre") val name: String?,
        @SerializedName("nombreContacto") val nameContact: String?
) : Parcelable {


    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun toString(): String {
        return name!!
    }

    class Deserializer : ResponseDeserializable<Entity> {
        override fun deserialize(reader: Reader) = Gson().fromJson(reader, Entity::class.java)
    }

    class ListDeserializer : ResponseDeserializable<List<Entity>> {
        override fun deserialize(reader: Reader): List<Entity> {
            val type = object : TypeToken<List<Entity>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nit)
        parcel.writeString(name)
        parcel.writeString(nameContact)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Entity> {
        override fun createFromParcel(parcel: Parcel): Entity {
            return Entity(parcel)
        }

        override fun newArray(size: Int): Array<Entity?> {
            return arrayOfNulls(size)
        }
    }


}