package co.gov.cnsc.mobile.simo.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOFragment
import co.gov.cnsc.mobile.simo.adapters.MyJobsPagerAdapter
import co.gov.cnsc.mobile.simo.databinding.FragmentMyEmploysBinding
import kotlinx.android.synthetic.*


/**
 * Esta clase contiene la funcionalidad de la pantalla de 'Mis empleos' en el menú principal
 * de la aplicación
 */
class MyEmploysFragment : SIMOFragment() {

    private var _binding: FragmentMyEmploysBinding? =null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding=FragmentMyEmploysBinding.inflate(inflater, container, false)
        return binding.root

    }

    /**
     * Al crear el layout, muestra 3 pestañas correspondientes a los 3 tipos de empleos
     * Inscritos, Preinscritos y Favoritos
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val jobsAdapter = MyJobsPagerAdapter(context, fragmentManager)
        binding.viewPagerJobs.adapter = jobsAdapter as PagerAdapter
        binding.tabLayoutJobs.setupWithViewPager(binding.viewPagerJobs)
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
