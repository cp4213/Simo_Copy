package co.gov.cnsc.mobile.simo.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.fragments.my_jobs.MyJobsFragment

/**
 * Esta clase contiene la funcionalidad para administrar las pestañas de trabajos inscritos, confirmados o favoritos es un viewpager
 * @param context Contexto de la aplicación
 * @param fm FragmentManager de la activity para mostrar varios fragments en forma de pestañas
 */
class MyJobsPagerAdapter(private val context: Context?, fm: FragmentManager?) :
    FragmentStatePagerAdapter(fm!!) {

    /**
     * Obtiene un nuevo frgamento dependiendo de la posición
     */
    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return MyJobsFragment.newInstance("F")
            }
            1 -> {
                return MyJobsFragment.newInstance("PI")
            }
            2 -> {
                return MyJobsFragment.newInstance("I")
            }
            else -> {
                return MyJobsFragment.newInstance("F")
            }
        }
    }

    /**
     * Obtiene el nombre/titulo del listado de empleos
     */
    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            1 -> {
                return context?.getString(R.string.preincritos_title)
            }
            2 -> {
                return context?.getString(R.string.inscritos_title)
            }
            0 -> {
                return context?.getString(R.string.favorites_title)
            }
            else -> {
                return context?.getString(R.string.audiences)
            }
        }
    }

    /**
     * Obtiene el total de pestañas
     */
    override fun getCount(): Int {
        return 3
    }

}