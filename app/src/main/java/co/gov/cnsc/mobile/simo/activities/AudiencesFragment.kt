package co.gov.cnsc.mobile.simo.activities

import android.content.Intent
import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOFragment
import co.gov.cnsc.mobile.simo.adapters.AudiencesAdapter
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.models.Audience
import com.github.kittinunf.fuel.core.Request
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_audiences.*


class AudiencesFragment : SIMOFragment(), SwipeRefreshLayout.OnRefreshListener{

    /**
     * Adapter que contiene el listado de convocatorias a mostrar en la aplicación
     */
    private var adapter: AudiencesAdapter? = null
    /**
     * Request que se realiza al servidor para traer el listado de convocatorias
     */
    private var request: Request? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_audiences, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureUI()
        getAudiences()
    }

    /**
     * Función que trae las Audiencias de la API
     */


    private fun getAudiences() {
        //AJUSTAR
        val aud:Audience = Audience("1","nombre1","procesoselección","dateIni","dateEnd")
        val aud2:Audience = Audience("2","nombre2","procesoselección","dateIni","dateEnd")
        val aud3:Audience = Audience("3","nombre3","procesoselección","dateIni","dateEnd")
        var audList:ArrayList<Audience> = arrayListOf(aud,aud2,aud3)
        //audList?.add(aud)
        //audList?.add(aud2)
        //audList?.add(aud3)
        adapter?.dataSource = audList
        listViewAudiences.adapter=adapter
        adapter?.notifyDataSetChanged()
    }

    /**
     * Configura el layout UI para agregar el listview de Audiencias
     * y la acción de swiperefresh
     */
    private fun configureUI() {
        adapter = AudiencesAdapter(activity, ArrayList<Audience>())

        adapter?.onConsultJob = { item: Audience, position: Int ->

            Toast.makeText(activity,"onConsultJob", Toast.LENGTH_SHORT).show()
            goToVacancyJobs(item)
            //SIMOApplication.goToWorkOffers(context = context!!, convocatory = item)

        }
        adapter?.setPrioriry = { item: Audience, position: Int ->

            Toast.makeText(activity,"setPrioriry", Toast.LENGTH_SHORT).show()
            //SIMOApplication.goToWorkOffers(context = context!!, convocatory = item)

        }
        adapter?.reportConsult = { item: Audience, position: Int ->

            Toast.makeText(activity,"reportConsult", Toast.LENGTH_SHORT).show()
            //SIMOApplication.goToWorkOffers(context = context!!, convocatory = item)

        }


        swipeRefreshAudiences?.setOnRefreshListener(this)
        listViewAudiences.emptyView = empty
        empty?.hide()
        /* listViewAlerts.setOnItemClickListener { parent, view, position, id ->
             val item = adapter?.getItem(position)
             goToDetailAlert(position, item)
         }*/
    }


    private fun goToVacancyJobs(audience: Audience) {
        val intent = Intent(activity, AudienceVacancyJobs::class.java)
        /*intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.action = action
        intent.putExtra("username", username)*/
        intent.putExtra("audience",audience)
        intent.putExtra("name",audience.name)
        intent.putExtra("s_p",audience.selectionProces)
        intent.putExtra("dini",audience.dateIni)
        intent.putExtra("dend",audience.dateEnd)
        startActivity(intent)
    }
    /**
     * Al finalizar la vista de Audiencias
     */
    override fun onDestroy() {
        super.onDestroy()
        //request?.interrupt()
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
        AnalyticsReporter.screenConvocatories(context)
    }

    companion object {
        /**
         * Usa este metodo para crear una nueva instancia de la vista
         * en este caso es un fragment
         */
        @JvmStatic
        fun newInstance() =
            AudiencesFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
    /**
     * Al darle swiperefresh al listado se actualiza el listado
     */
    override fun onRefresh() {
        getAudiences()
    }
}