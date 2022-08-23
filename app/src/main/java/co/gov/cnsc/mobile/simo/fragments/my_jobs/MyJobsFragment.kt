package co.gov.cnsc.mobile.simo.fragments.my_jobs

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.SIMOFragment
import co.gov.cnsc.mobile.simo.activities.InscriptionResultsActivity
import co.gov.cnsc.mobile.simo.adapters.MyInscriptionsAdapter
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.databinding.FragmentMyJobsBinding
import co.gov.cnsc.mobile.simo.models.Inscription
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_my_jobs.*

/**
 * Esta clase contiene la funcionalidad de cualquiera de las pantallas de Mis Empleos:
 * Favoritos, Inscritos o Confirmados de la aplicación
 */
class MyJobsFragment : SIMOFragment(), SwipeRefreshLayout.OnRefreshListener {


    private var _binding: FragmentMyJobsBinding? =null
    private val binding get() = _binding!!
    /**
     * Pestaña a mostrar dependiendo del empleo Favorito, Confirmado, Inscrito
     */
    private var status: String? = null

    /**
     * Adapter que contendrá la lista de empleos a mostrar en la pestaña
     */
    private var adapter: MyInscriptionsAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            status = it.getString(STATUS_JOB)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding= FragmentMyJobsBinding.inflate(inflater,container,false)
        return binding.root
    }

    /**
     * Al crear el layout lo configura y obtiene el listado de empleos
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureUI()
        getMyJobs()
    }

    /**
     * Configura la UI para mostrar el listado de empleos y la acción
     * swiperefresh para actualizarlos
     */
    private fun configureUI() {
        swipeRefresh?.setOnRefreshListener(this)
        adapter = MyInscriptionsAdapter(requireContext(), ArrayList())
        adapter?.onConvocatoryListener = { inscription, position ->
            SIMOApplication.goToDetailConvocatorie(context, inscription.convocatoryId)
        }
        adapter?.onItemListener = { inscription, position ->
            val intent = SIMOApplication.getIntentForWorkDetail(context, inscription.jobId, null, inscription.isFavorite, inscription.id, inscription.statusInscription)
            startActivityForResult(intent, REQUEST_CODE_WORK_DETAIL)
        }
        adapter?.onFavoriteListener = { inscription, position ->
            activity?.let {
                SIMOApplication.showConfirmDialog(requireActivity(), R.string.remove_favorites,
                        R.string.are_you_sure_remove_favorites, R.string.yes, { _, _ ->
                    setOrRemoveFromFavorites(inscription, position)
                }, R.string.no, { _, _ ->

                })
            }
        }
        adapter?.onQueryResults = { inscription, position ->
            goToInscriptionResults(inscription.jobId, inscription.id)
        }

        adapter?.onDownloadConstancyListener = { inscription, position ->
            activity?.let {
                SIMOApplication.checkIfConnectedByData(it) {
                    SIMOApplication.checkPermissionsAndDownloadFile(it, inscription.urlFileConstancy!!, inscription.nameFileConstancy!!)
                }
            }
        }

        listViewJobs?.emptyView = binding.empty
        listViewJobs?.adapter = adapter
        binding.empty?.hide()
        listViewJobs?.setOnItemClickListener { parent, view, position, id ->
            val item = adapter?.getItem(position)
            val intent = SIMOApplication.getIntentForWorkDetail(context, item?.jobId!!, null, item.isFavorite, item.id, item.statusInscription)
            if(position==2){
                //intent.
            }
            startActivityForResult(intent, REQUEST_CODE_WORK_DETAIL)
        }

    }

    /**
     * Va a la pantalla de resultados de una inscripción
     * @param idJob id del empleo
     * @param idInscription id de la inscripción
     */
    private fun goToInscriptionResults(idJob: String?, idInscription: String?) {
        val intent = Intent(activity, InscriptionResultsActivity::class.java)
        intent.putExtra("id_job", idJob)
        intent.putExtra("id_inscription", idInscription)
        startActivity(intent)
    }

    /**
     * Quita o agrega un empleo como favorito
     * @param inscription empleo a quitar o agregar de favoritos
     * @param position posición en el listado
     */
    private fun setOrRemoveFromFavorites(inscription: Inscription?, position: Int?) {
        if (activity != null)
            ProgressBarDialog.startProgressDialog(requireActivity())
        RestAPI.setOrRemoveAsFavorite(inscription?.jobId, inscription?.id, false,
                { workOffer ->
                    ProgressBarDialog.stopProgressDialog()
                    adapter?.remove(position)
                }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
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
     * Obtiene el listado de empleos dependiendo de su tipo
     * Favorito, Inscrito o Preinscrito
     */
    private fun getMyJobs() {
        swipeRefresh?.isRefreshing = true
        RestAPI.getMyJobs(status!!, { jobs ->
            swipeRefresh?.isRefreshing = false
            binding.empty?.showEmptyState()
            adapter?.dataSource = jobs as ArrayList<Inscription>
            adapter?.notifyDataSetChanged()
        }) { fuelError ->
            swipeRefresh?.isRefreshing = false
            SIMOApplication.showFuelError(context, fuelError)
            binding.empty?.showConectionErrorState(fuelError) {
                getMyJobs()
            }
        }
    }

    /**
     * Al darle swiperefresh al listado se actualiza
     */
    override fun onRefresh() {
        getMyJobs()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_WORK_DETAIL && resultCode == RESULT_OK && status?.equals("F") == true) {
            onRefresh()
        }
    }

    /**
     * Al iniciar la pantalla
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenMyJobs(context)
    }


    companion object {

        private const val REQUEST_CODE_WORK_DETAIL = 10
        private const val STATUS_JOB = "status_job"

        /**
         * Usa este metodo para crear una nueva instancia de la vista
         * en este caso es un fragment
         * @param status estado del empleo F,PI,I
         */
        @JvmStatic
        fun newInstance(status: String) =
                MyJobsFragment().apply {
                    arguments = Bundle().apply {
                        putString(STATUS_JOB, status)
                    }
                }
    }
}
