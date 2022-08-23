package co.gov.cnsc.mobile.simo.fragments.main


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.SIMOFragment
import co.gov.cnsc.mobile.simo.activities.CalendarAlertActivity
import co.gov.cnsc.mobile.simo.activities.DetailAlertActivity
import co.gov.cnsc.mobile.simo.adapters.AlertsAdapter
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.databinding.FragmentMyAlertsBinding
import co.gov.cnsc.mobile.simo.models.Alert
import co.gov.cnsc.mobile.simo.network.RestAPI
import com.github.kittinunf.fuel.core.Request
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_my_alerts.*


/**
 * Esta clase contiene la funcionalidad de la pantalla 'Alertas'
 * la cuál es una pantalla del menú principal de la aplicación
 */
class MyAlertsFragment : SIMOFragment(), SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentMyAlertsBinding? =null
    private val binding get() = _binding!!
    /**
     * Request que se realiza al servidor para traer el listado de alertas
     */
    private var request: Request? = null

    /**
     * Adapter que contiene el listado de alertas a mostrar en la aplicación
     */
    private var adapter: AlertsAdapter? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding =FragmentMyAlertsBinding.inflate(inflater,container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureUI()
        getAlerts()
    }

    /**
     * Configura el layout UI para agregar el listview de alertas
     * y la acción de swiperefresh
     */
    fun configureUI() {
        adapter = AlertsAdapter(activity, ArrayList<Alert>())
        swipeRefresh?.setOnRefreshListener(this)
        listViewAlerts.emptyView = binding.empty
        binding.empty?.hide()
        buttonCalendar.setOnClickListener {
            goToCalendar()
        }
        listViewAlerts.setOnItemClickListener { parent, view, position, id ->
            val item = adapter?.getItem(position)
            goToDetailAlert(position, item)
        }
    }

    private fun goToCalendar() {
        val intent = Intent(activity, CalendarAlertActivity::class.java)
        startActivity(intent)
    }

    /**
     * Al darle swiperefresh al listado se actualiza
     */
    override fun onRefresh() {
        getAlerts()
    }

    /**
     * Obtiene el listado de alertas desde el servidor
     * a través de un web service
     */
    //@SuppressLint("RestrictedApi")
    private fun getAlerts() {
        swipeRefresh?.isRefreshing = true
        request = RestAPI.getAlerts({ alerts ->
            swipeRefresh?.isRefreshing = false
            binding.empty?.showEmptyState()
            adapter?.dataSource = alerts as ArrayList<Alert>
            listViewAlerts?.adapter = adapter
            adapter?.notifyDataSetChanged()
            if (!adapter?.dataSource.isNullOrEmpty()){
                buttonCalendar.show()
            }
        }, { fuelError ->
            swipeRefresh?.isRefreshing = false
            binding.empty?.showConectionErrorState(fuelError) {
                getAlerts()
            }
            //SIMOApplication.showFuelError(activity,fuelError)
        })
    }


    /**
     * Al finalizar la vista de alertas
     */
    override fun onDestroy() {
        super.onDestroy()
        //request?.body
    }

    /**
     * Va a la vista de detalle de una alerta
     * @param position posición de la alerta en el listado
     * @param alert objeto que contiene la información de la alerta
     */
    private fun goToDetailAlert(position: Int, alert: Alert?) {
        val intent = Intent(activity, DetailAlertActivity::class.java)
        intent.putExtra("alert", alert)
        intent.putExtra("position", position)
        startActivityForResult(intent, SIMOApplication.REQUEST_CODE_DETAIL_ALERT)
    }

    /**
     * Obtiene información de la pantalla de detalle de alerta en este caso la posición
     * de la alerta para cambiar el estado a leído
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == SIMOApplication.REQUEST_CODE_DETAIL_ALERT) {
                val position = data?.getIntExtra("position", -1)
                if (position != null && position >= 0) {
                    val alert = adapter?.getItem(position)
                    alert?.isRead = true
                    adapter?.notifyDataSetChanged()
                }
            }
        }
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
        AnalyticsReporter.screenMyAlerts(context)
    }


    companion object {

        /**
         * Usa este metodo para crear una nueva instancia de la vista
         * en este caso es un fragment
         */
        @JvmStatic
        fun newInstance() =
                MyAlertsFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }
}
