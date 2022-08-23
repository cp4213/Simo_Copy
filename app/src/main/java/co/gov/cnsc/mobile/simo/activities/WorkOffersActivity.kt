package co.gov.cnsc.mobile.simo.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.adapters.WorkOfferAdapter
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.databinding.ActivityWorkOffersBinding
import co.gov.cnsc.mobile.simo.models.Filter
import co.gov.cnsc.mobile.simo.models.SIMO
import co.gov.cnsc.mobile.simo.models.WorkOffer
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import co.gov.cnsc.mobile.simo.views.FooterLisView
import kotlinx.android.synthetic.main.activity_work_offers.*


/**
 * Esta clase administra la funcionalidad de la pantalla de listado de ofertas de trabajo disponibles
 */
class WorkOffersActivity : SIMOActivity(), SwipeRefreshLayout.OnRefreshListener {

    val REQUEST_SEARCH = 1
    var filter: Filter? = null
    var adapter: WorkOfferAdapter? = null
    var footerLisView: FooterLisView? = null
    var page: Int = 0
    private lateinit var binding: ActivityWorkOffersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkOffersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showToolbarBack()
        setTextTitleToolbar(R.string.list_opec)
        filter = intent?.extras?.getParcelable("filter")
        configureUI()
        getWorkOffers()
    }

    /**
     * Configura componentes gráficos de la pantalla: ListView, eventos y paginado de empleos
     */
    fun configureUI() {
        adapter = WorkOfferAdapter(this, ArrayList<WorkOffer>(), SIMO.instance.isLogged)
        adapter?.onConvocatoryListener = { workOffer, position ->
            goToDetailConvocatorie(workOffer.job?.convocatory?.id!!)
        }
        adapter?.onFavoriteListener = { workOffer, position ->
            setOrRemoveFromFavorites(workOffer)
        }
        listView.adapter = adapter
        swipeRefresh?.setOnRefreshListener(this)
        swipeRefresh?.isRefreshing = true
        listView.setOnItemClickListener { parent, view, position, id ->
            val workOffer = adapter?.getItem(position)
            if (workOffer != null)
                goToDetailWorkOfferActivity(workOffer.job?.id!!, workOffer, workOffer.favorite, workOffer.inscriptionId, workOffer.statusInscription)
        }

        /**
         * Habilita el evento de descarga del manual de funciones para cada empleo (solo para los empleos que lo incluyan) */
        adapter?.onDownloadFunctionsListener = { workOffer, position ->
            SIMOApplication.checkIfConnectedByData(this) {
                /*if (workOffer.urlFileFunctionsDocument != null && workOffer.nameFileFunctionsDocument != null) {
                    SIMOApplication.checkPermissionsAndDownloadFile(this, workOffer.urlFileFunctionsDocument!!, workOffer.nameFileFunctionsDocument!!)
                } else {
                    rowAttachment.visibility = View.GONE
                }*/
                SIMOApplication.checkPermissionsAndDownloadFile(this, workOffer.urlFileFunctionsDocument!!, workOffer.nameFileFunctionsDocument!!)

            }
        }

        listView.emptyView = binding.empty
         //binding.emptyView =empty
        binding.empty.visibility = View.INVISIBLE
        footerLisView = FooterLisView(this)
        listView.addFooterView(footerLisView)
        val endScrollListener = SIMOApplication.endScrollListener {
            if (footerLisView?.isLoading == false) {
                getMoreWorkOffers()
            }
        }
        listView.setOnScrollListener(endScrollListener)
    }

    /**
     * Realiza un llamado al API Rest para obtener el listado de empleos paginados de 20 en 20
     */
    private fun getWorkOffers() {
        if (page == 0) {
            swipeRefresh?.isRefreshing = true
        } else {
            footerLisView?.showLoading()
        }
        RestAPI.getWorkOffers(page, filter, { workOffers ->
            swipeRefresh?.isRefreshing = false
            binding.empty?.showEmptyState()
            footerLisView?.hideLoading()
            if (page == 0) {
                adapter?.clear()
            }
            adapter?.addItems(workOffers)
            if (workOffers.size < RestAPI.RESULTS_PER_PAGE) {
                page = RestAPI.MAX_PAGES
            }
        }, { error ->
            footerLisView?.hideLoading()
            binding.empty?.showConectionErrorState(error) {
                getWorkOffers()
            }
            swipeRefresh?.isRefreshing = false
            page = RestAPI.MAX_PAGES
            adapter?.clear()
            adapter?.notifyDataSetChanged()
        }, { totalJobs ->
            if (totalJobs != null) {
                textTitleOffers.text = getString(R.string.number_offers_founded, totalJobs)
            } else {
                textTitleOffers.text = ""
            }
        })
    }

    private fun setOrRemoveFromFavorites(workOfferOld: WorkOffer?) {
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.setOrRemoveAsFavorite(workOfferOld?.id, workOfferOld?.inscriptionId, !workOfferOld?.favorite!!,
                { workOffer ->
                    ProgressBarDialog.stopProgressDialog()
                    workOfferOld.favorite = workOffer.favorite
                    workOfferOld.inscriptionId = workOffer.inscriptionId
                    adapter?.notifyDataSetChanged()
                }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
        })
    }

    /**
     * Realiza un llamado a la siguiente pagina que trae el listado de empleos
     */
    private fun getMoreWorkOffers() {
        if (page < RestAPI.MAX_PAGES) {
            page++
            getWorkOffers()
        }
    }

    /**
     * Abre el detalle de una oferta de trabajo
     * @param idJob id de la oferta seleccionada
     * @param workOffer oferta seleccionada
     * @param isFavorite si la oferta de trabajo está seleccionada como favorita
     * @param idInscription id de la inscripción del trabajo si la tiene
     * @param statusInscription si la oferta está seleccionada como Favorita, Preinscrita o Inscrita
     */
    private fun goToDetailWorkOfferActivity(idJob: String, workOffer: WorkOffer, isFavorite: Boolean?, idInscription: String?, statusInscription: String?) {
       // Log.d("DEV","WorkOffersA_goToDetailWorkOfferActivity")
        val intent = SIMOApplication.getIntentForWorkDetail(this, idJob, workOffer, isFavorite, idInscription, statusInscription)
        startActivityForResult(intent, REQUEST_CODE_WORK_DETAIL)
    }

    /**
     * Abre el detalle de la convocatoria de la oferta de trabajo seleccionada
     * @param idConvocatory id de la convocatoria de trabajo
     */
    private fun goToDetailConvocatorie(idConvocatory: String) {
      //  Log.d("DEV","WorkOffersA_goToDetailConvocatorie")
        SIMOApplication.goToDetailConvocatorie(this, idConvocatory)
    }

    /**
     * Muestra el menú Búsqueda avanzada de la parte superior derecha
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.advanced_search, menu)
        return true
    }

    /**
     * Administra las opciones del menú en caso de ser seleccionadas
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.menu_search -> {
                goToAdvancedSearch()
            }
            else -> return super.onOptionsItemSelected(item)

        }
        return true
    }

    /**
     * Abre la pantalla de Búsqueda Avanzada
     */
    private fun goToAdvancedSearch() {
        val intent = Intent(this, AdvanceSearchActivity::class.java)
        intent.putExtra("filter", filter)
        startActivityForResult(intent, REQUEST_SEARCH)
    }

    /**
     * Refresca el listado de empleos cuando se da un evento swipe to refresh al listado
     */
    override fun onRefresh() {
        page = 0
        getWorkOffers()
    }

    /**
     * Atrapa un filtro de la pantalla de búsqueda avanzada y realiza el filtro
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val bundle = data?.extras
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SEARCH) {
                this.filter = bundle?.get("filter") as Filter
                onRefresh()
            }
        }
    }

    /**
     * Ejecución al comenzar una activity
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenListJobs(this)
        Log.d("DEV","WorkOffersA")
    }

    companion object {
        const val REQUEST_CODE_WORK_DETAIL = 10
    }
}
