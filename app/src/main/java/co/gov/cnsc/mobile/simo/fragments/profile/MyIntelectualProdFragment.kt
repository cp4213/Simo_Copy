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
import co.gov.cnsc.mobile.simo.activities.EditProdIntelectualActivity
import co.gov.cnsc.mobile.simo.adapters.IntelectualProdAdapter
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.extensions.setOnItemClickListener
import co.gov.cnsc.mobile.simo.fragments.CVFragment
import co.gov.cnsc.mobile.simo.models.Credential
import co.gov.cnsc.mobile.simo.network.RestAPI
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_my_intelectual_prods.*


/**
 * Esta clase contiene la funcionalidad de la pantalla de 'Producción Intelectual' en el CV del usuario
 * de la aplicación
 */
class MyIntelectualProdFragment : CVFragment(), SwipeRefreshLayout.OnRefreshListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_intelectual_prods, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureUI()
        getIntelectualProducts()
    }

    /**
     * Configura el layout UI para agregar el listview de producciones intelectuales
     * la acción de swiperefresh y agregar una nueva producción intelectual
     */
    private fun configureUI() {
        swipeRefresh?.setOnRefreshListener(this)
        listViewProd?.emptyView = empty
        listViewProd?.setOnItemClickListener { position, item ->
            goToEditIntelectualProdActivity(item as Credential?, position)
        }
        empty?.hide()
        buttonAddCredential?.setOnClickListener {
            goToEditIntelectualProdActivity(null, null)
        }
    }

    /**
     * Al darle swiperefresh al listado se actualiza
     */
    override fun onRefresh() {
        getIntelectualProducts()
    }

    /**
     * Obtiene el listado de producción intelectual del servidor
     * através de un web service
     */
    @SuppressLint("RestrictedApi")
    private fun getIntelectualProducts() {
        swipeRefresh?.isRefreshing = true
        request = RestAPI.getProductsIntelectual({ credentials ->
            swipeRefresh?.isRefreshing = false
           // empty?.showEmptyState()
            buttonAddCredential?.visibility = View.VISIBLE
            val adapter = IntelectualProdAdapter(requireActivity(), credentials as ArrayList<Credential>)
            adapter.onDownloadListener = { item, position ->
                if (item.document != null && activity != null) {
                    SIMOApplication.checkIfConnectedByData(requireActivity()) {
                        SIMOApplication.checkPermissionsAndDownloadFile(requireActivity(), item.urlDocument!!,
                                item.nameDocument!!)
                    }
                }
            }
            listViewProd?.adapter = adapter
        }, { fuelError ->
            swipeRefresh?.isRefreshing = false
            buttonAddCredential?.visibility = View.INVISIBLE
            empty?.showConectionErrorState {
                getIntelectualProducts()
            }
            SIMOApplication.showFuelError(activity, fuelError)
        })
    }

    /**
     * Al destruir todos los componentes gráficos de la vista
     */
    override fun onDestroyView() {
        super.onDestroyView()
        clearFindViewByIdCache()
    }

    /**
     * Va al detalle de la producción intelectual para mostrar o editar dicha información
     */
    private fun goToEditIntelectualProdActivity(credential: Credential?, position: Int?) {
        val intent = Intent(context, EditProdIntelectualActivity::class.java)
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
        AnalyticsReporter.screenMyIntelectualProduct(context)
    }


    companion object {

        const val REQUEST_CODE_EDIT = 1

        /**
         * Usa este metodo para crear una nueva instancia de la vista
         * en este caso es un fragment
         */
        @JvmStatic
        fun newInstance() =
                MyIntelectualProdFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }
}
