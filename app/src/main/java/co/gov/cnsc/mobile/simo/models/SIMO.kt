package co.gov.cnsc.mobile.simo.models

import android.os.Parcel
import android.os.Parcelable

/**
 * Esta clase mantiene la información que se requiere durante todo momento en la aplicación
 */
class SIMO() : Parcelable {

    private object Holder {
        var INSTANCE = SIMO()
    }

    var session: User.Session? = null
    var categories: List<CategoryJob>? = null

    val isLogged: Boolean
        get() {
            return session != null
        }

    val categoryProfessional: CategoryJob?
        get() {
            return categories?.find { categoryJob -> categoryJob.id == "3" }
        }

    val categoryAsistencial: CategoryJob?
        get() {
            return categories?.find { categoryJob -> categoryJob.id == "5" }
        }

    val categoryTecnic: CategoryJob?
        get() {
            return categories?.find { categoryJob -> categoryJob.id == "4" }
        }

    val categoryTotal: CategoryJob?
        get() {
            return categories?.find { categoryJob -> categoryJob.id == "0" }
        }

    constructor(parcel: Parcel) : this() {
        session = parcel.readParcelable(User::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(session, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SIMO> {

        const val FORMAT_DATE = "yyyy-MM-dd"

        var instance: SIMO
            get() {
                return Holder.INSTANCE
            }
            set(value) {
                Holder.INSTANCE = value
            }


        override fun createFromParcel(parcel: Parcel): SIMO {
            return SIMO(parcel)
        }

        override fun newArray(size: Int): Array<SIMO?> {
            return arrayOfNulls(size)
        }
    }
}