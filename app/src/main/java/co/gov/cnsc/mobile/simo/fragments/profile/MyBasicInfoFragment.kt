package co.gov.cnsc.mobile.simo.fragments.profile

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.activities.EditAdressActivity
import co.gov.cnsc.mobile.simo.adapters.SIMOAutoCompleteTextAdapter
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.databinding.FragmentMyBasicInfoBinding
import co.gov.cnsc.mobile.simo.databinding.FragmentMyExperienceBinding
import co.gov.cnsc.mobile.simo.extensions.isEmailSyntax
import co.gov.cnsc.mobile.simo.fragments.CVFragment
import co.gov.cnsc.mobile.simo.fragments.main.SearchFragment
import co.gov.cnsc.mobile.simo.models.*
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.storage.SIMOResources
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import co.gov.cnsc.mobile.simo.views.TextInputLayout
import co.gov.cnsc.mobile.simo.util.ExtrasUtils
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
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_my_basic_info.*
import org.json.JSONObject
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * Esta clase contiene la funcionalidad de la pantalla de Información Básica de Mi CV
 */
class MyBasicInfoFragment : CVFragment(), SwipeRefreshLayout.OnRefreshListener {

    /**
     * Información de usuario a mostrar
     */
    private var user: User? = null

    var utils = ExtrasUtils()

    /**
     * Archivo de foto seleccionado por el usuario
     */
    private var filePhoto: File? = null

    /**
     * Archivo de foto subido al servidor
     */
    private var filePhotoUploaded: co.gov.cnsc.mobile.simo.models.File? = null

    /**
     * Archivo de identificación seleccionado por el usuario
     */
    private var fileIdentification: File? = null

    /**
     * Archivo de identificación subido a servidor
     */
    private var fileIdentificationUploaded: co.gov.cnsc.mobile.simo.models.File? = null

    /**
     * Pais de nacimiento
     */
    private var country: Country? = null

    /**
     * Pais de residencia
     */
    private var countryRes: Country? = null

    /**
     * Departamento de nacimienti
     */
    private var department: Department? = null

    /**
     * Municipio de nacimiento
     */
    private var city: City? = null

    /**
     * Departamento de residencia
     */
    private var departmentRes: Department? = null

    /**
     * Municipio de residencia
     */
    private var cityRes: City? = null

    /**
     * Nivel educativo
     */
    private var educationalLevel: EducationalLevel? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_basic_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureUI()
        getBasicInformation()
    }

    /**
     * Al destruir todos los componentes gráficos de la vista
     */
    override fun onDestroyView() {
        super.onDestroyView()
        clearFindViewByIdCache()
    }

    /**
     * Configura todos los campos de texto y spinners
     */
    fun configureUI() {
        swipeRefresh?.setOnRefreshListener(this)

        editTextEmail?.isFocusableInTouchMode = true
        editDni?.isFocusableInTouchMode = false

        editDateExpedition?.isFocusableInTouchMode = false
        editDateExpedition?.setOnClickListener {
            //val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(requireActivity(), editDateExpedition, SIMO.FORMAT_DATE)
            val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(requireActivity(), editDateExpedition, SIMO.FORMAT_DATE)
            val maxExpDateCal = Calendar.getInstance()
            maxExpDateCal.add(Calendar.YEAR, -52)
            datePickerDialog.datePicker.minDate = maxExpDateCal.time.time
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().time.time
        }

        editDateBirth?.isFocusableInTouchMode = false
        editDateBirth?.setOnClickListener {
            val minAgeCal = Calendar.getInstance()
            val maxAgeCal = Calendar.getInstance()
            maxAgeCal.add(Calendar.YEAR, -70) //65
            minAgeCal.add(Calendar.YEAR, -18)
            //val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(requireActivity(), editDateBirth, SIMO.FORMAT_DATE)
            val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(requireActivity(), editDateBirth, SIMO.FORMAT_DATE)
            datePickerDialog.datePicker.minDate = maxAgeCal.time.time
            datePickerDialog.datePicker.maxDate = minAgeCal.time.time
        }

        editTextAttachment?.isFocusableInTouchMode = false

        editTextAttachment?.setOnClickListener {
            if (activity != null && user?.urlDni != null) {
                SIMOApplication.checkIfConnectedByData(requireActivity()) {
                    SIMOApplication.checkPermissionsAndDownloadFile(requireActivity(), user?.urlDni!!, user?.nameDni!!)
                }
            }
        }

        buttonUpdate?.setOnClickListener {
            tryUpdateBasicInformation()
        }

        containerPhoto?.setOnClickListener {
            onPhotoClick()
        }

        buttonUpload?.setOnClickListener {
            onUploadIdentification()
        }

        buttonEditAdress?.setOnClickListener {
            gotoeditAdress()
        }

        checkBornAbroad?.setOnCheckedChangeListener { _, isChecked ->
            val visibilityAbroad = if (isChecked) {
                View.VISIBLE
            } else {
                View.GONE
            }
            val visibilityLocal = if (isChecked) {
                View.GONE
            } else {
                View.VISIBLE
            }
            textInputCountry?.visibility = visibilityAbroad
            textInputPostalPlace?.visibility = visibilityAbroad
            textInputCityBirth?.visibility = visibilityLocal
            textInputDepartmentBirth?.visibility = visibilityLocal
        }
        checkResAbroad?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                textInputCountryRes?.visibility =View.VISIBLE
                textInputPostalPlaceRes?.visibility =View.VISIBLE
                textInputAddress?.visibility =View.GONE
                buttonEditAdress.visibility=View.GONE
            } else {
                buttonEditAdress.visibility=View.VISIBLE
                textInputAddress?.visibility =View.VISIBLE
                textInputCountryRes?.visibility =View.GONE
                textInputPostalPlaceRes?.visibility =View.GONE

            }
        }

        editTextCountry?.setOnClickListener {
            SIMOApplication.goToSpinnerListView(fragment = this,
                typeResource = SIMOAutoCompleteTextAdapter.TYPE_COUNTRIES,
                query = editTextCountry.text.toString(),
                requestCode = REQUEST_CODE_COUNTRY)
        }

        editTextCountryRes?.setOnClickListener {
            SIMOApplication.goToSpinnerListView(fragment = this,
                typeResource = SIMOAutoCompleteTextAdapter.TYPE_COUNTRIES,
                query = editTextCountryRes.text.toString(),
                requestCode = REQUEST_CODE_COUNTRY_RES)
        }

        editDepartmentBirth.setOnClickListener {
            SIMOApplication.goToSpinnerListView(fragment = this, typeResource = SIMOAutoCompleteTextAdapter.TYPE_DEPARTMENTS,
                query = editDepartmentBirth.text.toString(), requestCode = REQUEST_CODE_DEPARTMENT)
        }

        editCityBirth?.setOnClickListener {
            if (department != null) {
                SIMOApplication.goToSpinnerListView(fragment = this, typeResource = SIMOAutoCompleteTextAdapter.TYPE_CITIES,
                    query = editCityBirth.text.toString(), requestCode = REQUEST_CODE_CITY, idFilter = department?.id)
            }
        }

        editTextEducationalLevel?.setOnClickListener {
            SIMOApplication.goToSpinnerListView(fragment = this, typeResource = SIMOAutoCompleteTextAdapter.TYPE_EDUCATIONAL_LEVELS,
                query = editTextEducationalLevel?.text.toString(), requestCode = REQUEST_CODE_EDUCATIONAL_LEVEL)
        }

        editTextDisabilities?.setOnClickListener {
            openChooseDisabilities()
        }
    }

    /**
     * Abre un popup con las discapacidades para que el usuario pueda seleccionar alguna o varias
     */
    private fun openChooseDisabilities() {
        val builder = AlertDialog.Builder(requireActivity())
        val disabilities = Disability.disabilitiesArray
        val checkedItems = Disability.getCheckedDisabilities(Disability.disabilities, user?.disabilities)
        builder.setMultiChoiceItems(disabilities, checkedItems, object : DialogInterface.OnMultiChoiceClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int, p2: Boolean) {

            }
        })
        builder.setCancelable(false)
        builder.setTitle(R.string.disability)
        builder.setPositiveButton("Continuar") { dialog, which ->
            val allDisabilities = Disability.disabilities
            val listDisabilities = mutableListOf<Disability>()
            disabilities.forEachIndexed { index, s ->
                val checked = checkedItems[index]
                if (checked) {
                    listDisabilities.add(allDisabilities[index])
                }
            }
            user?.disabilities = listDisabilities
            editTextDisabilities?.setText(user?.disabilitiesString)
        }
        builder.setNeutralButton("Cancelar") { dialog, which ->
            // Do something when click the neutral button
        }

        val dialog = builder.create()
        // Display the alert dialog on interface
        dialog.show()
    }

    /**
     * Esconde todos los mensajes de error de los campos
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
     * Valida el formulario con el fin de que los datos escritos sean sintácticamente válidos
     * para el sistema
     */
    private fun validateForm(): Boolean {
        cleanErrors()
        var validate = true
        if (editTextEmail?.text?.toString()?.isEmailSyntax == false) {
            textInputEmail?.error = getString(R.string.err_email)
            validate = false
        }
        if (editNames?.text?.matches(SIMOApplication.REGEX_FOR_NAMES.toRegex()) == false) {
            textInputName?.error = getString(R.string.names_requirements)
            validate = false
        }
        if (editLastNames?.text?.matches(SIMOApplication.REGEX_FOR_LASTNAMES.toRegex()) == false) {
            textInputLastNames?.error = getString(R.string.lastnames_requirements)
            validate = false
        }
        if (editDni?.text?.matches(SIMOApplication.REGEX_FOR_NUMBER_ID.toRegex()) == false) {
            textInputDni?.error = getString(R.string.number_id_requirements)
            validate = false
        }
        if (editDateExpedition?.text.isNullOrBlank()) {
            textInputDateExpedition?.error = getString(R.string.empty_field)
            validate = false
        }

        if (editDateBirth?.text.isNullOrBlank()) {
            textInputDateBirth?.error = getString(R.string.empty_field)
            validate = false
        }
        else {

            if (editDateExpedition.text.toString() <= editDateBirth.text.toString()) {
                textInputDateExpedition?.error = getString(R.string.lower_date)
                validate = false
            }
        }


        if (editAddress?.text.isNullOrBlank()) {
            textInputAddress?.error = getString(R.string.residential_address_requirements)
            validate = false
        }

        if (editPhone?.text?.matches(SIMOApplication.REGEX_FOR_PHONE_NUMBER.toRegex()) == false) {
            textInputPhone?.error = getString(R.string.phone_number_requirements)
            validate = false
        }

        if (checkBornAbroad?.isChecked == true) {
            if (editTextPostalPlace?.text?.matches(SIMOApplication.REGEX_FOR_POSTAL_CODE.toRegex()) == false) {
                textInputPostalPlace?.error = getString(R.string.postal_code_requirements)
                validate = false
            }
            if (country == null) {
                textInputCountry?.error = getString(R.string.select_value_required)
                validate = false
            }
        } else {
            val departmentBirth = department
            val cityBirth = SIMOResources.findCity(context, editCityBirth?.text.toString())
            if (departmentBirth == null) {
                textInputDepartmentBirth?.error = getString(R.string.select_value_required)
                validate = false
            }
            if (cityBirth == null) {
                textInputCityBirth?.error = getString(R.string.select_value_required)
                validate = false
            }
        }

        if (checkResAbroad?.isChecked == true) {
            if (editTextPostalPlaceRes?.text?.matches(SIMOApplication.REGEX_FOR_POSTAL_CODE.toRegex()) == false) {
                textInputPostalPlaceRes?.error = getString(R.string.postal_code_requirements)
                validate = false
            }
            if (countryRes == null) {
                textInputCountryRes?.error = getString(R.string.select_value_required)
                validate = false
            }
        } /*else {
            val departmentRes = SIMOResources.findDepartment(context, editDepartmentRes?.text.toString())
            val cityRes = SIMOResources.findCity(context, editCityRes?.text.toString())
            if (departmentRes == null) {
                textInputDepartmentRes?.error = getString(R.string.select_value_required)
                validate = false
            }
            if (cityRes == null) {
                textInputCityRes?.error = getString(R.string.select_value_required)
                validate = false
            }
        }*/

        if (educationalLevel == null) {
            textInputEducationalLevel?.error = getString(R.string.select_value_required)
            validate = false
        }

        if (user?.documentDni == null && fileIdentificationUploaded == null)
        {
            textInputAttachment.error = getString(R.string.upload_document_required)
            validate = false
        }

        return validate
    }

    /**
     * Obtiene toda la información correspondiente al usuario logeado
     * a través de un web service
     */
    private fun getBasicInformation() {
        swipeRefresh?.isRefreshing = true
        buttonUpdate?.visibility = View.INVISIBLE
        val username = SIMO.instance.session?.username
        request = RestAPI.getUser(username!!, { user ->
            swipeRefresh?.isRefreshing = false
            buttonUpdate?.visibility = View.VISIBLE
            this.user = user
            paint()
        }, { fuelError ->
            swipeRefresh?.isRefreshing = false
            buttonUpdate?.visibility = View.VISIBLE
            SIMOApplication.showFuelError(activity, fuelError)
            activity?.finish()
        })
    }

    private fun gotoeditAdress(){
        val intent = Intent(context, EditAdressActivity::class.java)
        intent.putExtra("userAdress", user?.address)
        //intent.putExtra("position", position)
        startActivityForResult(intent, 1)
    }

    /**
     * Pinta la información del usuario en el formulario
     * para ser mostrada y manipulada
     */
    private fun paint() {

        editTextEmail?.setText(user?.email)
        editNames?.setText(user?.name)
        editLastNames?.setText(user?.lastName)
        editDni?.setText(user?.identifier)

        //Èsta es la fecha que se pinta al cargar Datos Básicos
        val editDateExpeditionFromSIMO = user!!.dateExpedition.toString()
        //editDateExpedition?.setText(user?.dateExpedition)
        editDateExpedition?.setText(editDateExpeditionFromSIMO)

        checkBornAbroad?.isChecked = user?.birthAbroad == true
        if (user?.birthAbroad == true) {
            country = user?.countryBirth
            editTextCountry?.setText(user?.countryBirth?.name)
            editTextPostalPlace?.setText(user?.zipCodeBirth)
        } else {
            city = user?.cityBirth
            editCityBirth?.setText(city?.toString())
            department = user?.cityBirth?.department
            editDepartmentBirth?.setText(department?.realName)
        }

        val editDateBirthFromSIMO = user!!.dateBirth.toString()
        //editDateBirth?.setText(user?.dateBirth)
        editDateBirth?.setText(editDateBirthFromSIMO)

        checkResAbroad?.isChecked = user?.residentAbroad == true
        if (user?.residentAbroad == true) {
            countryRes = user?.countryResident
            editTextCountryRes?.setText(user?.countryResident?.name)
            editTextPostalPlaceRes?.setText(user?.zipCodeResident)
        } else {
            // departmentRes = user?.cityResident?.department
            // cityRes = user?.cityResident
            // editCityRes?.setText(cityRes?.toString())
            //  editDepartmentRes?.setText(departmentRes?.realName)
        }

        //
        if(activity?.intent?.getStringExtra("generatedAdress").isNullOrBlank()){
            editAddress?.setText(user?.address)
        }else{
            editAddress?.setText(activity?.intent?.getStringExtra("generatedAdress"))
        }

        editPhone?.setText(user?.telephone)
        checkReceiveEmail?.isChecked = user?.sendEmail == true
        editTextAttachment?.setText(user?.documentDni?.name)

        val types = DocumentType.documentTypes
        spinnerDocumentType?.items = types
        val indexDocumentType = types.indexOfFirst { documentType -> documentType.toString() == user?.documentType?.toString() }
        spinnerDocumentType?.setSelection(indexDocumentType, true)

        val genders = Gender.genders
        spinnerGender?.items = genders
        val indexGender = genders.indexOfFirst { gender -> gender.id == user?.gender }
        spinnerGender?.setSelection(indexGender, true)

        val urlImage = user?.docPhoto?.urlFile
        imagePhotoUser?.setImageURI(urlImage)

        educationalLevel = user?.educationalLevel
        editTextEducationalLevel?.setText(user?.educationalLevel?.name)

        editTextDisabilities?.setText(user?.disabilitiesString)
    }

    /**
     * Pinta la información del usuario en el formulario
     * para ser mostrada y manipulada
     */
    private fun paintRNEC() {

        editTextEmail?.setText(user?.email)

        editNames?.isFocusableInTouchMode = false
        editNames?.setText(user?.name)

        editLastNames?.isFocusableInTouchMode = false
        editLastNames?.setText(user?.lastName)

        editDni?.isFocusableInTouchMode = false
        editDni?.setText(user?.identifier)

        //val editDateExpeditionFromSOMO = "13-02-1985"
        val editDateExpeditionFromSIMO = user!!.dateExpedition.toString()
        editDateExpedition?.isFocusableInTouchMode = false
        editDateExpedition?.setText(editDateExpeditionFromSIMO)

        checkBornAbroad?.isChecked = user?.birthAbroad == true
        if (user?.birthAbroad == true) {
            country = user?.countryBirth
            editTextCountry?.setText(user?.countryBirth?.name)
            editTextPostalPlace?.setText(user?.zipCodeBirth)
        } else {
            city = user?.cityBirth
            editCityBirth?.setText(city?.toString())
            department = user?.cityBirth?.department
            editDepartmentBirth?.setText(department?.realName)
        }

        val editDateBirthFromSIMO = user!!.dateBirth.toString()
        //editDateBirth?.setText(user?.dateBirth)
        editDateBirth?.isFocusableInTouchMode = false
        editDateBirth?.setText(editDateBirthFromSIMO)

        checkResAbroad?.isChecked = user?.residentAbroad == true
        if (user?.residentAbroad == true) {
            countryRes = user?.countryResident
            editTextCountryRes?.setText(user?.countryResident?.name)
            editTextPostalPlaceRes?.setText(user?.zipCodeResident)
        } else {
            //departmentRes = user?.cityResident?.department
            //cityRes = user?.cityResident
            // editCityRes?.setText(cityRes?.toString())
            // editDepartmentRes?.setText(departmentRes?.realName)
        }
        if(activity?.intent?.getStringExtra("generatedAdress").isNullOrBlank()){
            editAddress?.setText(user?.address)
        }else{
            editAddress?.setText(activity?.intent?.getStringExtra("generatedAdress"))
        }

        editPhone?.setText(user?.telephone)
        checkReceiveEmail?.isChecked = user?.sendEmail == true
        editTextAttachment?.setText(user?.documentDni?.name)

        val types = DocumentType.documentTypes
        spinnerDocumentType?.items = types
        val indexDocumentType = types.indexOfFirst { documentType -> documentType.toString() == user?.documentType?.toString() }
        spinnerDocumentType?.isFocusableInTouchMode = false
        spinnerDocumentType?.setSelection(indexDocumentType, true)

        val genders = Gender.genders
        spinnerGender?.items = genders
        val indexGender = genders.indexOfFirst { gender -> gender.id == user?.gender }
        spinnerGender?.isFocusableInTouchMode = false
        spinnerGender?.setSelection(indexGender, true)

        val urlImage = user?.docPhoto?.urlFile
        imagePhotoUser?.setImageURI(urlImage)

        educationalLevel = user?.educationalLevel
        editTextEducationalLevel?.setText(user?.educationalLevel?.name)
        editTextDisabilities?.setText(user?.disabilitiesString)
    }

    /**
     * Al darle swiperefresh la información del usuario se actualiza
     */
    override fun onRefresh() {
        getBasicInformation()
    }

    /**
     * Al finalizar la vista de información básica
     */
    override fun onDestroy() {
        super.onDestroy()
        //request?.cancel()
    }

    /**
     * Valida el formulario y hace un llamado al servidor para actualizar la información del usuario
     */
    private fun tryUpdateBasicInformation() {
        val validate = validateForm()
        if (validate) {
            updateBasicInformation()
        }
    }

    /**
     * Captura la información del formulario y la envía a través de un web service
     * para ser resgitrada en el servidor
     */
    private fun updateBasicInformation() {
        val idUser = user?.id
        val username = user?.username
        val docPhoto = user?.docPhoto
        val stageIdIdentification = fileIdentificationUploaded?.stageId
        val stageIdPhoto = filePhotoUploaded?.stageId
        val email = editTextEmail?.text?.toString()
        val names = editNames?.text?.toString()
        val lastNames = editLastNames?.text?.toString()
        val idTypeDocument = (spinnerDocumentType.selectedItem as DocumentType).id
        val TypeDocument = (spinnerDocumentType.selectedItem as DocumentType).description
        val idDocumentForm = editDni?.text?.toString()
        val docIdentification = user?.documentDni

        val documentExpeditionDateForm = editDateExpedition.text.toString()
        val documentExpeditionDateFormConvToRNEC : String = utils.convertDateFromSIMO(documentExpeditionDateForm).toString()

        var idCountryBirth: String? = null
        var postalCodeBirth: String? = null

        var idDepartmentBirth: String? = null
        var idCityBirth: String? = null

        if (checkBornAbroad.isChecked) {
            idCountryBirth = country?.id
            postalCodeBirth = editTextPostalPlace?.text.toString()
        } else {
            idDepartmentBirth = department?.id
            idCityBirth = city?.id
        }

        //val documentBirthDateForm = editDateBirth?.text?.toString()!!
        val documentBirthDateForm = editDateBirth.text.toString()
        val documentBirthDateFormConvToRNEC : String = utils.convertDateFromSIMO(documentBirthDateForm).toString()

        var idCountryRes: String? = null
        var postalCodeRes: String? = null

        //var idDepartmentRes: String? = null
        //var idCityRes: String? = null

        if (checkResAbroad.isChecked) {
            val countryRes = countryRes
            idCountryRes = countryRes?.id
            postalCodeRes = editTextPostalPlaceRes?.text.toString()
        } /*else {
            idDepartmentRes = departmentRes?.id
            idCityRes = cityRes?.id
        }*/

        val gender = (spinnerGender?.selectedItem as Gender).id
        val address = editAddress?.text.toString()

        val telephone = editPhone?.text.toString()

        val educationLevel = educationalLevel
        val idEducationalLevel = educationLevel?.id

        val dateCreation = user?.dateCreation
        val sendEmail = checkReceiveEmail.isChecked

        activity?.window?.decorView?.clearFocus()

        //RNEC logic from here...

        if (TypeDocument == "Cédula"){
            //Invocamos el RESTful FCD-RNEC y definimos las variables en las que se almacenarán los valores que devuelve el servicio

            ProgressBarDialog.startProgressDialog(activity as Activity)
            RestAPI.validateCitizenBasicDataAgainstFCD(idDocumentForm, documentExpeditionDateFormConvToRNEC, { json ->
                ProgressBarDialog.stopProgressDialog()

                //aquí guardo los datos requerido, obtenidos del objeto JSON que ha sido devuelto como respuesta
                val obj = JSONObject(json)
                val valorRNECok = obj.getString("ok")
                val valorRNECvalida = obj.getString("valida")
                val persona = obj.getString("persona")

                if (persona == "null"){
                    //Cuando el servicio retorna "persona = null", y los valores para "ok" y "valida" son los siguiente: ok = true y valida = false
                    customDialog("¡No se encontró el número de cédula!\n\nRevíselo e inténte nuevamente.", "Cerrar")
                } else {
                    //Cuando el servicio retorna "persona != null" y distintas combinaciones para "ok" y "valida"
                    val personaRNEC                       = obj.getJSONObject("persona");
                    var idPersonaRNEC                     = personaRNEC.getString("id");
                    val numeroDocumentoIdentificacionRNEC = personaRNEC.getString("numeroDocumentoIdentificacion");
                    val primerNombreRNEC                  = personaRNEC.getString("primerNombre");
                    val segundoNombreRNEC                 = personaRNEC.getString("segundoNombre");
                    val primerApellidoRNEC                = personaRNEC.getString("primerApellido");
                    val segundoApellidoRNEC               = personaRNEC.getString("segundoApellido");
                    val sexoRNEC                          = personaRNEC.getString("sexo");
                    var fechaNacimientoRNEC               = personaRNEC.getString("fechaNacimiento");
                    val fechaExpedicionRNEC               = personaRNEC.getString("fechaExpedicion");


                    if (valorRNECok.equals("true") && valorRNECvalida.equals("true")){
                        // Los datos de cédula y fecha de expedición se encuentran en FCD y RNEC
                        ProgressBarDialog.startProgressDialog(activity as Activity)

                        RestAPI.updateUser(idUser = idUser, username = username, idTypeDocument = idTypeDocument,
                            idDocumentUser = idDocumentForm, documentDni = docIdentification, stageIdDni = stageIdIdentification, idPerson = idPersonaRNEC,
                            expeditionDate = documentExpeditionDateForm, names = primerNombreRNEC + " " + segundoNombreRNEC,
                            lastNames = primerApellidoRNEC + " " + segundoApellidoRNEC, dateBirth = fechaNacimientoRNEC,
                            idCity = idCityBirth, idDepartment = idDepartmentBirth, postalCodeBirth = postalCodeBirth,
                            idCountryBirth = idCountryBirth, gender = gender, address = address, standarAdress="true",
                            postalCodeRes = postalCodeRes,
                            idCountryRes = idCountryRes, email = email, telephone = telephone,
                            idLevelEducation = idEducationalLevel, dateCreation = dateCreation,
                            documentPhoto = docPhoto, stageIdPhoto = stageIdPhoto, sendEmail = sendEmail,
                            disabilities = user?.disabilities,
                            success = { user ->
                                ProgressBarDialog.stopProgressDialog()
                                this.user = user
                                SIMO.instance.session?.imageUrl = user.urlPhoto
                                SIMO.instance.session?.name = user.name
                                paintRNEC()
                                Toast.makeText(context, R.string.updated_data, Toast.LENGTH_LONG).show()
                                activity?.window?.decorView?.clearFocus()
                            },
                            error = { fuelError ->
                                ProgressBarDialog.stopProgressDialog()
                                SIMOApplication.showFuelError(activity, fuelError)
                                activity?.window?.decorView?.clearFocus()
                            }
                        )
                        // end RestAPI
                        //..

                    } else if (valorRNECok.equals("true") && valorRNECvalida.equals("false"))  {
                        // Cuando la fecha de expedición no es válida en RNEC
                        customDialog("¡La fecha de expedición del documento de identidad no es correcta!\n\n Revísela e inténte nuevamente.", "Aceptar")

                    } else if (valorRNECok.equals("true") && valorRNECvalida.equals("null"))  {
                        // Cuando la cédula no se encuentra en RNEC
                        customDialog("¡Número de cédula " + numeroDocumentoIdentificacionRNEC + " no válido!", "Aceptar")

                    } else if (valorRNECok.equals("false") && valorRNECvalida.equals("true"))  {
                        // Cuando la fecha de expedición sí es valida en FCD (el ciudadano ya existe en SIMO) pero no se obtuvo respuesta de RNEC

                        //Start RestAPI
                        ProgressBarDialog.startProgressDialog(activity as Activity)

                        RestAPI.updateUser(idUser = idUser, username = username, idTypeDocument = idTypeDocument,
                            idDocumentUser = idDocumentForm, documentDni = docIdentification, stageIdDni = stageIdIdentification, idPerson = idPersonaRNEC,
                            expeditionDate = documentExpeditionDateForm, names = names, lastNames = lastNames, dateBirth = documentBirthDateForm,
                            idCity = idCityBirth, idDepartment = idDepartmentBirth, postalCodeBirth = postalCodeBirth,
                            idCountryBirth = idCountryBirth, gender = gender, address = address,standarAdress="true",
                            postalCodeRes = postalCodeRes,
                            idCountryRes = idCountryRes, email = email, telephone = telephone,
                            idLevelEducation = idEducationalLevel, dateCreation = dateCreation,
                            documentPhoto = docPhoto, stageIdPhoto = stageIdPhoto, sendEmail = sendEmail,
                            disabilities = user?.disabilities,
                            success = { user ->
                                ProgressBarDialog.stopProgressDialog()
                                this.user = user
                                SIMO.instance.session?.imageUrl = user.urlPhoto
                                SIMO.instance.session?.name = user.name
                                paintRNEC()
                                Toast.makeText(context, R.string.updated_data, Toast.LENGTH_LONG).show()
                                activity?.window?.decorView?.clearFocus()
                            },
                            error = { fuelError ->
                                ProgressBarDialog.stopProgressDialog()
                                SIMOApplication.showFuelError(activity, fuelError)
                                activity?.window?.decorView?.clearFocus()
                            }
                        )
                        // end RestAPI

                    } else if (valorRNECok.equals("false") && valorRNECvalida.equals("false"))  {
                        // Cuando la fecha de expedición no es valida en FCD (el ciudadano ya existe en SIMO) pero no se obtuvo respuesta de RNEC
                        customDialog("¡La fecha de expedición de su documento de identidad no es correcta!\n\n Revísela e inténte nuevamente.", "Aceptar")

                    } else if (valorRNECok.equals("false") && valorRNECvalida == "null")  {
                        // Cuando los datos de la persona no se encuentran en FCD (el ciudadano no existe en SIMO) y tampoco se obtuvo respuesta de RNEC
                        customDialog("¡No se pudo validar la información!\n\nInténtelo más tarde", "Aceptar")

                    } else {
                        customDialog("¡Algo anda mal!\n\nInténtelo más tarde.", "Cerrar")
                    }
                } //delete this line content (related to "persona" NULL validation)

            }, error = { fuelError ->
                ProgressBarDialog.stopProgressDialog()
                SIMOApplication.showFuelError(activity, fuelError)
                activity?.window?.decorView?.clearFocus()
            })

        } else {
            // Si el tipo de documento es "Tarjeta de Identidad" sigue el curso normal. SIMO valida internamente la existencia
            // previa del "Usuario" y/o de la "Cédula" (al invocar el servicio de enviar código de verificación).
            //Start RestAPI
            ProgressBarDialog.startProgressDialog(activity as Activity)

            RestAPI.updateUser(idUser = idUser, username = username, idTypeDocument = idTypeDocument,
                idDocumentUser = idDocumentForm, documentDni = docIdentification, stageIdDni = stageIdIdentification, idPerson = null,
                expeditionDate = documentExpeditionDateForm, names = names, lastNames = lastNames, dateBirth = documentBirthDateForm,
                idCity = idCityBirth, idDepartment = idDepartmentBirth, postalCodeBirth = postalCodeBirth,
                idCountryBirth = idCountryBirth, gender = gender, address = address,standarAdress="true",
                postalCodeRes = postalCodeRes,
                idCountryRes = idCountryRes, email = email, telephone = telephone,
                idLevelEducation = idEducationalLevel, dateCreation = dateCreation,
                documentPhoto = docPhoto, stageIdPhoto = stageIdPhoto, sendEmail = sendEmail,
                disabilities = user?.disabilities,
                success = { user ->
                    ProgressBarDialog.stopProgressDialog()
                    this.user = user
                    SIMO.instance.session?.imageUrl = user.urlPhoto
                    SIMO.instance.session?.name = user.name
                    paint()
                    Toast.makeText(context, R.string.updated_data, Toast.LENGTH_LONG).show()
                    activity?.window?.decorView?.clearFocus()
                },
                error = { fuelError ->
                    ProgressBarDialog.stopProgressDialog()
                    SIMOApplication.showFuelError(activity, fuelError)
                    activity?.window?.decorView?.clearFocus()
                }
            )
            // end RestAPI

        }
        // ...to here
    }

    /**
     * Dialog personalizado para mensajes a consideración
     */
    fun customDialog(Message: String, ButtonText: String){
        val builder = AlertDialog.Builder(activity as Activity)
        //builder.setTitle("_________________________________")
        builder.setTitle("")
        builder.setMessage(Message)
        builder.setCancelable(false)

        builder.setPositiveButton(ButtonText){dialog, which ->
            //Toast.makeText(applicationContext,"Ok, we change the app background.",Toast.LENGTH_SHORT).show()
            //pagarPrueba();
            dialog.dismiss()
        }

        /*builder.setNegativeButton("Cancelar"){dialog,which ->
            //Toast.makeText(applicationContext,"You are not agree.",Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }*/

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    /**
     * Revisa los permisos necesario
     * y abre la galería o la cámara del dispositivo para obtener una imagen y subirla al servidor
     */
    private fun onPhotoClick() {
        Dexter.withContext(activity)
            .withPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener, MultiplePermissionsListener {
                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                    Log.d("DEV", "onPermissionRationaleShouldBeShown")
                    token?.continuePermissionRequest()
                }

                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    Log.d("DEV", "onPermissionsChecked")
                    if (report?.areAllPermissionsGranted() == true) {
                        EasyImage.openChooserWithGallery(this@MyBasicInfoFragment, getString(R.string.select_origin_image), REQUEST_CODE_PHOTO)
                    } else {
                        Log.d("DEV", "onPermissionDenied")
                    }
                }

                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    Log.d("DEV", "onPermissionGranted")
                    EasyImage.openChooserWithGallery(this@MyBasicInfoFragment, getString(R.string.select_origin_image), REQUEST_CODE_PHOTO)
                }

                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                    Log.d("DEV", "onPermissionRationaleShouldBeShown")
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Log.d("DEV", "onPermissionDenied")
                }

            })
            .check()
    }

    /**
     * Sube el archivo de cédula seleccionado al servidor, revisando permisos necesarios para
     * seleccionarlo
     */
    private fun onUploadIdentification() {
        activity?.let {
            SIMOApplication.checkIfConnectedByData(requireActivity()) {
                Dexter.withContext(activity)
                    .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(object : PermissionListener, MultiplePermissionsListener {
                        override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                            Log.d(SIMOApplication.TAG, "onPermissionRationaleShouldBeShown")
                            token?.continuePermissionRequest()
                        }

                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            Log.d(SIMOApplication.TAG, "onPermissionsChecked")
                            if (report?.areAllPermissionsGranted() == true) {
                                SIMOApplication.FilePickerNew(resultLauncher)
                            } else {
                                Log.d(SIMOApplication.TAG, "onPermissionDenied")
                            }
                        }

                        override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                            Log.d(SIMOApplication.TAG, "onPermissionGranted")
                            SIMOApplication.FilePickerNew(resultLauncher)
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

    }

    private var filedoc: Uri?=null
    /**
     * Maneja la respuesta al seleccionar un archivo pdf del dispositivo
     */
    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),{ result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null ) {
                filedoc =result.data!!.data
                val parcelFileDescriptor =activity?.contentResolver?.openFileDescriptor(filedoc!!, "r", null)
                val inputStream = FileInputStream(parcelFileDescriptor!!.fileDescriptor)
                fileIdentification = File(activity?.cacheDir, activity?.contentResolver?.getFileName(filedoc!!))
                val outputStream = FileOutputStream(fileIdentification)
                inputStream.copyTo(outputStream)
                if (SIMOApplication.checkMaxFileSize(activity as AppCompatActivity, fileIdentification)) {
                    uploadDocumentDni()
                }
            }
        })

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResultEasyImage(requestCode, resultCode, data)
        onActivityResultSIMOResources(requestCode, resultCode, data)
    }

    /**
     * Respuesta de la cámara o galería del dispositivo retornando la imagen seleccionada
     * o tomada
     */
    private fun onActivityResultEasyImage(requestCode: Int, resultCode: Int, data: Intent?) {
        EasyImage.handleActivityResult(requestCode, resultCode, data, activity, object : DefaultCallback() {
            override fun onImagePickerError(e: Exception?, source: EasyImage.ImageSource?, type: Int) {
                e!!.printStackTrace()
            }

            override fun onImagePicked(imageFile: File, source: EasyImage.ImageSource, type: Int) {
                try {
                    filePhoto = SIMOApplication.compressImage(context!!, imageFile)
                    val imageUri = Uri.fromFile(filePhoto)
                    imagePhotoUser.setImageURI(imageUri)
                    uploadPhoto()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context, getString(R.string.error_to_compress_image_to_send), Toast.LENGTH_LONG).show()
                }

            }

            override fun onCanceled(source: EasyImage.ImageSource?, type: Int) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    val photoFile = EasyImage.lastlyTakenButCanceledPhoto(context)
                    photoFile?.delete()
                }
            }
        })
    }

    /**
     * Resultado de la pantalla de selección de documento PDF
     * retornando el archivo seleccionado
     */
    private fun onActivityResultDocuments(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_ID) {
            if (data != null && resultCode == RESULT_OK) {
                val files = data.getParcelableArrayListExtra<MediaFile>(FilePickerActivity.MEDIA_FILES)
                if (files!!.size > 0) {
                    val realFilePath = files[0].path
                    if (realFilePath != null) {
                        val pickedFile = File(realFilePath)
                        if (SIMOApplication.checkMaxFileSize(activity as AppCompatActivity, pickedFile)) {
                            fileIdentification = pickedFile
                            uploadDocumentDni()
                        }
                    }
                }
            }
        }
    }

    /**
     * Sube la imagen seleccionada por el usuario al servidor
     */
    private fun uploadPhoto() {
        filePhoto?.let {
            ProgressBarDialog.startProgressDialog(activity as Activity)
            RestAPI.uploadImage(filePhoto!!, { file ->
                ProgressBarDialog.stopProgressDialog()
                filePhotoUploaded = file
                filePhoto = null
                tryUpdateBasicInformation()
            }, { fuelError ->
                ProgressBarDialog.stopProgressDialog()
                SIMOApplication.showFuelError(activity, fuelError)
            })
        }
    }

    /**
     * Sube el documento de cédula seleccionado por el usuario al servidor
     */
    private fun uploadDocumentDni() {
        fileIdentification?.let {
            ProgressBarDialog.startProgressDialog(activity as Activity)
            RestAPI.uploadDniUser(fileIdentification!!, { file ->
                ProgressBarDialog.stopProgressDialog()
                fileIdentificationUploaded = file
                editTextAttachment?.setText(fileIdentification?.nameWithoutExtension)
                fileIdentification = null
            }, { fuelError ->
                ProgressBarDialog.stopProgressDialog()
                if (fuelError.exception.message != null) {
                    Toast.makeText(context, fuelError.exception.message, Toast.LENGTH_LONG).show()
                } else {
                    SIMOApplication.showFuelError(context, fuelError)
                }
            })
        }
    }

    /**
     * Respuesta de la pantalla de recursos de SIMO retornando el item seleccionado
     * Pais, Pais de residencia, Departamento, Municipio de nacimiento...
     */
    private fun onActivityResultSIMOResources(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_COUNTRY) {
                country = data?.getParcelableExtra("item")
                editTextCountry?.setText(country?.name)
            } else if (requestCode == REQUEST_CODE_COUNTRY_RES) {
                countryRes = data?.getParcelableExtra("item")
                editTextCountryRes?.setText(countryRes?.name)
            } else if (requestCode == REQUEST_CODE_DEPARTMENT) {
                department = data?.getParcelableExtra("item")
                editDepartmentBirth?.setText(department?.realName)
                if (department?.id != city?.department?.id) {
                    city = null
                    editCityBirth?.setText("")
                }
            } else if (requestCode == REQUEST_CODE_CITY) {
                city = data?.getParcelableExtra("item")
                editCityBirth?.setText(city?.name)
            }  else if (requestCode == REQUEST_CODE_EDUCATIONAL_LEVEL) {
                educationalLevel = data?.getParcelableExtra("item")
                editTextEducationalLevel?.setText(educationalLevel?.name)
            }
        }
    }

    /**
     * Al iniciar la pantalla de información básica
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenMyCVBasic(context)
    }


    companion object {

        const val REQUEST_CODE_PHOTO = 0
        const val REQUEST_CODE_ID = 1
        const val REQUEST_CODE_COUNTRY = 2
        const val REQUEST_CODE_COUNTRY_RES = 4
        const val REQUEST_CODE_DEPARTMENT = 5
        const val REQUEST_CODE_CITY = 6
        const val REQUEST_CODE_DEPARTMENT_RES = 7
        const val REQUEST_CODE_CITY_RES = 8
        const val REQUEST_CODE_EDUCATIONAL_LEVEL = 9


        /**
         * Usa este metodo para crear una nueva instancia de la vista
         * en este caso es un fragment
         */
        @JvmStatic
        fun newInstance() =
            MyBasicInfoFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}
