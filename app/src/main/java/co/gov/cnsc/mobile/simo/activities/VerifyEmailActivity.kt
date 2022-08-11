package co.gov.cnsc.mobile.simo.activities

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.models.User
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import kotlinx.android.synthetic.main.activity_verify_email.*

/**
 * Contiene la funcionalidad de la pantalla de Verificar Email tanto para registro como para
 * la opción de olvidó su contraseña
 */
class VerifyEmailActivity : SIMOActivity() {

    companion object {
        const val ACTION_REGISTER = "action_register"
        const val ACTION_FORGOT_PASSWORD = "action_forgot"
    }

    /**
     * Acción a ejecutarse en la pantalla
     */
    var action: String = ACTION_REGISTER

    /**
     * Validador de formulario
     */
    var mAwesomeValidation: AwesomeValidation? = null

    /**
     * Usuario a verificar
     */
    var user: User? = null

    /**
     * Username con el que el usuario se va a registrar
     */
    var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_email)
        showToolbarBack()
        setTextTitleToolbar(R.string.verify_code)
        action = intent.action.toString()
        configureUI()
    }


    /**
     * Configura la interfaz gráfica de la pantalla
     */
    fun configureUI() {
        textResendCode?.paintFlags = textResendCode?.paintFlags!! or Paint.UNDERLINE_TEXT_FLAG
        textResendCode?.setOnClickListener {
            reSendRegisterVerifyCode()
        }
        if (action == ACTION_REGISTER) {
            textViewStep.visibility = View.VISIBLE
            user = intent?.extras?.getParcelable("user")
        } else {
            textViewStep.visibility = View.GONE
            username = intent?.extras?.getString("username")
        }
        mAwesomeValidation = AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT)
        AwesomeValidation.disableAutoFocusOnFirstFailure()
        mAwesomeValidation?.addValidation(this, R.id.textInputCode, SIMOApplication.REGEX_EMPTY_FIELD, R.string.empty_field)
    }


    /**
     * Se ejecuta una validación cuando se da tap en el botón verificar
     */
    fun onVerify(button: View) {
        val validate = mAwesomeValidation?.validate()
        if (validate!!) {
            checkVerificationCode()
        }
    }


    /**
     * Realiza un rellamado al servicio para reenviar el código de verificación
     */
    private fun reSendRegisterVerifyCode() {
        user?.let {
            ProgressBarDialog.startProgressDialog(this)
            RestAPI.registerSendVerifyCode(user!!, { json ->
                ProgressBarDialog.stopProgressDialog()
                SIMOApplication.showAlertDialog(this, R.string.security_code,
                        R.string.security_has_been_resent, R.string.ok) { _, _ ->

                }
            }, { fuelError ->
                ProgressBarDialog.stopProgressDialog()
                showFuelError(fuelError)
            })
        }
    }

    /**
     * Realiza la verificación del codigo y así asegurarse que se puede continuar
     * con el proceso
     */
    private fun checkVerificationCode() {
        val code = editCode.text.toString()
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.validateVerificationCode(code, { result ->
            ProgressBarDialog.stopProgressDialog()
            if (action == ACTION_REGISTER) {
                user?.verificationCode = editCode.text.toString()
                goToRegisterActivity(RegisterActivity.ACTION_SECOND, user)
            } else {
                goToNewPassword(username)
            }
        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
        })
    }


    /**
     * Pasa a la pantalla de 'Nueva Contraseña'
     */
    private fun goToNewPassword(username: String?) {
        val code = editCode.text.toString()
        val intent = Intent(this, NewPasswordActivity::class.java)
        intent.putExtra("username", username)
        intent.putExtra("verification_code", code)
        startActivity(intent)
    }

    /**
     * Ejecución al comenzar una activity
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenVerifyEmail(this)
    }


}
