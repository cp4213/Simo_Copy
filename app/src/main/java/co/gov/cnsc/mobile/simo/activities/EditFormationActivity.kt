package co.gov.cnsc.mobile.simo.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.adapters.SIMOAutoCompleteTextAdapter
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.models.*
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.storage.SIMOResources
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import co.gov.cnsc.mobile.simo.views.TextInputLayout
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.model.MediaFile
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_advance_search.*
import kotlinx.android.synthetic.main.activity_edit_formation.*
import java.io.File
import java.util.*

/**
 * Esta clase contiene la funcionalidad para la pantalla de editar o agregar una formación
 * a la hoja de vida
 */
class EditFormationActivity : SIMOActivity(), AdapterView.OnItemSelectedListener {
    /**
     * Formación a mostrar en la pantalla
     */
    private var credential: Credential? = null
    /**
     * Posición de la formación en el listado de la pantalla anterior
     */
    private var position: Int? = null

    /**
     * Archivo de soporte seleccionado
     */
    private var fileFormation: File? = null


    /**
     * Objeto que hace referencia al archivo que se subió al servidor
     */
    private var fileFormationUploaded: co.gov.cnsc.mobile.simo.models.File? = null


    /**
     * Institución educativa seleccionada por el usuario
     */
    private var institution: Institution? = null


    /**
     * Programa educativo seleccionado por el usuario
     */
    private var program: Program? = null

    private var convProgram: Program? = null


    /**
     * Pais seleccionado por el usuario
     */
    private var country: Country? = null
    lateinit var adapter: ArrayAdapter <EducationalLevel>
    lateinit var adapterB: ArrayAdapter <String>
    lateinit var adapterC: ArrayAdapter <EducationalClass>
    lateinit var ed :List<EducationalClass>
    private var paint: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_formation)
        showToolbarBack()
        credential = intent?.extras?.getParcelable("credential")
        position = intent?.extras?.getInt("position", -1)
       // Log.d("DEV","Cred ->"+credential)
        if (credential == null) {
            setTextTitleToolbar(R.string.add_formation)
            buttonUpload?.setText(R.string.add)
            AnalyticsReporter.screenAddFormation(this)
        } else {
            setTextTitleToolbar(R.string.edit_formation)
            buttonUpload?.setText(R.string.update)
            AnalyticsReporter.screenEditFormation(this)
        }
        configureUI()
        getPeriodicity()
    }


    /**
     * Configura la interfaz gráfica de la pantalla: asigna eventos a los checkbox, y campos de textos
     */
    fun configureUI() {
        //Log.d("DEV",""+educationLevels)
        /*
        27/12/21
        //spinnerEducationalType?.items = educationLevels **Deshacer**
*/      val educationLevels = SIMOResources.filterEducationalLevels(this, "")
        //val edFormal =EducationalClass("1",true,"EDUCACION FORMAL")
        //val edTD =EducationalClass("2",true,"EDUCACION PARA EL TRABAJO Y DESARROLLO HUMANO")
        //val edInFormal =EducationalClass("3",true,"EDUCACION INFORMAL")
        val levs: List<EducationalLevel> =filtroLevel(educationLevels,0)
        adapter = ArrayAdapter<EducationalLevel>(this, android.R.layout.simple_spinner_dropdown_item)
        adapterB = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
        adapterC = ArrayAdapter<EducationalClass>(this, android.R.layout.simple_spinner_dropdown_item)
        ed = listOf(EducationalClass("1",true,"EDUCACION FORMAL"),EducationalClass("2",true,"EDUCACION INFORMAL"),EducationalClass("3",true,"EDUCACION PARA EL TRABAJO Y DESARROLLO HUMANO"))
        adapterC.addAll(ed)
        adapter.clear()
        adapter.addAll(levs)
        spinnerEducationalLevel.adapter = adapter//B
        spinnerEducationalLevel.setSelection(0)


        //adapterC.getPosition()
        //adapterC.getPosition(credential?.levelEducationLevel==adapterC?.)
        //adapter.addAll(tOfEduc)
        spinnerEducationalType.adapter = adapterC
        spinnerEducationalType.onItemSelectedListener =this
        spinnerEducationalLevel.onItemSelectedListener =this



        //Grupo 1 - Pregrado y Posgrado
        // •	Si “spinner NivelEducativo”, en nodo id = 4||5||6||7||8||9||10||13||14||15
        checkMarkConvalidatedTitle?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                textInputResolutionConvalidationNumber.visibility = View.VISIBLE
                textInputDateResolutionConvalidation.visibility = View.VISIBLE
                textInputequivalent_abroad_title.visibility = View.VISIBLE
                editDateResolutionConvalidation?.isFocusableInTouchMode = false
                editTextInstitution?.setOnClickListener {}
                editTextProgramm?.setOnClickListener {}
                editDateResolutionConvalidation?.setOnClickListener {
                    val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(this, editDateResolutionConvalidation, SIMO.FORMAT_DATE)
                    val maxDateGradeCal = Calendar.getInstance()
                    maxDateGradeCal.add(Calendar.YEAR, -60)
                    datePickerDialog.datePicker.minDate = maxDateGradeCal.time.time
                    datePickerDialog.datePicker.maxDate = Calendar.getInstance().time.time
                }
                editequivalent_abroad_title?.isFocusableInTouchMode = false

                editequivalent_abroad_title?.setOnClickListener {
                        SIMOApplication.goToSpinnerListView(
                            activity = this,
                            typeResource = SIMOAutoCompleteTextAdapter.TYPE_INSTITUTION_CONVALIDATE_PROGRAMM,
                            query = editequivalent_abroad_title?.text?.toString(), requestCode = REQUEST_CODE_CONV_PROGRAM,
                            idFilter = (spinnerEducationalLevel.selectedItem as EducationalLevel).id)
                }
            }else{
                textInputResolutionConvalidationNumber.visibility = View.GONE
                textInputDateResolutionConvalidation.visibility = View.GONE
                textInputequivalent_abroad_title.visibility = View.GONE
            }

        }

        checkAbroadTitle?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (checkMarkIsGraduated.isChecked){
                    if(spinnerEducationalType.selectedItem==1)
                    checkMarkConvalidatedTitle.visibility = View.VISIBLE
                }
                editTextInstitution.setText("")
                editTextProgramm.setText("")
                checkMarkOther?.visibility = View.GONE
                if(spinnerEducationalType.selectedItemPosition ==0)
                textInputCountry?.visibility = View.VISIBLE
                editTextInstitution?.isFocusableInTouchMode = true
                editTextInstitution?.setOnClickListener {}
                editTextProgramm.isFocusableInTouchMode = true
                editTextProgramm?.setOnClickListener { }
                institution = null
                program = null
            }
            else{
                checkMarkConvalidatedTitle.visibility = View.GONE
                if(spinnerEducationalType.selectedItemPosition ==0){
                checkMarkOther?.visibility = View.VISIBLE
                    editTextInstitution.setText("")
                    editTextProgramm.setText("")
                    if (spinnerEducationalLevel.selectedItemPosition >2){
                        editTextInstitution?.isFocusableInTouchMode = false
                        editTextInstitution?.setOnClickListener {
                            SIMOApplication.goToSpinnerListView(
                                activity = this,
                                typeResource = SIMOAutoCompleteTextAdapter.TYPE_EDUCATIONAL_INTITUTIONS,
                                query = editTextInstitution?.text?.toString(), requestCode = REQUEST_CODE_INSTITUTIONS)
                        }
                        editTextProgramm?.isFocusableInTouchMode = false
                        editTextProgramm?.setOnClickListener {
                            if (institution != null) {
                                SIMOApplication.goToSpinnerListView(
                                    activity = this,
                                    typeResource = SIMOAutoCompleteTextAdapter.TYPE_INSTITUTION_PROGRAMM,
                                    query = editTextInstitution?.text?.toString(), requestCode = REQUEST_CODE_PROGRAM,
                                    idFilter = institution?.id)
                            }
                        }
                    }
                }
                textInputCountry?.visibility = View.GONE

            }
        }
        checkMarkIsGraduated?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                spinnePeriodicity?.visibility = View.GONE
                textInputLevelReached?.visibility = View.GONE

                if(checkAbroadTitle.isChecked && spinnerEducationalType.selectedItemPosition==0){
                    checkMarkConvalidatedTitle.visibility = View.VISIBLE
                }
                if(spinnerEducationalType.selectedItemPosition ==0 ){
                    textInputDateGrade?.visibility = View.VISIBLE
                    textInputFinishDate?.visibility = View.GONE
                }   else {
                    textInputDateGrade?.visibility = View.GONE
                    textInputFinishDate?.visibility = View.VISIBLE
                    textInputDateGrade?.visibility = View.GONE
                    editFinishDate?.isFocusableInTouchMode = false
                    editFinishDate?.setOnClickListener {
                        val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(this, editFinishDate, SIMO.FORMAT_DATE)
                        val maxDateGradeCal = Calendar.getInstance()
                        maxDateGradeCal.add(Calendar.YEAR, -60)
                        datePickerDialog.datePicker.minDate = maxDateGradeCal.time.time
                        datePickerDialog.datePicker.maxDate = Calendar.getInstance().time.time
                    }
                }
            }
            else {
                checkMarkConvalidatedTitle.visibility = View.GONE
                textInputResolutionConvalidationNumber.visibility = View.GONE
                textInputDateResolutionConvalidation.visibility = View.GONE
                textInputequivalent_abroad_title.visibility = View.GONE
                textInputDateGrade?.visibility = View.GONE
                textInputFinishDate?.visibility = View.GONE
                if(spinnerEducationalType.selectedItemPosition ==0){
                spinnePeriodicity?.visibility = View.VISIBLE
                textInputLevelReached?.visibility = View.VISIBLE
                }
            }
        }

        editTextCountry?.setOnClickListener {
            SIMOApplication.goToSpinnerListView(
                activity = this,
                    typeResource = SIMOAutoCompleteTextAdapter.TYPE_COUNTRIES,
                    query = editTextCountry?.text?.toString(), requestCode = REQUEST_CODE_COUNTRY)
        }

        editTextInstitution?.isFocusableInTouchMode = false
        editTextInstitution?.setOnClickListener {
            SIMOApplication.goToSpinnerListView(
                activity = this,
                    typeResource = SIMOAutoCompleteTextAdapter.TYPE_EDUCATIONAL_INTITUTIONS,
                    query = editTextInstitution?.text?.toString(), requestCode = REQUEST_CODE_INSTITUTIONS)
        }

        editTextProgramm?.isFocusableInTouchMode = false
        editTextProgramm?.setOnClickListener {
            if (institution != null) {
                SIMOApplication.goToSpinnerListView(
                    activity = this,
                        typeResource = SIMOAutoCompleteTextAdapter.TYPE_INSTITUTION_PROGRAMM,
                        query = editTextInstitution?.text?.toString(), requestCode = REQUEST_CODE_PROGRAM,
                        idFilter = institution?.id)
            }
        }

        checkMarkOther?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                editTextInstitution.setText("")
                editTextProgramm.setText("")
                editTextInstitution.isFocusableInTouchMode = true
                editTextInstitution?.setOnClickListener {
                }
                editTextProgramm.isFocusableInTouchMode = true
                editTextProgramm?.setOnClickListener { }
                institution = null
                program = null
            } else {
                editTextInstitution?.isFocusableInTouchMode = false
                editTextInstitution?.setOnClickListener {
                    SIMOApplication.goToSpinnerListView(
                        activity = this,
                            typeResource = SIMOAutoCompleteTextAdapter.TYPE_EDUCATIONAL_INTITUTIONS,
                            query = editTextInstitution?.text?.toString(), requestCode = REQUEST_CODE_INSTITUTIONS)
                }

                editTextProgramm?.isFocusableInTouchMode = false
                editTextProgramm?.setOnClickListener {
                    if (institution != null) {
                        SIMOApplication.goToSpinnerListView(
                            activity = this,
                                typeResource = SIMOAutoCompleteTextAdapter.TYPE_INSTITUTION_PROGRAMM,
                                query = editTextInstitution?.text?.toString(), requestCode = REQUEST_CODE_PROGRAM,
                                idFilter = institution?.id)
                    }
                }
            }
        }

        editTextAttachment?.isFocusableInTouchMode = false
        editTextAttachment?.setOnClickListener {
            if (credential?.document != null) {
                SIMOApplication.checkIfConnectedByData(this) {
                    SIMOApplication.checkPermissionsAndDownloadFile(this, credential?.urlDocument!!,
                            credential?.nameDocument!!)
                }
            }
        }

        editDateGrade?.isFocusableInTouchMode = false
        editDateGrade?.setOnClickListener {
            val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(this, editDateGrade, SIMO.FORMAT_DATE)
            val maxDateGradeCal = Calendar.getInstance()
            maxDateGradeCal.add(Calendar.YEAR, -60)
            datePickerDialog.datePicker.minDate = maxDateGradeCal.time.time
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().time.time
        }



        /*//Grupo 2 - Básica
        //•	Si “spinner NivelEducativo”, en nodo id = 1||2||3

        checkAbroadTitle?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {

                textInputCountry?.visibility = View.VISIBLE
                checkMarkOther?.visibility = View.GONE
                spinnePeriodicity?.visibility = View.VISIBLE
                textInputLevelReached?.visibility = View.VISIBLE

            } else {

                textInputCountry?.visibility = View.GONE
                checkMarkOther?.visibility = View.GONE
                spinnePeriodicity?.visibility = View.VISIBLE
                textInputLevelReached?.visibility = View.VISIBLE

            }
        }

        checkMarkIsGraduated?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {

                checkMarkOther?.visibility = View.GONE
                spinnePeriodicity?.visibility = View.GONE
                textInputLevelReached?.visibility = View.GONE
                textInputDateGrade?.visibility = View.VISIBLE

            } else {

                checkMarkOther?.visibility = View.GONE
                spinnePeriodicity?.visibility = View.VISIBLE
                textInputLevelReached?.visibility = View.VISIBLE
                textInputDateGrade?.visibility = View.GONE

            }
        }

        editTextCountry?.setOnClickListener {
            SIMOApplication.goToSpinnerListView(activity = this,
                    typeResource = SIMOAutoCompleteTextAdapter.TYPE_COUNTRIES,
                    query = editTextCountry?.text?.toString(), requestCode = REQUEST_CODE_COUNTRY)
        }

        checkMarkOther?.visibility = View.GONE

        editTextInstitution?.isFocusableInTouchMode = true
        editTextInstitution?.setOnClickListener {}
        editTextProgramm.isFocusableInTouchMode = true
        editTextProgramm?.setOnClickListener { }
        institution = null
        program = null

        editDateGrade?.isFocusableInTouchMode = false
        editDateGrade?.setOnClickListener {
            val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(this, editDateGrade, SIMO.FORMAT_DATE)
            val maxDateGradeCal = Calendar.getInstance()
            maxDateGradeCal.add(Calendar.YEAR, -60)
            datePickerDialog.datePicker.minDate = maxDateGradeCal.time.time
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().time.time
        }

        editInitialDate?.isFocusableInTouchMode = false
        editInitialDate?.setOnClickListener {
            val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(this, editInitialDate, SIMO.FORMAT_DATE)
            val maxDateGradeCal = Calendar.getInstance()
            maxDateGradeCal.add(Calendar.YEAR, -60)
            datePickerDialog.datePicker.minDate = maxDateGradeCal.time.time
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().time.time
        }

        editFinishDate?.isFocusableInTouchMode = false
        editFinishDate?.setOnClickListener {
            val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(this, editFinishDate, SIMO.FORMAT_DATE)
            val maxDateGradeCal = Calendar.getInstance()
            maxDateGradeCal.add(Calendar.YEAR, -60)
            datePickerDialog.datePicker.minDate = maxDateGradeCal.time.time
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().time.time
        }

        editTextAttachment?.isFocusableInTouchMode = false
        editTextAttachment?.setOnClickListener {
            if (credential?.document != null) {
                SIMOApplication.checkIfConnectedByData(this) {
                    SIMOApplication.checkPermissionsAndDownloadFile(this, credential?.urlDocument!!,
                            credential?.nameDocument!!)
                }
            }
        }*/



        /*//Grupo 3 - Informal
        //•	Si “spinner NivelEducativo”, en nodo id = 11||12||16

        checkAbroadTitle?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {

                textInputCountry?.visibility = View.VISIBLE
                checkMarkOther?.visibility = View.GONE
                spinnePeriodicity?.visibility = View.GONE
                textInputLevelReached?.visibility = View.GONE
                textInputFinishDate?.visibility = View.GONE


            } else {

                textInputCountry?.visibility = View.GONE
                checkMarkOther?.visibility = View.GONE
                spinnePeriodicity?.visibility = View.GONE
                textInputLevelReached?.visibility = View.GONE
                textInputFinishDate?.visibility = View.VISIBLE

            }
        }

        checkMarkIsGraduated?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {

                checkMarkOther?.visibility = View.GONE
                spinnePeriodicity?.visibility = View.GONE
                textInputLevelReached?.visibility = View.GONE
                textInputDateGrade?.visibility = View.GONE
                textInputFinishDate?.visibility = View.VISIBLE

            } else {

                checkMarkOther?.visibility = View.GONE
                spinnePeriodicity?.visibility = View.GONE
                textInputLevelReached?.visibility = View.GONE
                textInputDateGrade?.visibility = View.GONE
                textInputFinishDate?.visibility = View.GONE

            }
        }

        editTextCountry?.setOnClickListener {
            SIMOApplication.goToSpinnerListView(activity = this,
                    typeResource = SIMOAutoCompleteTextAdapter.TYPE_COUNTRIES,
                    query = editTextCountry?.text?.toString(), requestCode = REQUEST_CODE_COUNTRY)
        }

        checkMarkOther?.visibility = View.GONE
        spinnePeriodicity?.visibility = View.GONE
        textInputLevelReached?.visibility = View.GONE
        textInputInitialDate?.visibility = View.VISIBLE
        //textInputFinishDate?.visibility = View.VISIBLE
        textInputHourlyIntensity?.visibility = View.VISIBLE

        editTextInstitution?.isFocusableInTouchMode = true
        editTextInstitution?.setOnClickListener {}
        editTextProgramm.isFocusableInTouchMode = true
        editTextProgramm?.setOnClickListener { }
        institution = null
        program = null

        editDateGrade?.isFocusableInTouchMode = false
        editDateGrade?.setOnClickListener {
            val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(this, editDateGrade, SIMO.FORMAT_DATE)
            val maxDateGradeCal = Calendar.getInstance()
            maxDateGradeCal.add(Calendar.YEAR, -60)
            datePickerDialog.datePicker.minDate = maxDateGradeCal.time.time
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().time.time
        }

        editInitialDate?.isFocusableInTouchMode = false
        editInitialDate?.setOnClickListener {
            val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(this, editInitialDate, SIMO.FORMAT_DATE)
            val maxDateGradeCal = Calendar.getInstance()
            maxDateGradeCal.add(Calendar.YEAR, -60)
            datePickerDialog.datePicker.minDate = maxDateGradeCal.time.time
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().time.time
        }

        editFinishDate?.isFocusableInTouchMode = false
        editFinishDate?.setOnClickListener {
            val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(this, editFinishDate, SIMO.FORMAT_DATE)
            val maxDateGradeCal = Calendar.getInstance()
            maxDateGradeCal.add(Calendar.YEAR, -60)
            datePickerDialog.datePicker.minDate = maxDateGradeCal.time.time
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().time.time
        }

        editTextAttachment?.isFocusableInTouchMode = false
        editTextAttachment?.setOnClickListener {
            if (credential?.document != null) {
                SIMOApplication.checkIfConnectedByData(this) {
                    SIMOApplication.checkPermissionsAndDownloadFile(this, credential?.urlDocument!!,
                            credential?.nameDocument!!)
                }
            }
        }*/


    }


    /**
     * Pinta la información de la formación en la pantalla en caso de la opción editar
     */
    fun paint() {
        paint =false
        val educationLevels = SIMOResources.filterEducationalLevels(this, "")
        var type: Int=0
        credential?.let {
            //Ubica las posicines de acuerdo al back
            for (posicion in ed.indices){
                if (ed.get(posicion)?.name== credential?.levelEducationLevel?.classE?.name){
                    spinnerEducationalType.setSelection(posicion)
                    type =posicion
                    val levs: List<EducationalLevel> =filtroLevel(educationLevels,posicion)
                    for (pos in levs.indices){
                        if(levs.get(pos).name ==credential?.levelEducationLevel?.name){
                            //val educationLevels = SIMOResources.filterEducationalLevels(this, "")
                            adapter.clear()
                            adapter.addAll(levs)
                            spinnerEducationalLevel.adapter = adapter//B
                            spinnerEducationalLevel.setSelection(pos)
                            Log.d("DEV","posicion---> "+posicion+" Pos ---> "+pos)
                        }
                    }
                }
            }

            if(type !=0){
                editInitialDate?.setText(credential?.dateStart)
                editHourlyIntensity?.setText(credential?.hourIntensity)
                if(credential?.graduated ==true){
                    editFinishDate?.setText(credential?.dateEnd)
                }
            }
            //spinnerEducationalType.setSelection(1)

            checkAbroadTitle?.isChecked = credential?.foreignTitle == true
            //Log.d("DEV","checkAbroadTitle---> "+credential?.foreignTitle)
            checkMarkOther?.isChecked = credential?.otherProgramInstitution == true
            checkMarkIsGraduated?.isChecked = credential?.graduated == true
            checkMarkConvalidatedTitle?.isChecked = credential?.convalidate ==true
            if(credential?.graduated == true){
                spinnePeriodicity?.visibility = View.GONE
                textInputLevelReached?.visibility = View.GONE
                editResolutionConvalidationNumber.setText(credential?.convalidateResolutionNumber)
                editDateResolutionConvalidation?.isFocusableInTouchMode = false
                editDateResolutionConvalidation.setText(credential?.convalidateResolutionDate)
                editDateResolutionConvalidation?.setOnClickListener {
                    val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(this, editDateResolutionConvalidation, SIMO.FORMAT_DATE)
                    val maxDateGradeCal = Calendar.getInstance()
                    maxDateGradeCal.add(Calendar.YEAR, -60)
                    datePickerDialog.datePicker.minDate = maxDateGradeCal.time.time
                    datePickerDialog.datePicker.maxDate = Calendar.getInstance().time.time
                }
                if(credential?.convalidate == true){
                    editequivalent_abroad_title.setText(credential?.academicDiciplne?.name)
                    editTextInstitution?.setOnClickListener {}
                    editTextProgramm.setOnClickListener {}
                }
            }
            if( credential?.convalidate ==true){
                textInputResolutionConvalidationNumber.visibility = View.VISIBLE
                textInputDateResolutionConvalidation.visibility = View.VISIBLE
                textInputequivalent_abroad_title.visibility = View.VISIBLE
            }


            Log.d("DEV","Convalidate?---> "+credential?.convalidate)

            if (credential?.program != null) {

                institution = credential?.program?.institution
                editTextInstitution?.isFocusableInTouchMode = false
                editTextInstitution?.setOnClickListener {
                    SIMOApplication.goToSpinnerListView(
                        activity = this,
                            typeResource = SIMOAutoCompleteTextAdapter.TYPE_EDUCATIONAL_INTITUTIONS,
                            query = editTextInstitution?.text?.toString(), requestCode = REQUEST_CODE_INSTITUTIONS)
                }
                editTextInstitution?.setText(institution?.toString())

                program = credential?.program
                editTextProgramm?.isFocusableInTouchMode = false
                editTextProgramm?.setOnClickListener {
                    if (institution != null) {
                        SIMOApplication.goToSpinnerListView(
                            activity = this,
                                typeResource = SIMOAutoCompleteTextAdapter.TYPE_INSTITUTION_PROGRAMM,
                                query = editTextInstitution?.text?.toString(), requestCode = REQUEST_CODE_PROGRAM,
                                idFilter = institution?.id)
                    }
                }
                editTextProgramm?.setText(program?.toString())
            }
            else {
                editTextInstitution.isFocusableInTouchMode = true
                editTextInstitution?.setOnClickListener {
                }

                editTextProgramm.isFocusableInTouchMode = true
                editTextProgramm?.setOnClickListener { }

                editTextInstitution?.setText(credential?.entityEducationExt)
                editTextProgramm?.setText(credential?.programmExt)
                institution = null
                program = null
            }

            //periodicity
            if (credential?.periodicity != null) {
                spinnePeriodicity?.selectedItem = credential?.periodicity
            }

            // go on...
            editDateGrade?.setText(credential?.dateGrade)
            editLevelReached?.setText(credential?.levelReached)
            editTextAttachment?.setText(credential?.document?.name)

            country = credential?.country
            editTextCountry?.setText(country?.toString())
        }

        window?.decorView?.clearFocus()
    }


    /**
     * Obtiene el listado de tipos de periodicidad para ser mostrados en el formulario
     */
    private fun getPeriodicity() {
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.getPeriodicity({ periodicities ->
            ProgressBarDialog.stopProgressDialog()
            spinnePeriodicity.items = periodicities
            paint()
        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            SIMOApplication.showFuelError(this, fuelError)
            finish()
        })
    }


    /**
     * Pide confirmación en caso de que el usuario esté usando datos,
     * solicita permiso de acceso a los archivos,
     * y abre un explorador de archivos pdf
     */
    fun onUploadFile(button: View) {
        SIMOApplication.checkIfConnectedByData(this) {
            Dexter.withActivity(this)
                    .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(object : PermissionListener, MultiplePermissionsListener {
                        override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                            Log.d(SIMOApplication.TAG, "onPermissionRationaleShouldBeShown")
                            token?.continuePermissionRequest()
                        }

                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            Log.d(SIMOApplication.TAG, "onPermissionsChecked")
                            if (report?.areAllPermissionsGranted() == true) {
                                SIMOApplication.openChooserDocuments(this@EditFormationActivity, "application/pdf", REQUEST_CODE_ATTACHMENT)
                            } else {
                                Log.d(SIMOApplication.TAG, "onPermissionDenied")
                            }
                        }

                        override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                            Log.d(SIMOApplication.TAG, "onPermissionGranted")
                            SIMOApplication.openChooserDocuments(this@EditFormationActivity, "application/pdf", REQUEST_CODE_ATTACHMENT)
                        }

                        override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                            Log.d(SIMOApplication.TAG, "onPermissionRationaleShouldBeShown")
                        }

                        override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                            Log.d(SIMOApplication.TAG, "onPermissionDenied")
                        }

                    })
                    .check()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResultDocuments(requestCode, resultCode, data)
        onActivityResultSIMOResources(requestCode, resultCode, data)
    }


    /**
     * Maneja la respuesta al seleccionar un archivo pdf del dispositivo
     */
    private fun onActivityResultDocuments(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_ATTACHMENT) {
            if (data != null && resultCode == Activity.RESULT_OK) {
                val files = data.getParcelableArrayListExtra<MediaFile>(FilePickerActivity.MEDIA_FILES)
                if (files!!.size > 0) {
                    val realFilePath = files[0].uri.path
                    if (realFilePath != null) {
                        val pickedFile = File(realFilePath)
                        if (SIMOApplication.checkMaxFileSize(this, pickedFile)) {
                            fileFormation = pickedFile
                            uploadDocumentFormation()
                        }
                    }
                }
            }
        }
    }


    /**
     * Retorno del item seleccionado en algún listado de recursos SIMO
     * Instituciones, Programas, Paises
     */
    private fun onActivityResultSIMOResources(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_INSTITUTIONS) {
                institution = data?.getParcelableExtra("item")
                editTextInstitution?.setText(institution?.name)
                if (institution?.id != program?.institution?.id) {
                    program = null
                    editTextProgramm?.setText("")
                }
            } else if (requestCode == REQUEST_CODE_PROGRAM) {
                program = data?.getParcelableExtra("item")
                editTextProgramm?.setText(program?.toString())
            } else if (requestCode == REQUEST_CODE_COUNTRY) {
                country = data?.getParcelableExtra("item")
                editTextCountry?.setText(country?.toString())
            }
            else if (requestCode == REQUEST_CODE_CONV_PROGRAM) {
                convProgram = data?.getParcelableExtra("item")
                editequivalent_abroad_title?.setText(convProgram?.toString())
            }
        }
    }


    /**
     * Sube un archivo seleccionado al servidor
     */
    private fun uploadDocumentFormation() {
        editTextAttachment?.error = null
        fileFormation?.let {
            ProgressBarDialog.startProgressDialog(this)
            RestAPI.uploadFilePDF(fileFormation!!, { file ->
                ProgressBarDialog.stopProgressDialog()
                fileFormationUploaded = file
                editTextAttachment?.setText(fileFormation?.nameWithoutExtension)
                fileFormation = null
            }, { fuelError ->
                ProgressBarDialog.stopProgressDialog()
                if (fuelError.exception.message != null) {
                    Toast.makeText(this, fuelError.exception.message, Toast.LENGTH_LONG).show()
                } else {
                    SIMOApplication.showFuelError(this, fuelError)
                }
            })
        }
    }


    /**
     * Valida el formulario de la Formación y sube la información al servidor
     */
    fun onUploadData(button: View) {
        cleanErrors()
        val validate = validateForm()
        if (validate) {
            updateInformation()
        }
    }


    /**
     * Valida la información en el formulario
     */
    private fun validateForm(): Boolean {
        var validate = true
        if (checkAbroadTitle?.isChecked == true && checkAbroadTitle?.visibility == View.VISIBLE)
        {
            if (country == null && spinnerEducationalType.selectedItemPosition ==0) {
                textInputCountry?.error = getString(R.string.select_value_required)
                validate = false
            }

            if(checkMarkIsGraduated.isChecked && checkMarkConvalidatedTitle.isChecked){
                if(editResolutionConvalidationNumber?.text?.matches(SIMOApplication.REGEX_FOR_RESOLUTIONNUMBER.toRegex()) == false) {
                    textInputResolutionConvalidationNumber?.error = getString(R.string.convalidate_number_requirements)
                    validate = false
                }
                if (editDateResolutionConvalidation?.text.isNullOrBlank()) {
                    textInputDateResolutionConvalidation?.error = getString(R.string.equivalent_title_date_required)
                    validate = false
                }
                if(editequivalent_abroad_title?.text.isNullOrBlank()) {
                    textInputequivalent_abroad_title?.error = getString(R.string.equivalent_title_required)
                    validate = false
                }
            }

            if(spinnerEducationalType.selectedItemPosition ==0 ){
                if(spinnerEducationalLevel.selectedItemPosition >2 ){
                    if (editTextInstitution?.text?.matches(SIMOApplication.REGEX_FOR_PROGRAM_INSTITUTION.toRegex()) == false) {
                        textInputInstitution?.error = getString(R.string.program_institution_requirements)
                        validate = false
                    }
                    if (editTextProgramm?.text?.matches(SIMOApplication.REGEX_FOR_PROGRAM_INSTITUTION.toRegex()) == false) {
                        textInputProgramm?.error = getString(R.string.program_institution_requirements)
                        validate = false
                    }
                }else{
                    if (editTextInstitution?.text.isNullOrBlank()) {
                        textInputInstitution?.error = getString(R.string.program_institution_requirements)
                        validate = false
                    }
                    if (editTextProgramm?.text.isNullOrBlank()) {
                        textInputProgramm?.error = getString(R.string.program_institution_requirements)
                        validate = false
                    }
                }
            }
        }
        else if (checkMarkOther.isChecked && checkMarkOther?.visibility == View.VISIBLE)
        {
            if (editTextInstitution?.text?.matches(SIMOApplication.REGEX_FOR_PROGRAM_INSTITUTION.toRegex()) == false) {
                textInputInstitution?.error = getString(R.string.program_institution_requirements)
                validate = false
            }

        }
        else
        {
            if (editTextInstitution?.text.isNullOrBlank()) {
                textInputInstitution?.error = getString(R.string.program_institution_requirements)
                validate = false
            }
            if (editTextProgramm?.text.isNullOrBlank()) {
                textInputProgramm?.error = getString(R.string.program_institution_requirements)
                validate = false
            }
            if(spinnerEducationalType.selectedItemPosition ==0 ){
                if(spinnerEducationalLevel.selectedItemPosition >2 ){
                    if (institution == null) {
                        textInputInstitution.error = getString(R.string.select_value_required)
                        validate = false
                    }
                    if (program == null) {
                        textInputProgramm.error = getString(R.string.select_value_required)
                        validate = false
                    }
                }
            }
        }

        if (checkMarkIsGraduated?.isChecked == true)
        {
            if(spinnerEducationalType.selectedItemPosition ==0 ){
                    if (editDateGrade?.text.isNullOrBlank()) {

                        textInputDateGrade?.error = getString(R.string.select_value_required)
                        validate = false
                    }
            }
            else{
                if (editFinishDate?.text.isNullOrBlank()) {
                    textInputDateGrade?.error = getString(R.string.select_value_required)
                    validate = false
                }
            }
        }
        else
        {
            if(spinnerEducationalType.selectedItemPosition ==0){
                if (editLevelReached?.text?.matches(SIMOApplication.REGEX_FOR_REACHED_LEVEL.toRegex()) == false)
                {
                    textInputLevelReached?.error = getString(R.string.reached_level_requirements)
                    validate = false
                }
                if(Integer.parseInt(editLevelReached?.text.toString())>12||Integer.parseInt(editLevelReached?.text.toString())==0){
                    textInputLevelReached?.error = getString(R.string.max_reached_level)
                    validate = false
                }
            }
        }

        if (fileFormationUploaded == null && credential?.document == null)
        {
            textInputAttachment?.error = getString(R.string.upload_document_required)
            validate = false
        }

        if(spinnerEducationalType.selectedItemPosition !=0) {
            if (editInitialDate?.text.toString() == "") {
                textInputInitialDate?.error = getString(R.string.initial_date_required)
                validate = false
            }
            if (spinnerEducationalType.selectedItemPosition == 1) {
                if(editHourlyIntensity.text.toString() == ""){
                    validate = false
                    textInputHourlyIntensity?.error = getString(R.string.hourly_Intensity_Informal)
                }else{
                    if (editHourlyIntensity.text.toString()
                            .toInt() > 160 || editHourlyIntensity.text.toString().toInt() < 1

                    ) {
                        textInputHourlyIntensity?.error = getString(R.string.hourly_Intensity_Informal)
                        validate = false
                    }
                }
            }
            if (spinnerEducationalType.selectedItemPosition == 2) {
                if (spinnerEducationalLevel.selectedItemPosition == 0) {
                    if(editHourlyIntensity.text.toString() == ""){
                        validate = false
                        textInputHourlyIntensity?.error = getString(R.string.hourly_Intensity_FA)
                    }else
                    {
                        if (editHourlyIntensity.text.toString()
                                .toInt() <= 160 || editHourlyIntensity.text.toString()
                                .toInt() >= 1800
                        ) {
                            textInputHourlyIntensity?.error = getString(R.string.hourly_Intensity_FA)
                            validate = false
                        }
                    }
                }
                if (spinnerEducationalLevel.selectedItemPosition == 1) {
                    if(editHourlyIntensity.text.toString() == ""){
                        textInputHourlyIntensity?.error = getString(R.string.hourly_Intensity_FP)
                        validate = false
                    }
                    else{
                        if (editHourlyIntensity.text.toString()
                                .toInt() < 0 || editHourlyIntensity.text.toString()
                                .toInt() > 999999999) {
                            textInputHourlyIntensity?.error = getString(R.string.hourly_Intensity_FP)
                            validate = false
                        }
                    }
                }
                if (spinnerEducationalLevel.selectedItemPosition == 2) {
                    if(editHourlyIntensity.text.toString() == ""){
                        textInputHourlyIntensity?.error = getString(R.string.hourly_Intensity_FL)
                        validate = false
                    }else{
                        if (editHourlyIntensity.text.toString()
                                .toInt() <= 600 || editHourlyIntensity.text.toString().toInt() > 1800
                        ) {
                            textInputHourlyIntensity?.error = getString(R.string.hourly_Intensity_FL)
                            validate = false
                        }
                    }
                }
            }
        }
        Log.d("DEV",""+validate)
        return validate
    }


    /**
     * Esconde los labels de errores en cada uno de los campos de formulario
     */
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
     * Sube la información del formulario al servidor para actualizar un item formación o para crearlo
     */
    private fun updateInformation() {

        //vals
        val idUser = SIMO.instance.session?.idUser
        val username = SIMO.instance.session?.username
        val idCredential = credential?.id
        val levelEducational = spinnerEducationalLevel?.selectedItem as EducationalLevel?
        val typeEducational = spinnerEducationalType?.selectedItem as EducationalClass?
        val idLevelEducational = levelEducational?.id
        val idTypeEducational = typeEducational?.id
        val idClassEdcuational = levelEducational?.classE?.id

       // Log.d("DEV","idLevelEducational "+idLevelEducational+" idTypeEducational= "+idTypeEducational+" idClassEdcuational= "+idClassEdcuational)

        //vars
        var country: Country? = null
        var institutionExt: String? = null
        var programmExt: String? = null
        var institution: Institution? = null
        var program: Program? = null
        var convProgram: Program? =null
        var checkMarkOtherS: String? = null

        //if
        if (checkAbroadTitle?.isChecked == true) {
            country = this.country
            institutionExt = editTextInstitution?.text.toString()
            programmExt = editTextProgramm?.text.toString()
        }
        else if (checkMarkOther.isChecked && checkMarkOther.visibility == View.VISIBLE) {
            institutionExt = editTextInstitution?.text.toString()
            programmExt = editTextProgramm?.text.toString()
            checkMarkOtherS="true"
        }
        else if (spinnerEducationalType.selectedItemPosition ==0 && spinnerEducationalLevel.selectedItemPosition <3){
            institutionExt = editTextInstitution?.text.toString()
            programmExt = editTextProgramm?.text.toString()
        }
        else {
            institution = this.institution
            program = this.program
        }

        //vars
        var periodicity: IdName? = null
        var dateGrade: String? = null
        var dateIni: String? = null
        var dateFin: String? = null
        var levelReached: String? = null
        var hourInten: String? = null
        var graduado: String? = null
        var convalidate: String? =null
        var convalidateResolutionNumber: String? = null
        var convalidateResolutionDate: String? = null
        //if
        if (checkMarkIsGraduated?.isChecked == true) {
            graduado="true"
            if(spinnerEducationalType.selectedItemPosition !=0){
                dateFin =  editFinishDate.text.toString()
            }else{
                dateGrade = editDateGrade?.text.toString()
            }
            if (checkMarkConvalidatedTitle?.isChecked==true && checkMarkConvalidatedTitle?.visibility == View.VISIBLE){
                convalidate ="true"
                convalidateResolutionNumber =editResolutionConvalidationNumber.text.toString()
                convalidateResolutionDate= editDateResolutionConvalidation.text.toString()
                //programid=null
                convProgram= this.convProgram
            }
        }
        else {
            if(spinnerEducationalType.selectedItemPosition ==0) {
                periodicity = spinnePeriodicity?.selectedItem as IdName?
                levelReached = editLevelReached?.text.toString()
            }
        }

        if(spinnerEducationalType.selectedItemPosition !=0){
            dateIni =  editInitialDate?.text.toString()
            hourInten = editHourlyIntensity.text.toString()
            institutionExt = editTextInstitution?.text.toString()
            programmExt = editTextProgramm?.text.toString()
            if(editFinishDate.text.toString()!="") {
                dateFin =  editFinishDate.text.toString()
            }
        }
        Log.d("DEV","institutionext "+institutionExt)
        //ProgressBarDialog
        ProgressBarDialog.startProgressDialog(this)

        //RestAPI
        RestAPI.createOrUpdateFormation(idUser = idUser, username = username, idCredential = idCredential,
                idLevelEducation = idLevelEducational,
                idTypeEducation = idTypeEducational,
                idClassEducation = idClassEdcuational,
                idInstitution = institution?.id, idProgramm = program?.id,
                isAbroad = checkAbroadTitle.isChecked,
                isOther =checkMarkOtherS,
                idCountry = country?.id, institutionExt = institutionExt, programmExt = programmExt,
                idPeriodicity = periodicity?.id, levelReached = levelReached,
                graduado=graduado,
                dateGrade = dateGrade,
                dateIni= dateIni,dateFin=dateFin, hourInten=hourInten,
                convalidate=convalidate,convalidateResolutionNumber=convalidateResolutionNumber,
                convalidateResolutionDate=convalidateResolutionDate,
                academicDiciplineid=convProgram?.id,
                documentAttachment = credential?.document,
                stageIdAttachment = fileFormationUploaded?.stageId,
                success = { credential ->
                    ProgressBarDialog.stopProgressDialog()
                    var messageId: Int = R.string.formation_created_successfully
                    if (idCredential != null) {
                        messageId = R.string.formation_updated_successfully
                    }
                    Toast.makeText(this, messageId, Toast.LENGTH_LONG).show()
                    returnToBack(credential)
                },
                error = { fuelError ->
                    ProgressBarDialog.stopProgressDialog()
                    SIMOApplication.showFuelError(this, fuelError)
                    Log.d("DEV","Error Envio formación: "+fuelError)
                })

    }


    /**
     * Vuelve a la pantalla anterior envía la formación y la posición en el listado
     */
    private fun returnToBack(credential: Credential?) {
        val intent = Intent()
        intent.putExtra("credential", credential)
        intent.putExtra("position", position)
        setResult(RESULT_OK, intent)
        finish()
    }


    /**
     * Pone el menú de eliminar en la parte superior derecha de la pantalla
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (credential != null)
            menuInflater.inflate(R.menu.delete, menu)
        return true
    }


    /**
     * Evento cuando se da tap sobre la opción eliminar de la pantalla
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.menu_delete -> {
                SIMOApplication.showConfirmDialog(this, R.string.delete_formation, R.string.are_you_sure_delete_item_cv, R.string.yes,
                        { dialogInterface, i ->
                            deleteFormation()
                        }, R.string.no,
                        { dialogInterface, i ->

                        })
            }
            else -> return super.onOptionsItemSelected(item)

        }
        return true
    }


    /**
     * Llama a un servicio web para borrar dicho registro de formacion del servidor
     */
    private fun deleteFormation() {
        val idUser = SIMO.instance.session?.idUser
        val idCredential = credential?.id
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.deleteFormation(idUser, idCredential, { response ->
            ProgressBarDialog.stopProgressDialog()
            Toast.makeText(this, R.string.formation_deleted, Toast.LENGTH_LONG).show()
            returnToBack(credential)
        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            SIMOApplication.showFuelError(this, fuelError)
        })

    }


    companion object {
        const val REQUEST_CODE_ATTACHMENT = 0
        const val REQUEST_CODE_INSTITUTIONS = 1
        const val REQUEST_CODE_PROGRAM = 2
        const val REQUEST_CODE_COUNTRY = 3
        const val REQUEST_CODE_CONV_PROGRAM = 4

    }

    fun filtroLevel (lista: List<EducationalLevel>, position: Int): List<EducationalLevel> {

        var filtro : List<EducationalLevel> =lista.filterIndexed{ index,edlevel -> index >2 && index !=16}
        if(position==0){
            filtro = lista.filterIndexed{ index,edlevel -> index >2 && index !=16}
            }
        if(position==2){
            filtro = lista.filterIndexed{ index,edlevel -> index <3 &&  index >0 || index ==16}
        }
        if(position==1){
            filtro = lista.filterIndexed{ index,edlevel -> index ==0}
        }
        return filtro
    }

         /*
        Función encargada de realizar las configuraciones de los Spinners de selección de tipo de educación y nivel educativo
        CAP
        */

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
       if(paint==true) {
           val educationLevels = SIMOResources.filterEducationalLevels(this, "")
           // Inicio Spinner de tipo de educación
           if (p0?.id == spinnerEducationalType.id) {
               if (position == 0) {
                   adapter.clear()
                   adapter.addAll(filtroLevel(educationLevels, position))
                   spinnerEducationalLevel.adapter = adapter
                   if(checkAbroadTitle?.isChecked ==true) textInputCountry.visibility = View.VISIBLE
                   if (checkMarkIsGraduated?.isChecked == true) {
                       textInputDateGrade.visibility = View.VISIBLE
                   } else {
                       textInputLevelReached?.visibility = View.VISIBLE
                       spinnePeriodicity?.visibility = View.VISIBLE
                   }
                   if (checkMarkConvalidatedTitle.isChecked == false) {
                       checkMarkOther?.visibility = View.VISIBLE
                   }
                   textInputInitialDate?.visibility = View.GONE
                   textInputFinishDate?.visibility = View.GONE
                   textInputHourlyIntensity?.visibility = View.GONE
                   editTextInstitution?.isFocusableInTouchMode = false
                   editTextInstitution?.setOnClickListener {
                       SIMOApplication.goToSpinnerListView(
                           activity = this,
                           typeResource = SIMOAutoCompleteTextAdapter.TYPE_EDUCATIONAL_INTITUTIONS,
                           query = editTextInstitution?.text?.toString(),
                           requestCode = REQUEST_CODE_INSTITUTIONS
                       )
                   }
                   editTextProgramm.isFocusableInTouchMode = false
                   editTextProgramm?.setOnClickListener {
                       if (institution != null) {
                           SIMOApplication.goToSpinnerListView(
                               activity = this,
                               typeResource = SIMOAutoCompleteTextAdapter.TYPE_INSTITUTION_PROGRAMM,
                               query = editTextInstitution?.text?.toString(),
                               requestCode = REQUEST_CODE_PROGRAM,
                               idFilter = institution?.id
                           )
                       }
                   }

               }
               if (position == 1) {
                   //adapterB.clear()
                   //adapterB.addAll(listOf("EDUCACION INFORMAL"))
                   adapter.clear()
                   adapter.addAll(filtroLevel(educationLevels, position))
                   spinnerEducationalLevel.adapter = adapter//B

                   spinnePeriodicity?.visibility = View.GONE
                   textInputLevelReached?.visibility = View.GONE
                   checkMarkOther?.visibility = View.GONE
                   textInputDateGrade?.visibility = View.GONE
                   textInputCountry.visibility = View.GONE

                   if (checkMarkIsGraduated?.isChecked == true) {
                       textInputFinishDate.visibility = View.VISIBLE
                       editFinishDate?.setOnClickListener {
                           val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(
                               this,
                               editFinishDate,
                               SIMO.FORMAT_DATE
                           )
                           val maxDateGradeCal = Calendar.getInstance()
                           maxDateGradeCal.add(Calendar.YEAR, -60)
                           datePickerDialog.datePicker.minDate = maxDateGradeCal.time.time
                           datePickerDialog.datePicker.maxDate = Calendar.getInstance().time.time
                       }
                   }
                   editTextInstitution.isFocusableInTouchMode = true
                   editTextInstitution?.setOnClickListener { }//Retira la funcionalidad para dejar solo campo de texto
                   editTextProgramm.isFocusableInTouchMode = true
                   editTextProgramm?.setOnClickListener { }
                   //textInputDateInicialDate.visibility = View.VISIBLE
                   institution = null
                   program = null

                   textInputInitialDate?.visibility = View.VISIBLE
                   textInputHourlyIntensity?.visibility = View.VISIBLE

                   editInitialDate?.isFocusableInTouchMode = false
                   editInitialDate?.setOnClickListener {
                       val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(
                           this,
                           editInitialDate,
                           SIMO.FORMAT_DATE
                       )
                       val maxDateGradeCal = Calendar.getInstance()
                       maxDateGradeCal.add(Calendar.YEAR, -60)
                       datePickerDialog.datePicker.minDate = maxDateGradeCal.time.time
                       datePickerDialog.datePicker.maxDate = Calendar.getInstance().time.time
                   }
               }
               if (position == 2) {
                   adapter.clear()
                   adapter.addAll(filtroLevel(educationLevels, position))
                   spinnerEducationalLevel.adapter = adapter//B
                   spinnePeriodicity?.visibility = View.GONE
                   textInputLevelReached?.visibility = View.GONE
                   checkMarkOther?.visibility = View.GONE
                   textInputDateGrade?.visibility = View.GONE
                   textInputCountry.visibility = View.GONE
                   if (checkMarkIsGraduated?.isChecked == true) {
                       textInputFinishDate.visibility = View.VISIBLE
                       editFinishDate?.setOnClickListener {
                           val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(
                               this,
                               editFinishDate,
                               SIMO.FORMAT_DATE
                           )
                           val maxDateGradeCal = Calendar.getInstance()
                           maxDateGradeCal.add(Calendar.YEAR, -60)
                           datePickerDialog.datePicker.minDate = maxDateGradeCal.time.time
                           datePickerDialog.datePicker.maxDate = Calendar.getInstance().time.time
                       }
                   }
                   editTextInstitution.isFocusableInTouchMode = true
                   editTextInstitution?.setOnClickListener { }
                   editTextProgramm.isFocusableInTouchMode = true
                   editTextProgramm?.setOnClickListener { }
                   //textInputDateInicialDate.visibility = View.VISIBLE
                   institution = null
                   program = null

                   textInputInitialDate?.visibility = View.VISIBLE
                   textInputHourlyIntensity?.visibility = View.VISIBLE

                   editInitialDate?.isFocusableInTouchMode = false
                   editInitialDate?.setOnClickListener {
                       val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(
                           this,
                           editInitialDate,
                           SIMO.FORMAT_DATE
                       )
                       val maxDateGradeCal = Calendar.getInstance()
                       maxDateGradeCal.add(Calendar.YEAR, -60)
                       datePickerDialog.datePicker.minDate = maxDateGradeCal.time.time
                       datePickerDialog.datePicker.maxDate = Calendar.getInstance().time.time
                   }
               }
           }
           // FIN Spinner de tipo de educación
           // Inicio Spinner de Nivel de educación
           if (p0?.id == spinnerEducationalLevel.id) {
               if (spinnerEducationalType.selectedItemPosition == 0) {
                   if (spinnerEducationalLevel.selectedItemPosition <= 2) {
                       checkMarkOther?.visibility = View.GONE
                       editTextInstitution.isFocusableInTouchMode = true
                       editTextInstitution?.setOnClickListener { }
                       editTextProgramm.isFocusableInTouchMode = true
                       editTextProgramm?.setOnClickListener { }
                   } else {
                       if (checkMarkConvalidatedTitle.isChecked == false) {
                           checkMarkOther?.visibility = View.VISIBLE
                       }
                       editTextInstitution?.isFocusableInTouchMode = false
                       editTextInstitution?.setOnClickListener {
                           SIMOApplication.goToSpinnerListView(
                               activity = this,
                               typeResource = SIMOAutoCompleteTextAdapter.TYPE_EDUCATIONAL_INTITUTIONS,
                               query = editTextInstitution?.text?.toString(),
                               requestCode = REQUEST_CODE_INSTITUTIONS
                           )
                       }
                       editTextProgramm.isFocusableInTouchMode = false
                       editTextProgramm?.setOnClickListener {
                           if (institution != null) {
                               SIMOApplication.goToSpinnerListView(
                                   activity = this,
                                   typeResource = SIMOAutoCompleteTextAdapter.TYPE_INSTITUTION_PROGRAMM,
                                   query = editTextInstitution?.text?.toString(),
                                   requestCode = REQUEST_CODE_PROGRAM,
                                   idFilter = institution?.id
                               )
                           }
                       }
                   }
               }

           }
           // FIN Spinner de Nivel de educación
       }
        paint=true
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }
}


