package co.gov.cnsc.mobile.simo.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import co.gov.cnsc.mobile.simo.fragments.profile.*

/**
 * Esta clase contiene la funcionalidad para administrar las pestañas de mi hoja de vida
 * @param context Contexto de la aplicación
 * @param fm FragmentManager de la activity para mostrar varios fragments en forma de pestañas
 */
class MyProfilePagerAdapter(private val context: Context?, fm: FragmentManager?)
    : FragmentPagerAdapter(fm!!) {

    /**
     * Obtiene un nuevo frgamento dependiendo de la posición
     */
    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return MyBasicInfoFragment.newInstance() as Fragment
            }
            1 -> {
                return MyFormationFragment.newInstance() as Fragment
            }
            2 -> {
                return MyExperienceFragment.newInstance() as Fragment
            }
            3 -> {
                return MyIntelectualProdFragment.newInstance() as Fragment
            }
            else -> {
                return MyOtherDocumentsFragment.newInstance() as Fragment
            }
        }
    }

    /**
     * Obtiene el total de pestañas
     */
    override fun getCount(): Int {
        return 5
    }

}