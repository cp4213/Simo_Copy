package co.gov.cnsc.mobile.simo.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOFragment
import co.gov.cnsc.mobile.simo.adapters.MyJobsPagerAdapter
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_my_employs.*


/**
 * Esta clase contiene la funcionalidad de la pantalla de 'Mis empleos' en el menú principal
 * de la aplicación
 */
class MyEmploysFragment : SIMOFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_employs, container, false)

    }

    /**
     * Al crear el layout, muestra 3 pestañas correspondientes a los 3 tipos de empleos
     * Inscritos, Preinscritos y Favoritos
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val jobsAdapter = MyJobsPagerAdapter(context, fragmentManager)
        viewPagerJobs.adapter = jobsAdapter as PagerAdapter
        tabLayoutJobs.setupWithViewPager(viewPagerJobs)
    }

    /**
     * Al destruir todos los componentes gráficos de la vista
     */
    override fun onDestroyView() {
        super.onDestroyView()
        clearFindViewByIdCache()
    }


    companion object {

        /**
         * Usa este metodo para crear una nueva instancia de la vista
         * en este caso es un fragment
         */
        @JvmStatic
        fun newInstance() =
                MyEmploysFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }
}
