package co.gov.cnsc.mobile.simo.activities

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import co.gov.cnsc.mobile.simo.BuildConfig
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.extensions.intentOpenUrl
import co.gov.cnsc.mobile.simo.models.DocumentType
import co.gov.cnsc.mobile.simo.models.User
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import co.gov.cnsc.mobile.simo.util.ExtrasUtils
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONObject
import java.util.*

/**
 * Contiene la funcionalidad de las 2 pantallas de registro
 */
class RegisterActivity : SIMOActivity() {

    /**
     * Validador del primer formulario de registro
     */
    private var mAwesomeValidationFirst: AwesomeValidation? = null

    /**
     * Validador del segundo formulario de registro
     */
    private var mAwesomeValidationSecond: AwesomeValidation? = null
    var user: User? = null

    var utils = ExtrasUtils()

    companion object {
        val ACTION_FIRST = "action_first"
        val ACTION_SECOND = "action_second"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        showToolbarBack()
        setTextTitleToolbar(R.string.register_simo)
        user = intent?.extras?.getParcelable("user")
        configureUI()
    }

    /**
     * Configuración adicional para los componentes gráficos del layout
     * Muestra la primera o el segundo formulario de registro
     */
    fun configureUI() {
        buttonLogin?.paintFlags = buttonLogin?.paintFlags!! or Paint.UNDERLINE_TEXT_FLAG
        val action = intent.action
        if (action == ACTION_FIRST) {
            textLabelHaveAlreadyAccount.visibility = View.GONE
            buttonLogin.visibility = View.GONE
            linearLayoutFirst.visibility = View.VISIBLE
            val types = DocumentType.documentTypes
            spinnerDocumentType.items = types

        } else {
            textLabelHaveAlreadyAccount.visibility = View.GONE
            buttonLogin.visibility = View.GONE
            textViewStep.visibility = View.VISIBLE
            linearLayoutSecond.visibility = View.VISIBLE
        }
        configureFirstFormValidation()
        configureSecondFormValidation()
    }

    /**
     * Configura las validaciones de la primera pantalla de registro
     */
    fun configureFirstFormValidation() {
        mAwesomeValidationFirst = AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT)
        AwesomeValidation.disableAutoFocusOnFirstFailure()
        mAwesomeValidationFirst?.addValidation(this, R.id.textInputEmail, android.util.Patterns.EMAIL_ADDRESS, R.string.err_email)
        mAwesomeValidationFirst?.addValidation(this, R.id.textInputUsername, SIMOApplication.REGEX_FOR_USERNAME, R.string.username_requirements)
        mAwesomeValidationFirst?.addValidation(this, R.id.textInputDni, SIMOApplication.REGEX_FOR_NUMBER_ID, R.string.number_id_requirements)
        mAwesomeValidationFirst?.addValidation(this, R.id.textInputDniExpeditionDate, SIMOApplication.REGEX_EMPTY_FIELD, R.string.empty_field)

        editDniExpeditionDate?.isFocusableInTouchMode = false
        editDniExpeditionDate?.setOnClickListener {
            val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(this, editDniExpeditionDate,"dd-MM-yyyy")
            val maxDateEnterCal = Calendar.getInstance()
            maxDateEnterCal.add(Calendar.YEAR, -52)
            datePickerDialog.datePicker.minDate = maxDateEnterCal.time.time
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().time.time
        }
    }

    /**
     * Configura las validaciones de la segunda pantalla de registro
     */
    private fun configureSecondFormValidation() {
        mAwesomeValidationSecond = AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT)
        AwesomeValidation.disableAutoFocusOnFirstFailure()
        mAwesomeValidationSecond?.addValidation(this, R.id.textInputName, SIMOApplication.REGEX_FOR_NAMES, R.string.names_requirements)
        mAwesomeValidationSecond?.addValidation(this, R.id.textInputLastNames, SIMOApplication.REGEX_FOR_LASTNAMES, R.string.lastnames_requirements)
        mAwesomeValidationSecond?.addValidation(this, R.id.textInputPassword, SIMOApplication.REGEX_FOR_PASSWORD, R.string.password_requirements)
    }

    /**
     * Evento al dar tap al botón Continuar de la primera pantalla de registro
     */
    fun onContinueFirst(button: View) {

        editDni?.error = null
        editDniExpeditionDate?.error = null
        editUsername?.error = null
        editTextEmail?.error = null

        val validate = mAwesomeValidationFirst?.validate()
        if (validate!!) {
            goToRegisterSendVerifyCode()
        }
    }


    /**
     * Llama al servicio API Rest para enviar la información básica del usuario y enviar un código de verificación al correo del usuario que quiere ser registrado en SIMO
     */
    private fun goToRegisterSendVerifyCode() {
        val documentType = (spinnerDocumentType.selectedItem as DocumentType)
        val cedulaForm = editDni.text.toString()
        val fechaExpedicionForm = editDniExpeditionDate.text.toString()
        val username = editUsername.text.toString()
        val email = editTextEmail.text.toString()
        val receiveEmail = checkReceiveEmail.isChecked


        if (documentType.description == "Cédula"){
            //Invocamos el servicio RESTful RNEC y definimos las variables en las que se almacenarán los valores que devuelve el servicio RESTful RNEC

            ProgressBarDialog.startProgressDialog(this)
            RestAPI.validateCitizenBasicDataAgainstFCD(cedulaForm, fechaExpedicionForm, { json ->
                ProgressBarDialog.stopProgressDialog()

                //aquí guardo los datos requeridos, obtenidos del objeto JSON que ha sido devuelto como respuesta
                val obj = JSONObject(json)
                val valorRNECok = obj.getString("ok")
                val valorRNECvalida = obj.getString("valida")
                //val persona = obj.getJSONObject("persona")
                val persona = obj.getString("persona")

                if (persona == "null"){
                    //Cuando el servicio retorna "persona = null", y los valores para "ok" y "valida" son los siguiente: ok = true y valida = false
                    customDialog("¡No se encontró el número de cédula!\n\nRevíselo e inténte nuevamente.", "Cerrar")
                } else {
                    //Cuando el servicio retorna "persona != null" y distintas combinaciones para "ok" y "valida"
                    val personaRNEC                       = obj.getJSONObject("persona");
                    var idPersonaRNEC                     = personaRNEC.getString("id");
                    val numeroDocumentoIdentificacionRNEC = personaRNEC.getString("numeroDocumentoIdentificacion");
                    var primerNombreRNEC                  = personaRNEC.getString("primerNombre");
                    var segundoNombreRNEC                 = personaRNEC.getString("segundoNombre");
                    var primerApellidoRNEC                = personaRNEC.getString("primerApellido");
                    var segundoApellidoRNEC               = personaRNEC.getString("segundoApellido");
                    var sexoRNEC                          = personaRNEC.getString("sexo");
                    var fechaNacimientoRNEC               = personaRNEC.getString("fechaNacimiento");
                    val fechaExpedicionRNEC               = personaRNEC.getString("fechaExpedicion");


                    if (valorRNECok.equals("true") && valorRNECvalida.equals("true")){
                        // Los datos de cédula y fecha de expedición se encuentran en FCD y RNEC
                        ProgressBarDialog.startProgressDialog(this)

                        val fechaExpeditionToSIMO : String = utils.convertDateFromRNEC(fechaExpedicionRNEC).toString()

                        val user = User(documentType = documentType, identifier = cedulaForm, dateExpedition = fechaExpeditionToSIMO, username = username, email = email, sendEmail = receiveEmail)

                        RestAPI.registerSendVerifyCode(user, { json ->
                            ProgressBarDialog.stopProgressDialog()
                            goToVerifyCode(user)
                        }, { fuelError ->
                            ProgressBarDialog.stopProgressDialog()
                            showFuelError(fuelError)
                        })

                    } else if (valorRNECok.equals("true") && valorRNECvalida.equals("false"))  {
                        // Cuando la fecha de expedición no es válida en RNEC
                        customDialog("¡La fecha de expedición del documento de identidad no es correcta!\n\n Revísela e inténte nuevamente.", "Aceptar")

                    } else if (valorRNECok.equals("true") && valorRNECvalida.equals("null"))  {
                        // Cuando la cédula no se encuentra en RNEC
                        customDialog("¡Número de cédula " + numeroDocumentoIdentificacionRNEC + " no válido!", "Aceptar")

                    } else if (valorRNECok.equals("false") && valorRNECvalida.equals("true"))  {
                        // Cuando la fecha de expedición sí es valida en FCD (el ciudadano ya existe en SIMO) pero no se obtuvo respuesta de RNEC
                        customDialog("¡Ud. ya tiene usuario registrado en SIMO!", "Aceptar")

                    } else if (valorRNECok.equals("false") && valorRNECvalida.equals("false"))  {
                        // Cuando la fecha de expedición no es valida en FCD (el ciudadano ya existe en SIMO) pero no se obtuvo respuesta de RNEC
                        customDialog("¡Ud. ya tiene usuario registrado en SIMO!", "Aceptar")

                    } else if (valorRNECok.equals("false") && valorRNECvalida == "null")  {
                        // Cuando los datos de la persona no se encuentran en FCD (el ciudadano no existe en SIMO) y tampoco se obtuvo respuesta de RNEC
                        //Aquí puede cambiar el mensaje y quizás dejar que el ciudadano se registre (validar con Germám)
                        customDialog("¡No se pudo validar la información!\n\nInténtelo más tarde.", "Aceptar")

                    } else {
                        customDialog("¡Algo anda mal!\n\nInténtelo más tarde.", "Cerrar")
                    }
                }

            }, {fuelError ->
                ProgressBarDialog.stopProgressDialog()
                showFuelError(fuelError)
            })

        } else {
            // Si el tipo de documento es "Tarjeta de Identidad" sigue el curso normal. SIMO valida internamente la existencia
            // previa del "Usuario" y/o de la "Cédula" (al invocar el servicio de enviar código de verificación).
            ProgressBarDialog.startProgressDialog(this)
            val user = User(documentType = documentType, identifier = cedulaForm, username = username, email = email, sendEmail = receiveEmail)
            RestAPI.registerSendVerifyCode(user, { json ->
                ProgressBarDialog.stopProgressDialog()
                goToVerifyCode(user)
            }, { fuelError ->
                ProgressBarDialog.stopProgressDialog()
                showFuelError(fuelError)
            })

        }
    }

    /**
     * Dialog personalizado para mensajes a consideración
     */
    fun customDialog(Message: String, ButtonText: String){
        val builder = AlertDialog.Builder(this)
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
     * Evento al dar tap al botón 'Registrar' de la segunda pantalla de Registro
     */
    fun onRegisterFinal(button: View) {
        editNames?.error = null
        editLastNames?.error = null
        editPassword?.error = null
        val validate = mAwesomeValidationSecond?.validate()
        if (validate!!) {
            registerUserFinal()
        }
    }

    /**
     * Llama al servicio API Rest para finalmente registrar al usuario en la plataforma
     */
    private fun registerUserFinal() {
        val names = editNames.text.toString()
        val lastnames = editLastNames.text.toString()
        val password = editPassword.text.toString()
        user?.name = names
        user?.lastName = lastnames
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.registerSendFinal(user!!, password, { _ ->
            ProgressBarDialog.stopProgressDialog()
            SIMOApplication.showAlertDialog(this, R.string.registered, R.string.your_account_en_simo,
                    R.string.ok) { _, _ ->
                goToStartActivity(StartActivity.ACTION_LOGIN, user?.username)
            }
        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
        })
    }

    /**
     * Abre la pantalla de enviar codigo de verificación del registro de nuevo usuario
     */
    private fun goToVerifyCode(user: User?) {
        val intent = Intent(this, VerifyEmailActivity::class.java)
        intent.action = VerifyEmailActivity.ACTION_REGISTER
        intent.putExtra("user", user)
        startActivity(intent)
    }


    /**
     * Evento cuando el usuario da tap en Términos y Condiciones
     */
    fun onTerms(button: View) {
        goToLegalActivity()
    }

    /**
     * Evento cuando el usuario da tap en Políticas de Privacidad
     */
    fun onPolicy(button: View) {
        this.intentOpenUrl(BuildConfig.URL_POLICY_PRIVACY)
    }

    /**
     * Evento cuando el usuario da tap en la opción 'Ya tienes cuenta: Ingresar'
     */
    fun onLogin(button: View) {
        goToLoginActivity()
    }

    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenRegister(this)
    }
}
