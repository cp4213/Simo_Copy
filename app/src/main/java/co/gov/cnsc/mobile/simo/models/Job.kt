package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.Reader

/**
 * Datos de un Empleo/Trabajo
 */
data class Job(
        @SerializedName("id") val id: String?,
        @SerializedName("asignacionSalarial") val salary: Int,
        @SerializedName("codigoEmpleo") val codeJob: String?,
        @SerializedName("denominacion") val denomination: IdAndName?,
        @SerializedName("gradoDenominacion") val denominationgrade: GradeDenomination?,
        @SerializedName("descripcion") val description: String?,
        @SerializedName("gradoNivel") val gradeLevel: GradeLevel?,
        @SerializedName("convocatoria") val convocatory: Convocatory?,
        @SerializedName("funciones") val functions: List<Function>?,
        @SerializedName("requisitosMinimos") val minimalRequirements: List<Requirement>?,
        @SerializedName("vacantes") val vacancies: List<Vacancy>?,
        @SerializedName("documento") val document: Document?,
        @SerializedName("vigenciaSalarial") val salarialVigency: String?,
        @SerializedName("area") val area: String?

) : Parcelable {
    val allFunctions: String?
        get() {
            val string = StringBuilder()
            functions?.forEach { function ->
                string.append("• " + function.description + "\n")
            }
            return string.toString()
        }

    val allRequirementsStudy: String?
        get() {
            val string = StringBuilder()
            minimalRequirements?.forEach { requirement ->
                string.append(requirement.study)
            }
            return string.toString()
        }

    val allRequirementsExperience: String?
        get() {
            val string = StringBuilder()
            minimalRequirements?.forEach { requirement ->
                string.append(requirement.experience)
            }
            return string.toString()
        }

    val allRequirementsStudyAlternatives: String?
        get() {
            val string = StringBuilder()
            minimalRequirements?.forEach { requirement ->
                string.append(requirement.allAlternativesStudy)
            }
            return string.toString()
        }

    val allRequirementsExperienceAlternatives: String?
        get() {
            val string = StringBuilder()
            minimalRequirements?.forEach { requirement ->
                string.append(requirement.allAlternativesExperience)
            }
            return string.toString()
        }

    val allRequirementsStudyEquivalences: String?
        get() {
            val string = StringBuilder()
            minimalRequirements?.forEach { requirement ->
                string.append(requirement.allEquivalencesStudy)
            }
            return string.toString()
        }

    val allRequirementsExperienceEquivalences: String?
        get() {
            val string = StringBuilder()
            minimalRequirements?.forEach { requirement ->
                string.append(requirement.allEquivalencesExperience)
            }
            return string.toString()
        }

    val allVacancies: String?
        get() {
            val string = StringBuilder()
            var total = 0

            vacancies?.forEach { vacancy ->
                string.append("▪ Dependencia: " + vacancy.dependency + "\n")
                string.append("▪ Municipio: " + vacancy.city + "\n")
                if (!vacancy?.toString().equals("null"))
                    total += vacancy?.quantity
                string.append("▪ Total Vacantes: " + vacancy.quantity + "\n")
                string.append(vacancy.allCharges_prepensionados + "\n\n")
            }
            return string.toString(); total
        }

    val totalVancancies: Int
        get() {
            var total = 0
            vacancies?.forEach { vacancy ->
                if (!vacancy?.toString().equals("null"))
                    total += vacancy?.quantity
            }
            return total
        }

    val firstVancancy: Vacancy?
        get() = vacancies?.get(0)

    //Total Cargos
    val totalCharges: Int
        get() {
            var total = 0
                //vacancies?.forEach { vacancy ->
               // if (!vacancy?.toString().equals("null"))
              //      total += vacancy?.charges.size
            //}
            return total
        }

    class Deserializer : ResponseDeserializable<Job> {
        override fun deserialize(reader: Reader) = Gson().fromJson(reader, Job::class.java)
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readInt(),
            source.readString(),
            source.readParcelable<IdAndName>(IdAndName::class.java.classLoader),
            source.readParcelable<GradeDenomination>(GradeDenomination::class.java.classLoader),
            source.readString(),
            source.readParcelable<GradeLevel>(GradeLevel::class.java.classLoader),
            source.readParcelable<Convocatory>(Convocatory::class.java.classLoader),
            source.createTypedArrayList(Function.CREATOR),
            source.createTypedArrayList(Requirement.CREATOR),
            source.createTypedArrayList(Vacancy.CREATOR),
            source.readParcelable<Document>(Document::class.java.classLoader),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeInt(salary)
        writeString(codeJob)
        writeParcelable(denomination, 0)
        writeParcelable(denominationgrade, 0)
        writeString(description)
        writeParcelable(gradeLevel, 0)
        writeParcelable(convocatory, 0)
        writeTypedList(functions)
        writeTypedList(minimalRequirements)
        writeTypedList(vacancies)
        writeParcelable(document, 0)
        writeString(salarialVigency)
        writeString(area)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Job> = object : Parcelable.Creator<Job> {
            override fun createFromParcel(source: Parcel): Job = Job(source)
            override fun newArray(size: Int): Array<Job?> = arrayOfNulls(size)
        }
    }
}