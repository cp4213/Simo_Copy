package co.gov.cnsc.mobile.simo.fragments.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.SIMOFragment
import co.gov.cnsc.mobile.simo.adapters.SIMOAutoCompleteTextAdapter
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.models.Convocatory
import co.gov.cnsc.mobile.simo.models.SIMO
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.storage.SIMOResources
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_search.*

/**
 * Esta clase contiene la funcionalidad para la 'Pantalla Inicial' en el menú principal
 * de la aplicación donde se puede buscar empleos por palabra clave o por Proceso de seleción
 */
class SearchFragment : SIMOFragment() {

    /**
     * Proceso de s. seleccionado para realizar la búsqueda
     */
    var selectionProces: Convocatory? =null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)

    }

    /**
     * Cuando el layout es creado se obtienen las diferentes categorias de trabajo de SIMO
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureUI()
        if (SIMO.instance.categories == null) {
            getCategoryJobs()
        } else {
            paint()
        }
    }
    /**
     * Configura la vista del layout para obtener los Procesos de selección,
     * mostrar el número de empleos disponibles, el nombre de usuario y su foto
     */
    private fun configureUI() {
        editSelectionProcess.setOnClickListener {
            SIMOApplication.goToSpinnerListView(
                fragment = this,
                    typeResource = SIMOAutoCompleteTextAdapter.TYPE_SELECTION_PROCESS,
                    query = editSelectionProcess.text.toString(), requestCode = REQUEST_CODE_SELECTIONPROCES)
        }


        editKeyWord.setAdapter(SIMOAutoCompleteTextAdapter(mainActivity as AppCompatActivity, SIMOAutoCompleteTextAdapter.TYPE_KEY_WORDS))
        editKeyWord.threshold = 1
        if (SIMO.instance.isLogged) {
            val urlImage = SIMO.instance.session?.imageUrl
            imagePhotoUser.setImageURI(urlImage)
            imagePhotoUser.visibility = View.VISIBLE
            imagePhotoUser.setOnClickListener {
                mainActivity?.goToMyCVActivity()
            }
            textViewUser.visibility = View.VISIBLE
            textViewUser.text = SIMO.instance.session?.name
        } else {
            imageAfferJobs.visibility = View.VISIBLE
        }
        imageButtonSelecProcess?.setOnClickListener {
            editSelectionProcess?.setText("")
            selectionProces = null
        }
        imageButtonCloseCharge?.setOnClickListener {
            editKeyWord?.setText("")
        }

        buttonSearch.setOnClickListener {
            onSearch()
        }
    }

    /**
     * Obtiene el número total de empleos por categoría
     */
    private fun getCategoryJobs() {
        ProgressBarDialog.startProgressDialog(activity as Activity)
        RestAPI.getCategoryOffers({ categories ->
            ProgressBarDialog.stopProgressDialog()
            SIMO.instance.categories = categories
            paint()
        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            SIMOApplication.showFuelError(activity, fuelError)
        })
    }

    /**
     * Pinta el número de empleos por categoría en la vista
     * para mostrarlos
     */
    private fun paint() {
        val total = SIMO.instance.categoryTotal?.value
        val numberProfesional = SIMO.instance.categoryProfessional?.value
        val numberAssistencial = SIMO.instance.categoryAsistencial?.value
        val numberTecnical = SIMO.instance.categoryTecnic?.value
        textTotalJobs.text = getString(R.string.we_have_number_job_offers, total)
        textViewProfessional.text = getString(R.string.number_professional_jobs, numberProfesional)
        textViewAssistencial.text = getString(R.string.number_assistencial_jobs, numberAssistencial)
        textViewTecnical.text = getString(R.string.number_tecnical_jobs, numberTecnical)

        if (numberProfesional != null && numberProfesional <= 0) {
            textViewProfessional.visibility = View.GONE
        }
        if (numberAssistencial != null && numberAssistencial <= 0) {
            textViewAssistencial.visibility = View.GONE
        }
        if (numberTecnical != null && numberTecnical <= 0) {
            textViewTecnical.visibility = View.GONE
        }

        if (total != null && total <= 0) {
            cardView.visibility = View.INVISIBLE
            textViewNoOffers.visibility = View.VISIBLE
        }

    }

    /**
     * Valida la información del formulario para ser filtrada por proceso de selección o palabra clave
     */
    private fun validateForm(): Boolean {
        var validate = true
        editSelectionProcess.error = null
        val selProces = SIMOResources.findSelectionProcess(requireActivity(), editSelectionProcess.text.toString())
        if (editSelectionProcess?.text?.isNullOrBlank() == false) {
            if (selProces == null) {
                editSelectionProcess.error = getString(R.string.not_valid_field)
                validate = false
            }
        }
        return validate
    }

    /**
     * Evento cuando se da tap sobre el botón Buscar
     */
    private fun onSearch() {
        if (validateForm()) {
            val keyWord = editKeyWord.text.toString()
            SIMOResources.tryAddKeyWord(requireContext(), keyWord)
            goToWorkOffers()
        }
    }

    /**
     * Va a la vista de lista de empleos OPEC
     */
    private fun goToWorkOffers() {
        val keyWord = editKeyWord.text.toString()
        val selectionProces = selectionProces
        SIMOApplication.goToWorkOffers(context = requireActivity(), keyWord = keyWord,convocatory = selectionProces)
    }

    /**
     * Al destruir todos los componentes gráficos de la vista
     */
    override fun onDestroyView() {
        super.onDestroyView()
        clearFindViewByIdCache()
    }

    /**
     * Al volver a esta pantalla desde cualquier otro punto de la aplicación
     * Se actualizan los datos de nombre y foto del usuario
     */
    override fun onResume() {
        super.onResume()
        val urlImage = SIMO.instance.session?.imageUrl
        imagePhotoUser?.setImageURI(urlImage)
        textViewUser?.text = SIMO.instance.session?.name
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResultSIMOResources(requestCode, resultCode, data)
    }

    /**
     * Obtiene el proceso de selección por el usuario desde el popup de proceso de selección
     */
    private fun onActivityResultSIMOResources(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECTIONPROCES) {
                selectionProces = data?.getParcelableExtra("item")
                editSelectionProcess.setText(selectionProces?.toString())
            }
        }
    }

    /**
     * Al iniciar la pantalla
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenMainSearch(context)
    }


    companion object {

        const val REQUEST_CODE_SELECTIONPROCES = 0

        /**
         * Usa este metodo para crear una nueva instancia de la vista
         * en este caso es un fragment
         */
        @JvmStatic
        fun newInstance() =
                SearchFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }
}
