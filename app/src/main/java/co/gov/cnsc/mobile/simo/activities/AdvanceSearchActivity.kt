package co.gov.cnsc.mobile.simo.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.adapters.SIMOAutoCompleteTextAdapter
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.extensions.showDropdownWhenGetFocus
import co.gov.cnsc.mobile.simo.models.*
import co.gov.cnsc.mobile.simo.views.TextInputLayout
import kotlinx.android.synthetic.main.activity_advance_search.*
import kotlinx.android.synthetic.main.activity_edit_formation.*


/**
 * Esta activity da la funcionalidad a la pantalla de 'Búsqueda Avanzada'
 */
class AdvanceSearchActivity : SIMOActivity() {

    /**
     * Entidad sobre la cuál se quiere filtrar
     */
    private var entity: Entity? = null

    /**
     * Departamento sobre el que se quiere filtrar
     */
    private var department: Department? = null

    /**
     * Municipio sobre el cual se quiere filtrar
     */
    private var city: City? = null

    /**
     * Convocatoria sobre la cuál se quiere filtrar
     */
    private var convocatory: Convocatory? = null

    /**
     * Rango salarial sobre el cuál se quiere filtrar
     */
    private var range: SalarialRange? = null

    /**
     * Nivel sobre el cuál se quiere filtrar
     */
    private var level: IdName? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advance_search)
        showToolbarBack()
        setTextTitleToolbar(R.string.advanced_search)
        val bundle = intent?.extras
        configureUI(bundle?.getParcelable("filter"))
    }


    /**
     * Configura los elementos UI del layout
     * @param filter filtro aplicado en el listado OPEC
     */
    fun configureUI(filter: Filter?) {
        editKeyWord?.setAdapter(SIMOAutoCompleteTextAdapter(this, SIMOAutoCompleteTextAdapter.TYPE_KEY_WORDS))
        editKeyWord?.showDropdownWhenGetFocus()
        editKeyWord.setText(filter?.keyWord)

        entity = filter?.entity //2
        editTextEntity.setOnClickListener {
            SIMOApplication.goToSpinnerListView(
                activity = this, typeResource = SIMOAutoCompleteTextAdapter.TYPE_ENTITIES,
                    query = editTextEntity.text.toString(), requestCode = REQUEST_CODE_ENTITY)
        }
        editTextEntity.setText(entity?.toString())
        buttonClearEntity.setOnClickListener {
            entity = null
            editTextEntity.setText("")
        }

        department = filter?.department //3
        editDepartment.setOnClickListener {
            SIMOApplication.goToSpinnerListView(
                activity = this, typeResource = SIMOAutoCompleteTextAdapter.TYPE_DEPARTMENTS_OPEC,
                    query = editDepartment.text.toString(), requestCode = REQUEST_CODE_DEPARTMENT_OPEC)
        }
        editDepartment.setText(department?.realName)
        buttonClearDepartment.setOnClickListener {
            department = null
            editDepartment.setText("")
        }

        city = filter?.city //4
        editCities?.setOnClickListener {
            if (department != null) {
                SIMOApplication.goToSpinnerListView(
                    activity = this, typeResource = SIMOAutoCompleteTextAdapter.TYPE_CITIES_OPEC,
                        query = editCities.text.toString(), requestCode = REQUEST_CODE_CITY_OPEC, idFilter = department?.id)
            }
        }
        editCities.setText(city?.name)
        buttonClearCity.setOnClickListener {
            city = null
            editCities.setText("")
        }

        convocatory = filter?.convocatory //5
        editConvocatory.setOnClickListener {
            SIMOApplication.goToSpinnerListView(
                activity = this, typeResource = SIMOAutoCompleteTextAdapter.TYPE_SELECTION_PROCESS,
                    query = editConvocatory.text.toString(), requestCode = REQUEST_CODE_SELECTIONPROCES)
        }
        editConvocatory.setText(convocatory?.name)
        buttonClearConvocatory.setOnClickListener {
            convocatory = null
            editConvocatory.setText("")
        }

        range = filter?.salarialRange //6
        editRange.setOnClickListener {
            SIMOApplication.goToSpinnerListView(
                activity = this, typeResource = SIMOAutoCompleteTextAdapter.TYPE_SALARIAL_RANGE,
                    query = editRange.text.toString(), requestCode = REQUEST_CODE_RANGE)
        }
        editRange.setText(range?.toString())

        //Limpia el valor seleccionado
        buttonClearRange.setOnClickListener {
            range = null
            editRange.setText("")
        }

        editLowerLimit.setText(filter?.lowerLimitSR) //7
        editUpperLimit.setText(filter?.upperLimitSR) //8

        level = filter?.level //9

        editLevel.setOnClickListener {
            SIMOApplication.goToSpinnerListView(
                activity = this, typeResource = SIMOAutoCompleteTextAdapter.TYPE_LEVELS,
                    query = editLevel.text.toString(), requestCode = REQUEST_CODE_LEVEL)
        }
        editLevel.setText(level?.toString())

        buttonClearLevel.setOnClickListener {
            level = null
            editLevel.setText("")
        }

        editOPEC.setText(filter?.numberOPEC) // 10
    }


    /**
     * Limpia todos los campos de la UI
     */
    private fun clearFields() {
        editKeyWord.setText("")
        entity = null
        editTextEntity.setText("")
        department = null
        editDepartment.setText("")
        city = null
        editCities.setText("")
        range = null
        editRange.setText("")
        level = null
        editLevel.setText("")
        convocatory = null
        editConvocatory.setText("")
        editOPEC.setText("")
        editLowerLimit.setText("")
        editUpperLimit.setText("")
        editKeyWord.requestFocus()
    }

    /**
     * Cuando se da tap al botón buscar se obtiene un filtro y retorna a la pantalla de
     * ofertas laborales
     */
    fun onSearch(button: View) {
        cleanErrors()
        val validate = validateForm()
        if (validate) {
            val filter = getFilterFromForm()
            returnToBack(filter)
        }

    }

    private fun validateForm(): Boolean {
        var validate = true
        if (!getFilterFromForm()?.lowerLimitSR?.isNullOrBlank()!! && !getFilterFromForm()?.lowerLimitSR?.isNullOrBlank()!!){
            if (getFilterFromForm()?.lowerLimitSR?.toLong()!!>getFilterFromForm()?.upperLimitSR?.toLong()!!){
                editLowerLimit?.error = getString(R.string.minValue)
                editUpperLimit?.error = getString(R.string.maxValueErr)
                validate = false
            }
        }
        if (!getFilterFromForm()?.upperLimitSR?.isNullOrBlank()!!){
            if (getFilterFromForm()?.upperLimitSR?.toLong()!! >"15000000".toLong()){
                editUpperLimit?.error = getString(R.string.maxValue)
                validate = false
            }
        }
        if (!getFilterFromForm()?.lowerLimitSR?.isNullOrBlank()!!){
            if (getFilterFromForm()?.lowerLimitSR?.toLong()!! >"15000000".toLong()){
                editLowerLimit?.error = getString(R.string.maxValue)
                validate = false
            }
        }
        return validate
    }

    private fun cleanErrors() {
        val count = linearForm?.childCount
        textInputAttachment?.error = null
        count?.let {
            for (position in 0..count) {
                val view = linearForm?.getChildAt(position)
                if (view is TextInputLayout) {
                    view.error = null
                }
            }
        }
    }
    /**
     * Regresa a la pantalla anterior y le envía un filtro
     */
    private fun returnToBack(filter: Filter) {
        val intent = Intent()
        intent.putExtra("filter", filter)
        setResult(RESULT_OK, intent)
        finish()
    }

    /**
     * Construye un objeto filtro desde los campos edittext de ésta pantalla
     */
    private fun getFilterFromForm(): Filter {
        val keyWord = editKeyWord.text.toString()
        val opec = editOPEC.text.toString()
        val limiteInferior : String = editLowerLimit.text.toString()
        val limiteSuperior : String = editUpperLimit.text.toString()
        return Filter(keyWord, entity, department, city, convocatory, range, limiteInferior, limiteSuperior, level, opec)
    }

    /**
     * Pone el menú en la parte superior derecha de la pantalla
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.clean_up, menu)
        return true
    }

    /**
     * Evento cuando se da tap sobre el menú de la parte superior derecha
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.menu_clean -> {
                clearFields()
            }
            else -> return super.onOptionsItemSelected(item)

        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResultSIMOResources(requestCode, resultCode, data)
    }

    /**
     * Cuando se vuelve de una pantalla de listado de recursos 'Entidades, Departamnetos, Ciudades', se ejecuta este método
     * @param requestCode codigo del tipo de recurso
     * @param resultCode codigo que indica si la solicitud fue exitosa o no
     * @param data datos que fueron retornados a esta pantalla
     */
    private fun onActivityResultSIMOResources(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_ENTITY) {
                entity = data?.getParcelableExtra("item")
                editTextEntity.setText(entity?.toString())
            } else if (requestCode == REQUEST_CODE_DEPARTMENT_OPEC) {
                department = data?.getParcelableExtra("item")
                editDepartment?.setText(department?.realName)
                if (department?.id != city?.department?.id) {
                    city = null
                    editCities?.setText("")
                }
            } else if (requestCode == REQUEST_CODE_CITY_OPEC) {
                city = data?.getParcelableExtra("item")
                editCities?.setText(city?.name)
            } else if (requestCode == REQUEST_CODE_SELECTIONPROCES) {
                convocatory = data?.getParcelableExtra("item")
                editConvocatory?.setText(convocatory?.name)
            } else if (requestCode == REQUEST_CODE_RANGE) {
                range = data?.getParcelableExtra("item")
                editRange.setText(range?.toString())
            } else if (requestCode == REQUEST_CODE_LEVEL) {
                level = data?.getParcelableExtra("item")
                editLevel?.setText(level?.toString())
            }
        }
    }

    /**
     * Ejecución al comenzar una activity
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenAdvancedSearch(this)
    }

    companion object {
        const val REQUEST_CODE_ENTITY = 0
        const val REQUEST_CODE_DEPARTMENT_OPEC = 1
        const val REQUEST_CODE_CITY_OPEC = 2
        const val REQUEST_CODE_SELECTIONPROCES = 3
        const val REQUEST_CODE_RANGE = 4
        const val REQUEST_CODE_LEVEL = 5
    }
}
