package co.gov.cnsc.mobile.simo.fragments.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.activities.EditFormationActivity
import co.gov.cnsc.mobile.simo.adapters.FormationAdapter
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.extensions.setOnItemClickListener
import co.gov.cnsc.mobile.simo.fragments.CVFragment
import co.gov.cnsc.mobile.simo.models.Credential
import co.gov.cnsc.mobile.simo.network.RestAPI
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_my_formation.*


/**
 * Esta clase contiene la funcionalidad de la pantalla de 'Formación Académica' en el CV del usuario
 * de la aplicación
 */
class MyFormationFragment : CVFragment(), SwipeRefreshLayout.OnRefreshListener {

    /**
     * Adapter que contiene el listado de formación académica del usuario
     */
    private var adapter: FormationAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_formation, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureUI()
        getFormations()
    }

    /**
     * Configura el layout UI para agregar el listview de formación académica
     * la acción de swiperefresh y agregar una nueva formación académica
     */
    private fun configureUI() {
        swipeRefresh?.setOnRefreshListener(this)
        //listViewFormation?.emptyView = empty
        listViewFormation?.setOnItemClickListener { position, item ->
            goToEditFormationActivity(item as Credential?, position)
        }
        //empty?.hide()
        buttonAddCredential?.setOnClickListener {
            goToEditFormationActivity(null, null)
        }
    }


    /**
     * Obtiene el listado de formación académica del servidor
     * a través de un web service
     */
    @SuppressLint("RestrictedApi")
    private fun getFormations() {
        swipeRefresh?.isRefreshing = true
        request = RestAPI.getFormations({ credentials ->
            swipeRefresh?.isRefreshing = false
            //empty?.showEmptyState()
            buttonAddCredential?.visibility = View.VISIBLE
            adapter = FormationAdapter(requireActivity(), credentials as ArrayList<Credential>)
            listViewFormation?.adapter = adapter
            adapter?.onDownloadListener = { item, position ->
                if (item.document != null && activity != null) {
                    SIMOApplication.checkIfConnectedByData(requireActivity()) {
                        SIMOApplication.checkPermissionsAndDownloadFile(requireActivity(), item.urlDocument!!,
                                item.nameDocument!!)
                    }
                }
            }
        }, { fuelError ->
            swipeRefresh?.isRefreshing = false
            buttonAddCredential?.visibility = View.INVISIBLE
            empty?.showConectionErrorState {
                getFormations()
            }
            SIMOApplication.showFuelError(activity, fuelError)
        })
    }

    /**
     * Al darle swiperefresh al listado se actualiza
     */
    override fun onRefresh() {
        getFormations()
    }

    /**
     * Al destruir todos los componentes gráficos de la vista
     */
    override fun onDestroyView() {
        super.onDestroyView()
        clearFindViewByIdCache()
    }

    /**
     * Va al detalle de la formación para mostrar o editar dicha información
     */
    private fun goToEditFormationActivity(credential: Credential?, position: Int?) {
        val intent = Intent(context, EditFormationActivity::class.java)
        intent.putExtra("credential", credential)
        intent.putExtra("position", position)
        startActivityForResult(intent, REQUEST_CODE_EDIT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_EDIT && resultCode == Activity.RESULT_OK) {
            onRefresh()
        }
    }

    /**
     * Al iniciar la pantalla del listado de formación académica
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenMyFormations(context)
    }


    companion object {

        const val REQUEST_CODE_EDIT = 1

        /**
         * Usa este metodo para crear una nueva instancia de la vista
         * en este caso es un fragment
         */
        @JvmStatic
        fun newInstance() =
                MyFormationFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }
}
