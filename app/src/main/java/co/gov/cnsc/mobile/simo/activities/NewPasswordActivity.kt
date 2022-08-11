package co.gov.cnsc.mobile.simo.activities

import android.os.Bundle
import android.view.View
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import kotlinx.android.synthetic.main.activity_new_password.*

/**
 * Contiene la funcionalidad para ingresar una nueva contraseña despues de hacer la verficiación
 * por email en la opción 'Olvidó su contraseña'
 */
class NewPasswordActivity : SIMOActivity() {

    /**
     * Nombre de usuario con el cuál se quiere reestablecer la contraseña
     */
    private var username: String? = null

    /**
     * Codigo de validación para reestablecer la contraseña
     */
    private var validationCode: String? = null

    /**
     * Validador del formulario de esta pantalla
     */
    private var mAwesomeValidation: AwesomeValidation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_password)
        showToolbarBack()
        setTextTitleToolbar(R.string.new_password)
        username = intent?.extras?.getString("username")
        validationCode = intent?.extras?.getString("verification_code")
        configureUI()
    }

    /**
     * Configura los elementos del formulario UI
     */
    private fun configureUI() {
        mAwesomeValidation = AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT)
        AwesomeValidation.disableAutoFocusOnFirstFailure()
        mAwesomeValidation?.addValidation(this, R.id.textInputNewPassword, SIMOApplication.REGEX_FOR_PASSWORD, R.string.password_requirements)
    }

    /**
     * Valida el formulario
     */
    private fun validateForm() {
        textInputNewPassword?.error = null
        val valid = mAwesomeValidation?.validate()
        if (valid == true) {
            changePassword()
        }
    }

    /**
     * Evento cuando se da tap sobre el botón Cambiar Contraseña
     */
    fun onChangePassword(button: View) {
        validateForm()
    }

    /**
     * Envía el codigo de verificación y la nueva contraseña al servidor
     * y así reestablecer la contraseña del usuario
     */
    private fun changePassword() {
        val newPassword = editNewPassword?.text.toString()
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.resetPassword(validationCode, newPassword, { json ->
            ProgressBarDialog.stopProgressDialog()
            SIMOApplication.showAlertDialog(this, R.string.password_modified,
                    R.string.your_key_change_has_been_success, R.string.continuee) { dialogInterface, i ->
                finish()
                goToLoginActivity(username, newPassword)
            }
        }, {
            ProgressBarDialog.stopProgressDialog()
        })
    }

    /**
     * Ejecución al comenzar la activity
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenNewPassword(this)
    }


}
