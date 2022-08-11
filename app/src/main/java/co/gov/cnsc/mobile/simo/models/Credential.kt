package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Datos de una credencial ya sea Formación, Experiencia, Producción Intelectual u Otro Documento
 */
data class Credential(
        @SerializedName("id") val id: String,
        @SerializedName("documento") val document: Document?,
        @SerializedName("tipo") val type: String?,
        @SerializedName("pais") val country: Country?,
        @SerializedName("fechaActualizacion") val dateUpdate: String?,
        @SerializedName("fechaIngreso") val dateEntry: String?,
        @SerializedName("fechaInicio") val dateStart: String?,
        @SerializedName("fechaTerminacion") val dateEnd: String?,
        @SerializedName("fechaOrdenamiento") val dateOrder: String?,
        @SerializedName("fechaCertificacion") val dateCertification: String?,
        @SerializedName("intensidadHoras") val hourIntensity: String?,

        @SerializedName("fechaGrado") val dateGrade: String?,
        @SerializedName("entidadEducativaExt") val entityEducationExt: String?,
        @SerializedName("graduado") val graduated: Boolean? = false,
        @SerializedName("tituloExtranjero") val foreignTitle: Boolean? = false,
        @SerializedName("otroPrograma") val otherProgramInstitution: Boolean? = false,
        @SerializedName("convalidado") val convalidate: Boolean? = false,
        @SerializedName("convalidadoNumeroResolucion") val convalidateResolutionNumber: String?,
        @SerializedName("convalidadoFechaResolucion") val convalidateResolutionDate: String?,
        @SerializedName("programa") val program: Program?,
        @SerializedName("periodicidad") val periodicity: IdName?,
        @SerializedName("programaExt") val programmExt: String?,
        @SerializedName("nivelEducacionFormal") val levelEducationLevel: EducationalLevel?,
        @SerializedName("nivelAlcanzado") val levelReached: String?,

        @SerializedName("identificador") val identifier: String?,
        @SerializedName("fichaBibliografica") val bibliographicRecord: String?,
        @SerializedName("tipoProduccionIntelectual") val typeProdIntelectual: IdDescription?,

        @SerializedName("actual") val actual: Boolean = false,
        @SerializedName("independiente") val independent: String?,
        @SerializedName("cargo") val charge: String?,
        @SerializedName("empresa") val company: String?,
        @SerializedName("jornadaCompleta") val fullTime: Boolean? = false,
        @SerializedName("horasDiarias") val dailyHours: String?,
        @SerializedName("funciones") val functions: String?,
        @SerializedName("tiempo") val time: String?,
        @SerializedName("tiempoHoras") val timeHours: Boolean?,
        @SerializedName("tipoContrato") val typeContract: String?,
        @SerializedName("tipoExperiencia") val typeExperience: TypeData?,
        @SerializedName("tiempoTotal") val timeTotal: String?,
        @SerializedName("tiempoTotalMeses") val timeTotalMonths: String?,

        @SerializedName("datos") val data: String?,
        @SerializedName("valor") val value: String?,
        @SerializedName("tipoInformacion") val typeInformation: TypeData?,
        @SerializedName("disciplinaAcademica") val academicDiciplne: AcademicDicipline?
) : Parcelable {
    val programmName: String?
        get() {
            if (program?.name != null) {
                return program.name
            } else {
                return programmExt
            }
        }

    val entityEducationalName: String?
        get() {
            if (program?.institution != null) {
                return program.institution.name
            } else {
                return entityEducationExt
            }
        }

    val nameDocument: String?
        get() {
            return "${document?.name}.pdf"
        }

    val urlDocument: String?
        get() {
            return document?.urlFile
        }

    val valueIsGraduated: String?
        get() {
            if (graduated == true) {
                return "Si"
            } else {
                return "No"
            }
        }

    class ListDeserializer : ResponseDeserializable<List<Credential>> {
        override fun deserialize(reader: Reader): List<Credential> {
            val type = object : TypeToken<List<Credential>>() {}.type
            return Gson().fromJson(reader, type)
        }
    }

    constructor(source: Parcel) : this(
            source.readString()!!,
            source.readParcelable<Document>(Document::class.java.classLoader),
            source.readString(),
            source.readParcelable<Country>(Country::class.java.classLoader),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readValue(Boolean::class.java.classLoader) as Boolean?,
            source.readValue(Boolean::class.java.classLoader) as Boolean?,
            source.readValue(Boolean::class.java.classLoader) as Boolean?,
            source.readValue(Boolean::class.java.classLoader) as Boolean?,
            source.readString(),
            source.readString(),
            source.readParcelable<Program>(Program::class.java.classLoader),
            source.readParcelable<IdName>(IdName::class.java.classLoader),
            source.readString(),
            source.readParcelable<EducationalLevel>(EducationalLevel::class.java.classLoader),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readParcelable<IdDescription>(IdDescription::class.java.classLoader),
            1 == source.readInt(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readValue(Boolean::class.java.classLoader) as Boolean?,
            source.readString(),
            source.readString(),
            source.readString(),
            1 == source.readInt(),
            source.readString(),
            source.readParcelable<TypeData>(TypeData::class.java.classLoader),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readParcelable<TypeData>(TypeData::class.java.classLoader),
            source.readParcelable<AcademicDicipline>(AcademicDicipline::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeParcelable(document, 0)
        writeString(type)
        writeParcelable(country, 0)
        writeString(dateUpdate)
        writeString(dateEntry)
        writeString(dateStart)
        writeString(dateEnd)
        writeString(dateOrder)
        writeString(dateCertification)
        writeString(hourIntensity)
        writeString(dateGrade)
        writeString(entityEducationExt)
        writeValue(graduated)
        writeValue(foreignTitle)
        writeValue(otherProgramInstitution)
        writeValue(convalidate)
        writeString(convalidateResolutionNumber)
        writeString(convalidateResolutionDate)
        writeParcelable(program, 0)
        writeParcelable(periodicity, 0)
        writeString(programmExt)
        writeParcelable(levelEducationLevel, 0)
        writeString(levelReached)
        writeString(identifier)
        writeString(bibliographicRecord)
        writeParcelable(typeProdIntelectual, 0)
        writeInt((if (actual) 1 else 0))
        writeString(independent)
        writeString(charge)
        writeString(company)
        writeValue(fullTime)
        writeString(dailyHours)
        writeString(functions)
        writeString(time)
        writeInt((if (timeHours == true) 1 else 0))
        writeString(typeContract)
        writeParcelable(typeExperience, 0)
        writeString(timeTotal)
        writeString(timeTotalMonths)
        writeString(data)
        writeString(value)
        writeParcelable(typeInformation, 0)
        writeParcelable(academicDiciplne, 0)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<Credential> = object : Parcelable.Creator<Credential> {
            override fun createFromParcel(source: Parcel): Credential = Credential(source)
            override fun newArray(size: Int): Array<Credential?> = arrayOfNulls(size)
        }
    }


    class Deserializer : ResponseDeserializable<Credential> {
        override fun deserialize(reader: Reader) = Gson().fromJson(reader, Credential::class.java)
    }
}