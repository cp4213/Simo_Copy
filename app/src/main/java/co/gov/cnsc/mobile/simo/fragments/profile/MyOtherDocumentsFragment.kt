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
import co.gov.cnsc.mobile.simo.activities.EditOtherDocumentActivity
import co.gov.cnsc.mobile.simo.adapters.OtherDocumentsAdapter
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.databinding.FragmentMyOtherDocumentsBinding
import co.gov.cnsc.mobile.simo.extensions.setOnItemClickListener
import co.gov.cnsc.mobile.simo.fragments.CVFragment
import co.gov.cnsc.mobile.simo.models.Credential
import co.gov.cnsc.mobile.simo.network.RestAPI
import kotlinx.android.synthetic.*


/**
 * Esta clase contiene la funcionalidad de la pantalla de 'Otros documentos' en el CV del usuario
 * de la aplicación
 */
class MyOtherDocumentsFragment : CVFragment(), SwipeRefreshLayout.OnRefreshListener {
    private var _binding: FragmentMyOtherDocumentsBinding? =null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding =FragmentMyOtherDocumentsBinding.inflate(inflater,container,false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureUI()
        getOtherProducts()
    }

    /**
     * Configura el layout UI para agregar el listview de otros documentos
     * la acción de swiperefresh y agregar una nuevo documento
     */
    private fun configureUI() {
        binding.swipeRefresh?.setOnRefreshListener(this)
        binding.listViewDocuments?.emptyView = binding.empty
        binding.listViewDocuments?.setOnItemClickListener { position, item ->
            goToEditOtherDocumentActivity(item as Credential?, position)
        }
        binding.empty?.hide()
        binding.buttonAddCredential?.setOnClickListener {
            goToEditOtherDocumentActivity(null, null)
        }
    }

    /**
     * Al darle swiperefresh al listado se actualiza
     */
    override fun onRefresh() {
        getOtherProducts()
    }

    /**
     * Obtiene el listado de otros documentos del servidor
     * através de un web service
     */
    @SuppressLint("RestrictedApi")
    private fun getOtherProducts() {
        binding.swipeRefresh?.isRefreshing = true
        request = RestAPI.getOtherDocuments({ credentials ->
            binding.swipeRefresh?.isRefreshing = false
            binding.empty?.showEmptyState()
            binding.buttonAddCredential?.visibility = View.VISIBLE
            val adapter = OtherDocumentsAdapter(requireContext(), credentials as ArrayList<Credential>)
            adapter.onDownloadListener = { item, position ->
                if (item.document != null && activity != null) {
                    SIMOApplication.checkIfConnectedByData(requireActivity()) {
                        SIMOApplication.checkPermissionsAndDownloadFile(requireActivity(), item.urlDocument!!,
                                item.nameDocument!!)
                    }
                }
            }
            binding.listViewDocuments?.adapter = adapter
        }, { fuelError ->
            binding.swipeRefresh?.isRefreshing = false
            binding.buttonAddCredential?.visibility = View.INVISIBLE
            binding.empty?.showConectionErrorState {
                getOtherProducts()
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
     * Va al detalle del documento para mostrar o editar dicha información
     */
    private fun goToEditOtherDocumentActivity(credential: Credential?, position: Int?) {
        val intent = Intent(context, EditOtherDocumentActivity::class.java)
        intent.putExtra("credential", credential)
        intent.putExtra("position", position)
        startActivityForResult(intent, REQUEST_CODE_EDIT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MyIntelectualProdFragment.REQUEST_CODE_EDIT && resultCode == Activity.RESULT_OK) {
            onRefresh()
        }
    }

    /**
     * Al iniciar la pantalla del listado de otros documentos
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenMyOtherDocuments(context)
    }


    companion object {

        const val REQUEST_CODE_EDIT = 1
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment SearchFragment.
         */
        @JvmStatic
        fun newInstance() =
                MyOtherDocumentsFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }
}
