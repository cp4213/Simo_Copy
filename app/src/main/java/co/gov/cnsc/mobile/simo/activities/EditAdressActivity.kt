package co.gov.cnsc.mobile.simo.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.adapters.SIMOAutoCompleteTextAdapter
import co.gov.cnsc.mobile.simo.models.*
import co.gov.cnsc.mobile.simo.models.Filter
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.storage.SIMOResources
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import co.gov.cnsc.mobile.simo.views.TextInputLayout
import com.github.kittinunf.fuel.core.Request
import kotlinx.android.synthetic.main.activity_edit_adress.*
import kotlinx.android.synthetic.main.activity_edit_adress.buttonUpdate

class EditAdressActivity : SIMOActivity(), RadioGroup.OnCheckedChangeListener,
    AdapterView.OnItemSelectedListener{
    /**
     * Departamento de residencia
     */
    private var department: Department? = null
    private var user: User? = null
    var request: Request? = null
    private var filePhotoUploaded: co.gov.cnsc.mobile.simo.models.File? = null
    /**
     * Municipio de residencia
     */
    private var city: City? = null

    //Trae las opciones de tipo vía principal
    private val urbanViaType = SIMOResources.urbanVia
    private val ruralViaType = SIMOResources.ruralVia
    private val ruralViaTypeb = SIMOResources.ruralViab
    //Trae las opciones de cuadrante
    private val cuadrante= SIMOResources.cuadrante
    //Trae las opciones de letra
    private val letra = SIMOResources.letter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("DEV","EditAdressActivity")
        setContentView(R.layout.activity_edit_adress)
        if(true){//Validación de la dirección actualizada
            showToolbarBack()
        }
        setTextTitleToolbar(R.string.advanced_search)
        radioGroupZone?.setOnCheckedChangeListener(this)
        val bundle = intent?.extras
        configureUI(bundle?.getParcelable("filter"))

    }

    /**
     * Configura los elementos UI del layout
     * @param filter filtro aplicado en el listado OPEC
     */

    private fun configureUI(filter: Filter?) {
        //Listener de Deparamentos
        val username = SIMO.instance.session?.username
        request = RestAPI.getUser(username!!, { user ->
            this.user = user
        }, { fuelError ->
            SIMOApplication.showFuelError(this, fuelError)
        })
        textViewAdress.setText(intent?.getStringExtra("userAdress"))
        editTextDepartmentRes.setOnClickListener {
            SIMOApplication.goToSpinnerListView(
                activity = this, typeResource = SIMOAutoCompleteTextAdapter.TYPE_DEPARTMENTS_OPEC,
                query = editTextDepartmentRes.text.toString(), requestCode = AdvanceSearchActivity.REQUEST_CODE_DEPARTMENT_OPEC
            )
        }
    //Listener de Ciudades-Munic
        editTextCityRes?.setOnClickListener {
            if (department != null) {
                SIMOApplication.goToSpinnerListView(
                    activity = this, typeResource = SIMOAutoCompleteTextAdapter.TYPE_CITIES_OPEC,
                    query = editTextCityRes.text.toString(), requestCode = AdvanceSearchActivity.REQUEST_CODE_CITY_OPEC, idFilter = department?.id)
            }
        }
        var adapterUrbanType: ArrayAdapter<IdDescription> = ArrayAdapter<IdDescription>(this, android.R.layout.simple_spinner_dropdown_item)
        adapterUrbanType.addAll(urbanViaType)
        adapterUrbanType.isEnabled(0)


        //Se añade tipo vía urbana (se usa adapter para tener un listener)
        spinneStreetType.adapter=adapterUrbanType
        //Se añade el listener
        spinneStreetType.onItemSelectedListener =this
        //Se añade tipo vía Rural
        spinneRuralStreetType.items =ruralViaType
        spinnerSecundaria.items =ruralViaTypeb
        //Se añade letras de Sufijo

        spinneSecondaryStreetLeter.items =letra
        spinneSufixLeter.items =letra
        spinneMainStreetLetter.items =letra
        //Se añade Cuadrantes
        spinneCuadrante.items = cuadrante
        spinneSecondaryCuadrante.items = cuadrante

        checkSufix?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                spinneSufixLeter?.visibility=View.VISIBLE
            }else{
                spinneSufixLeter?.visibility=View.GONE
            }
        }
        buttonUpdate.setOnClickListener{

            cleanErrors()
            if(validateForm()){
                updateBasicInformation()
            }else{
                Toast.makeText(this,"Verifica la información registrada",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun cleanErrors() {
        val count = linearFormAdress?.childCount
        //textInputAttachment?.error = null
        count?.let {
            for (position in 0..count) {
                val view = linearFormAdress?.getChildAt(position)
                if (view is TextInputLayout) {
                    view.error = null
                }
            }
        }
    }
    //Valida los campos en el formulario al realizar guardado.
    private fun validateForm(): Boolean {
        var validate = true
        if(editTextDepartmentRes?.text.isNullOrBlank()){
            textInputDepartmentRes?.error=getString(R.string.required_field)
            validate =false
        }
        if(editTextCityRes?.text.isNullOrBlank()){
            textInputCityRes?.error=getString(R.string.required_field)
            validate =false
        }
        if(radioButtonUrbana.isChecked){
            when(spinneStreetType.selectedItemPosition){
                1,2,12-> {
                    if(editTextStreetName?.text.isNullOrBlank()){
                        textInputStreetName?.error=getString(R.string.required_field)
                        validate =false
                    }
                    if (!editTextS_StreetNumber.text.isNullOrBlank()){
                        if (editTextS_StreetNumber?.text?.length!! > 3 ){
                            textInputS_StreetNumber?.error=getString(R.string.err_oversise)
                            validate =false
                        }
                    }
                    if (!editText2SecondaryStreetNumber.text.isNullOrBlank()){
                        if (editText2SecondaryStreetNumber?.text?.length!! > 3 ){
                            textInput2SecondaryStreetNumber?.error=getString(R.string.err_oversise)
                            validate =false
                        }
                    }
                }
                else->{
                    if(editTextMainStreetNumber?.text.isNullOrBlank()){
                        textInputMainStreetNumber?.error=getString(R.string.required_field)
                        validate =false
                    }else{
                        if (editTextMainStreetNumber?.text?.length!! > 3 ){
                            textInputMainStreetNumber?.error=getString(R.string.err_oversise)
                            validate =false
                        }
                    }
                    if (editTextS_StreetNumber.text.isNullOrBlank()){
                        textInputS_StreetNumber?.error=getString(R.string.required_field)
                        validate =false
                    }else{
                        if (editTextS_StreetNumber?.text?.length!! > 3 ){
                            textInputS_StreetNumber?.error=getString(R.string.err_oversise)
                            validate =false
                        }
                    }
                    if (editText2SecondaryStreetNumber.text.isNullOrBlank()){
                        textInput2SecondaryStreetNumber?.error=getString(R.string.required_field)
                        validate =false
                    }else{
                        if (editText2SecondaryStreetNumber?.text?.length!! > 3 ){
                            textInput2SecondaryStreetNumber?.error=getString(R.string.err_oversise)
                            validate =false
                        }
                    }
                }
            }


        }
        if(radioButtonRural.isChecked){
            if(editTextRuralStreetName?.text.isNullOrBlank()){
                textInputRuralStreetName?.error=getString(R.string.required_field)
                validate =false
            }
        }
        return validate
    }

    //Construye la dirección a partir del formulario lleno
    private fun BuildAdress(): String {

        var adressgenerated =""

        if(radioButtonUrbana.isChecked){
            val urbType = spinneStreetType?.selectedItem as IdDescription?
            val mainLeter = spinneMainStreetLetter?.selectedItem as IdDescription?
            val sufixLeter = spinneSufixLeter?.selectedItem as IdDescription?
            val mainCuadranteType = spinneCuadrante?.selectedItem as IdDescription?
            val letersecundary = spinneSecondaryStreetLeter?.selectedItem as IdDescription?
            val secundaryCuadrante = spinneSecondaryCuadrante?.selectedItem as IdDescription?

            adressgenerated= urbType?.description.toString()+" "
            when(spinneStreetType.selectedItemPosition){
                1,2,12-> {
                    //Nombre de la vía
                    adressgenerated=adressgenerated+editTextStreetName.text+" "
                    //BIS
                    if(checkSufix.isChecked){
                        adressgenerated=adressgenerated+"BIS "
                        //Letra sufijo
                        if(!sufixLeter?.description.toString().isNullOrBlank()){
                            adressgenerated=adressgenerated+sufixLeter?.description.toString()+" "
                        }
                    }
                    //Cuadrante Main
                    if(!mainCuadranteType?.description.toString().isNullOrBlank()){
                        adressgenerated=adressgenerated+mainCuadranteType?.description.toString()+" "
                    }
                    //1° Número secundario
                    if(!editTextS_StreetNumber?.text.isNullOrBlank()){
                        adressgenerated=adressgenerated+" # "+editTextS_StreetNumber?.text+" "
                        //Letra asociada a vía secundaria
                        if (!letersecundary?.description.toString().isNullOrBlank()){
                            adressgenerated=adressgenerated+letersecundary?.description.toString()+" "
                        }
                        //2do número vía secundaria
                        if(!editText2SecondaryStreetNumber?.text.isNullOrBlank()){
                            adressgenerated=adressgenerated+" "+editText2SecondaryStreetNumber?.text+" "
                        }
                    }
                    //Cuadrante vía secundaria
                    if(!secundaryCuadrante?.description.toString().isNullOrBlank()){
                        adressgenerated=adressgenerated+secundaryCuadrante?.description.toString()+" "
                    }
                }
                else->{
                    //Número principal
                    adressgenerated=adressgenerated+editTextMainStreetNumber.text+" "
                    //LetraPrincipal
                    if(!mainLeter?.description.toString().isNullOrBlank()){
                        adressgenerated=adressgenerated+secundaryCuadrante?.description.toString()+" "
                    }
                    //BIS
                    if(checkSufix.isChecked){
                        adressgenerated=adressgenerated+"BIS "
                        //Letra Sufijo
                        if(!sufixLeter?.description.toString().isNullOrBlank()){
                            adressgenerated=adressgenerated+sufixLeter?.description.toString()+" "
                        }
                    }
                    //Cuadrante Main
                    if(!mainCuadranteType?.description.toString().isNullOrBlank()){
                        adressgenerated=adressgenerated+mainCuadranteType?.description.toString()+" "
                    }
                    //número vía secundaria
                    adressgenerated=adressgenerated+"# "+editTextS_StreetNumber?.text+" "
                    //Letra asociada vía secundaria
                    if (!letersecundary?.description.toString().isNullOrBlank()){
                        adressgenerated=adressgenerated+letersecundary?.description.toString()+" "
                    }
                    //Segundo número vía secundaria
                    adressgenerated=adressgenerated+editText2SecondaryStreetNumber?.text+" "
                    //Cuadrante vía secundaria
                    if(!secundaryCuadrante?.description.toString().isNullOrBlank()){
                        adressgenerated=adressgenerated+secundaryCuadrante?.description.toString()+" "
                    }
                }
            }
        }
        if(radioButtonRural.isChecked){
            val ruralType = spinneRuralStreetType?.selectedItem as IdDescription?
            val secRuralType = spinnerSecundaria?.selectedItem as IdDescription?
            adressgenerated= ruralType?.description.toString()+" "
            adressgenerated=adressgenerated+editTextRuralStreetName.text+" "
            if(!secRuralType?.description.toString().isNullOrBlank()){
                adressgenerated=adressgenerated+secRuralType?.description.toString()+" "
            }
        }
        //Complemento
        if(!editTextComplementaryAdress?.text.isNullOrBlank()){
            adressgenerated=adressgenerated+editTextComplementaryAdress?.text+" "
        }
        adressgenerated=adressgenerated+", "+editTextCityRes?.text+", "
        adressgenerated=adressgenerated+editTextDepartmentRes?.text+", "
        adressgenerated=adressgenerated+"CO"
        return adressgenerated
    }


//Función que se encarga de ser el lístener del radio Group que lanza las opciones de
    //si la residencia es urbana o rural

    override fun onCheckedChanged(group: RadioGroup?, Idradio: Int) {
        when (Idradio) {
            //Cuando se seleciona Zona urbana
            radioButtonUrbana?.id->{
                textInputDepartmentRes?.visibility=View.VISIBLE
                textInputCityRes?.visibility=View.VISIBLE
                textViewZone?.setText("Zona Urbana")
                textViewZone?.visibility=View.VISIBLE
                dividerZone?.visibility=View.VISIBLE
                textViewMainStreet?.visibility=View.VISIBLE
                textViewStreetType?.visibility=View.VISIBLE
                spinneStreetType?.visibility=View.VISIBLE
                textInputStreetName?.visibility=View.VISIBLE
                editTextS_StreetNumber?.visibility=View.VISIBLE
                textInputMainStreetNumber?.visibility=View.VISIBLE
                spinneMainStreetLetter?.visibility=View.VISIBLE
                checkSufix?.visibility=View.VISIBLE

                spinneCuadrante?.visibility=View.VISIBLE
                dividerGeneratedAdress?.visibility=View.VISIBLE
                textViewSecondaryStreet?.visibility=View.VISIBLE
                textInputS_StreetNumber?.visibility=View.VISIBLE
                spinneSecondaryStreetLeter?.visibility=View.VISIBLE
                textInput2SecondaryStreetNumber?.visibility=View.VISIBLE
                spinneSecondaryCuadrante?.visibility=View.VISIBLE
                textInputComplementaryAdress?.visibility=View.VISIBLE
                textViewGeneratedAdressLabel?.visibility=View.VISIBLE
                textViewGeneratedAdress?.visibility=View.VISIBLE
                buttonUpdate?.visibility=View.VISIBLE

                //Invisibles

                spinneRuralStreetType?.visibility=View.GONE
                textInputRuralStreetName?.visibility=View.GONE
                spinnerSecundaria?.visibility=View.GONE

                //Reinicio de texto
                editTextDepartmentRes.setText("")
                editTextCityRes?.setText("")
                editTextComplementaryAdress?.setText("")
            }
            //Cuando se seleciona Zona Rural
            radioButtonRural?.id->{
                //Invisibles
                textViewMainStreet?.visibility=View.GONE
                textViewStreetType?.visibility=View.GONE
                spinneStreetType?.visibility=View.GONE
                textInputStreetName?.visibility=View.GONE
                textInputMainStreetNumber?.visibility=View.GONE
                spinneMainStreetLetter?.visibility=View.GONE
                checkSufix?.visibility=View.GONE
                spinneSufixLeter?.visibility=View.GONE
                spinneCuadrante?.visibility=View.GONE //
                dividerGeneratedAdress?.visibility=View.GONE
                textViewSecondaryStreet?.visibility=View.GONE
               // editTextStreetName.setOnEditorActionListener(this)
                //editTextStreetName.setOnKeyListener(this)
                //
                editTextS_StreetNumber?.visibility=View.GONE //Da problemas
                //
                 spinneSecondaryStreetLeter?.visibility=View.GONE
                 textInput2SecondaryStreetNumber?.visibility=View.GONE
                 spinneSecondaryCuadrante?.visibility=View.GONE

                 textInputDepartmentRes?.visibility=View.VISIBLE
                 textInputCityRes?.visibility=View.VISIBLE
                 textViewZone?.setText("Zona Rural")
                 textViewZone?.visibility=View.VISIBLE
                 dividerZone?.visibility=View.VISIBLE

                 spinneRuralStreetType?.visibility=View.VISIBLE
                 textInputRuralStreetName?.visibility=View.VISIBLE
                 spinnerSecundaria?.visibility=View.VISIBLE

                 textInputComplementaryAdress?.visibility=View.VISIBLE
                 textViewGeneratedAdressLabel?.visibility=View.VISIBLE
                 textViewGeneratedAdress?.visibility=View.VISIBLE
                 buttonUpdate?.visibility=View.VISIBLE

                //Reinicio de texto
                editTextRuralStreetName.setText("")
                editTextDepartmentRes.setText("")
                editTextCityRes?.setText("")
                editTextComplementaryAdress?.setText("")
                 //edit?.visibility=View.GONE//AQUI

            }
        }
    }
    /**
     * Cuando se vuelve de una pantalla de listado de recursos 'Departamnetos, Ciudades', se ejecuta este método
     * @param requestCode codigo del tipo de recurso
     * @param resultCode codigo que indica si la solicitud fue exitosa o no
     * @param data datos que fueron retornados a esta pantalla
     */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResultSIMOResources(requestCode, resultCode, data)
    }

    private fun onActivityResultSIMOResources(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_DEPARTMENT_OPEC) {
                department = data?.getParcelableExtra("item")
                editTextDepartmentRes?.setText(department?.realName)
                if (department?.id != city?.department?.id) {
                    city = null
                    editTextCityRes?.setText("")
                }
            }
            else if (requestCode == REQUEST_CODE_CITY_OPEC) {
                city = data?.getParcelableExtra("item")
                editTextCityRes?.setText(city?.name)
            }
        }

    }


    companion object {
        const val REQUEST_CODE_DEPARTMENT_OPEC = 1
        const val REQUEST_CODE_CITY_OPEC = 2
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        if (parent?.id == spinneStreetType.id) {
           when(position){
               1,2,12-> {textInputStreetName?.visibility=View.VISIBLE
                   textInputMainStreetNumber?.visibility=View.GONE
                   spinneMainStreetLetter?.visibility=View.GONE
                   editTextMainStreetNumber.setText("")
               }
               else->{textInputStreetName?.visibility=View.GONE
                   textInputMainStreetNumber?.visibility=View.VISIBLE
                   spinneMainStreetLetter?.visibility=View.VISIBLE
                   editTextStreetName.setText("")
               }
           }
        }
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {
        Log.d("DEV","Hola mundo onNothingSelected")
    }

    private fun updateBasicInformation() {
        val adress=BuildAdress()
        textViewGeneratedAdress.setText(adress)
        var usersendEmail :Boolean= true
        if(user?.sendEmail ==null || user?.sendEmail ==false){
            usersendEmail = false
        }

        ProgressBarDialog.startProgressDialog(this as Activity)

        RestAPI.updateUser(idUser = user?.id, username = SIMO.instance.session?.username, idTypeDocument = user?.documentType?.id,
            idDocumentUser = user?.identifier, documentDni = user?.documentDni, stageIdDni = user?.documentDni?.documentOriginId, idPerson = null,
            expeditionDate = user!!.dateExpedition.toString(), names = user?.name, lastNames = user?.lastName, dateBirth = user!!.dateBirth.toString(),
            idCity = user?.cityBirth?.id, idDepartment = user?.cityBirth?.department?.id, postalCodeBirth = user?.zipCodeBirth,
            idCountryBirth = user?.countryBirth?.id, gender = user?.gender, address = adress, standarAdress="true",
            postalCodeRes = user?.zipCodeResident,
            idCountryRes = user?.countryResident?.id, email = user?.email, telephone = user?.telephone,
            idLevelEducation = user?.educationalLevel?.id, dateCreation = user?.dateCreation,
            documentPhoto = user?.docPhoto, stageIdPhoto = filePhotoUploaded?.stageId, sendEmail = usersendEmail,
            disabilities = user?.disabilities,
            success = { user ->
                ProgressBarDialog.stopProgressDialog()
                this.user = user
                this?.window?.decorView?.clearFocus()
                Toast.makeText(this, R.string.updated_data, Toast.LENGTH_LONG).show()
                val intent = Intent(this, MyCVActivity::class.java)
                startActivityForResult(intent, 1)
            },
            error = { fuelError ->
                ProgressBarDialog.stopProgressDialog()
                SIMOApplication.showFuelError(this, fuelError)
                this?.window?.decorView?.clearFocus()
            }
        )
    }
}