package co.gov.cnsc.mobile.simo.fragments.main

import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOFragment
import co.gov.cnsc.mobile.simo.adapters.PaymentsAdapter
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.databinding.FragmentPaymentsBinding
import co.gov.cnsc.mobile.simo.models.Payment
import co.gov.cnsc.mobile.simo.models.SIMO
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.views.FooterLisView

import com.github.kittinunf.fuel.core.Request
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_payments.*


/**
 * Esta clase contiene la funcionalidad de la pantalla de 'Pagos Realizados' en el menú principal
 * de la aplicación
 */
class PaymentsFragment : SIMOFragment(), SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentPaymentsBinding? =null
    private val binding get() = _binding!!
    /**
     * Request que se realiza al servidor para traer el listado de pagos
     */
    private var request: Request? = null

    /**
     * Adapter que contiene el listado de pagos realizados a mostrar en la aplicación
     */
    private var adapter: PaymentsAdapter? = null

    var footerLisView: FooterLisView? = null
    var page: Int = 0




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding= FragmentPaymentsBinding.inflate(inflater,container,false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureUI()
        getPayments()
    }

    /**
     * Configura el layout UI para agregar el listview de pagos
     * y la acción de swiperefresh
     */
    private fun configureUI() {
        adapter = PaymentsAdapter(activity, ArrayList())
        swipeRefresh?.setOnRefreshListener(this)
        listViewPayments.emptyView = binding.empty
        binding.empty?.hide()
    }

    /**
     * Al darle swiperefresh al listado se actualiza
     */
    override fun onRefresh() {
        getPayments()
    }

    /**
     * Obtiene el listado de pagos desde el servidor
     * através de un web service
     */
    private fun getPayments() {
        swipeRefresh?.isRefreshing = true
        val idUser = SIMO.instance.session?.idUser
        request = RestAPI.getPayments(idUser, { convocatories ->
            swipeRefresh?.isRefreshing = false
            binding.empty?.showEmptyState()
            adapter?.dataSource = convocatories as ArrayList<Payment>
            listViewPayments?.adapter = adapter
            adapter?.notifyDataSetChanged()
            //Log.i("DEV","-------------  "+adapter?.getItem(0))
        }, { fuelError ->
            swipeRefresh?.isRefreshing = false
            binding.empty?.showConectionErrorState(fuelError) {
                getPayments()
            }
            //SIMOApplication.showFuelError(activity,fuelError)
        })
    }

    /**
     * Al finalizar la vista de pagos
     */
    override fun onDestroy() {
        super.onDestroy()
    // request?.cancel()
    }

    /**
     * Al destruir todos los componentes gráficos de la vista
     */
    override fun onDestroyView() {
        super.onDestroyView()
        clearFindViewByIdCache()
    }

    /**
     * Al iniciar la pantalla
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenPaymentsDone(context)
    }


    companion object {

        /**
         * Usa este metodo para crear una nueva instancia de la vista
         * en este caso es un fragment
         */
        @JvmStatic
        fun newInstance() =
                PaymentsFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }
}
