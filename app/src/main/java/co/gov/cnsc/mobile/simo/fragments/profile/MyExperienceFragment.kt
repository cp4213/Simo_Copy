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
import co.gov.cnsc.mobile.simo.activities.EditExperienceActivity
import co.gov.cnsc.mobile.simo.adapters.ExperienceAdapter
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.extensions.setOnItemClickListener
import co.gov.cnsc.mobile.simo.fragments.CVFragment
import co.gov.cnsc.mobile.simo.models.Credential
import co.gov.cnsc.mobile.simo.network.RestAPI
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_my_experience.*

/**
 * Esta clase contiene la funcionalidad de la pantalla de 'Experiencia Laboral' en el CV del usuario
 * de la aplicación
 */
class MyExperienceFragment : CVFragment(), SwipeRefreshLayout.OnRefreshListener {

    /**
     * Adapter que contiene el listado de experiencia laboral del usuario
     */
    var adapter: ExperienceAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_experience, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureUI()
        getExperience()
    }

    /**
     * Configura el layout UI para agregar el listview de experiencia laborsl
     * y la acción de swiperefresh y agregar una nueva experiencia laboral
     */
    private fun configureUI() {
        swipeRefresh?.setOnRefreshListener(this)
        //listViewExperience?.emptyView = empty
        listViewExperience?.setOnItemClickListener { position, item ->
            goToEditExperienceActivity(item as Credential?, position)
        }
        //empty?.hide()
        buttonAddCredential?.setOnClickListener {
            goToEditExperienceActivity(null, null)
        }
    }

    /**
     * Al darle swiperefresh al listado se actualiza
     */
    override fun onRefresh() {
        getExperience()
    }

    /**
     * Obtiene el listado de experiencia laboral del servidor
     * através de un web service
     */
    @SuppressLint("RestrictedApi")
    private fun getExperience() {
        swipeRefresh?.isRefreshing = true
        listViewExperience?.visibility = View.INVISIBLE
        request = RestAPI.getExperiences({ credentials ->
            if (activity != null) {
                swipeRefresh?.isRefreshing = false
                //empty?.showEmptyState()
                buttonAddCredential?.visibility = View.VISIBLE
                adapter = ExperienceAdapter(requireActivity(), credentials as ArrayList<Credential>)
                adapter?.onDownloadListener = { item, position ->
                    if (item.document != null && activity != null) {
                        SIMOApplication.checkIfConnectedByData(requireActivity()) {
                            SIMOApplication.checkPermissionsAndDownloadFile(requireActivity(), item.urlDocument!!,
                                    item.nameDocument!!)
                        }
                    }
                }
                listViewExperience?.visibility = View.VISIBLE
                listViewExperience?.adapter = adapter
            }
        }, { fuelError ->
            swipeRefresh?.isRefreshing = false
            empty?.showConectionErrorState(fuelError) {
                getExperience()
            }
            listViewExperience?.visibility = View.VISIBLE
            buttonAddCredential?.visibility = View.INVISIBLE
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
     * Va al detalle de la experiencia para mostrar o editar dicha información
     */
    private fun goToEditExperienceActivity(credential: Credential?, position: Int?) {
        val intent = Intent(context, EditExperienceActivity::class.java)
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
     * Al iniciar la pantalla del listado de experiencias
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenMyExperience(context)
    }


    companion object {

        const val REQUEST_CODE_EDIT = 1

        /**
         * Usa este metodo para crear una nueva instancia de la vista
         * en este caso es un fragment
         */
        @JvmStatic
        fun newInstance() =
                MyExperienceFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }
}
