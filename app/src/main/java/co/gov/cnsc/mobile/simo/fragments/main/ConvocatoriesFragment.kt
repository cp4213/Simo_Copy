package co.gov.cnsc.mobile.simo.fragments.main

import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.SIMOFragment
import co.gov.cnsc.mobile.simo.adapters.ConvocatoriesAdapter
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.databinding.FragmentConvocatoriesBinding
import co.gov.cnsc.mobile.simo.models.Convocatory
import co.gov.cnsc.mobile.simo.network.RestAPI
import com.github.kittinunf.fuel.core.Request
import kotlinx.android.synthetic.main.fragment_convocatories.*


/**
 * Esta clase contiene la funcionalidad de la pantalla de 'Convocatorias' en el menú principal
 * de la aplicación
 */
class ConvocatoriesFragment : SIMOFragment(), SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentConvocatoriesBinding? =null
    private val binding get() = _binding!!

    /**
     * Request que se realiza al servidor para traer el listado de convocatorias
     */
    private var request: Request? = null

    /**
     * Adapter que contiene el listado de convocatorias a mostrar en la aplicación
     */
    private var adapter: ConvocatoriesAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding =FragmentConvocatoriesBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureUI()
        getConvocatories()
    }


    /**
     * Configura el layout UI para agregar el listview de convocatorias
     * y la acción de swiperefresh
     */
    fun configureUI() {
        adapter = ConvocatoriesAdapter(activity, ArrayList<Convocatory>())
        adapter?.onSeeJobsListener = { item: Convocatory, position: Int ->
            if (context != null)
                SIMOApplication.goToWorkOffers(context = requireContext(), convocatory = item)
        }
        swipeRefresh?.setOnRefreshListener(this)
        binding.listViewConvocatories.emptyView = binding.empty
        binding.empty?.hide()
        listViewConvocatories.setOnItemClickListener { parent, view, position, id ->
            val item = adapter?.getItem(position)
            goToDetailConvocatory(item)
        }
    }

    /**
     * Al darle swiperefresh al listado se actualiza el listado
     */
    override fun onRefresh() {
        getConvocatories()
    }

    /**
     * Obtiene el listado de convocatorias desde el servidor
     * através de un web service
     */
    private fun getConvocatories() {
        swipeRefresh?.isRefreshing = true
        request = RestAPI.getConvocatoriesLogged({ convocatories ->
            swipeRefresh?.isRefreshing = false
            binding.empty?.showEmptyState()
            adapter?.dataSource = convocatories as ArrayList<Convocatory>
            listViewConvocatories?.adapter = adapter
            adapter?.notifyDataSetChanged()
        }, { fuelError ->
            swipeRefresh?.isRefreshing = false
            binding.empty?.showConectionErrorState(fuelError) {
                getConvocatories()
            }
        })
    }

    /**
     * Al finalizar la vista de convocatorias
     */
    override fun onDestroy() {
        super.onDestroy()
       // request?.cancel()
    }

    /**
     * Va a la vista de detalle de convocatoria
     */
    private fun goToDetailConvocatory(convocatory: Convocatory?) {
        if (convocatory != null)
            SIMOApplication.goToDetailConvocatorie(activity, convocatory.id)
    }

    /**
     * Al destruir todos los componentes gráficos de la vista
     */
    override fun onDestroyView() {
        super.onDestroyView()
        //clearFindViewByIdCache()
    }

    /**
     * Al iniciar la pantalla
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenConvocatories(context)
    }


    companion object {
        /**
         * Usa este metodo para crear una nueva instancia de la vista
         * en este caso es un fragment
         */
        @JvmStatic
        fun newInstance() =
                ConvocatoriesFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }
}
