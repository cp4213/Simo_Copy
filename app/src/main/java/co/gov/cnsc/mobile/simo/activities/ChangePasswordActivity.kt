package co.gov.cnsc.mobile.simo.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.models.SIMO
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import kotlinx.android.synthetic.main.activity_change_password.*


/**
 * Esta activity da la funcionalidad a la pantalla de 'Cambiar Contraseña'
 */
class ChangePasswordActivity : SIMOActivity() {

    var mAwesomeValidation: AwesomeValidation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        showToolbarBack()
        setTextTitleToolbar(R.string.change_password)
        settingValidationsForm()
    }

    /**
     * Configura la validación del formulario para esta pantalla
     */
    private fun settingValidationsForm() {
        mAwesomeValidation = AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT)
        AwesomeValidation.disableAutoFocusOnFirstFailure()
        mAwesomeValidation?.addValidation(this, R.id.textInputOldPassword, SIMOApplication.REGEX_EMPTY_FIELD, R.string.empty_field)
        mAwesomeValidation?.addValidation(this, R.id.textInputNewPassword, SIMOApplication.REGEX_EMPTY_FIELD, R.string.empty_field)
    }


    /**
     * Cuando se da tap sobre el botón 'Cambiar Contraseña'
     */
    fun onChangePassword(button: View) {
        val validate = mAwesomeValidation?.validate()


        if (validate!!) {

            if(editConfirmNewPassword.text.toString().equals(editNewPassword.text.toString())){
                changePassword()
               //editConfirmNewPassword
            }else{
            textInputConfirmNewPassword?.error= getString(co.gov.cnsc.mobile.simo.R.string.new_key_confirm_error)}
        }
    }


    /**
     * Captura los datos de los campos en pantalla y ejecuta el cambio de
     * contraseña en el servidor
     */
    private fun changePassword() {
        val username = SIMO.instance.session?.username
        val oldPassword = editOldPassword.text.toString()
        val newPassword = editNewPassword.text.toString()
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.changePassword(username!!, oldPassword,
                newPassword, { response ->
            ProgressBarDialog.stopProgressDialog()
            val message = response.obj().getString("mensajeRespuestaRestOk")
            SIMOApplication.showAlertDialog(this, R.string.password_modified,
                    R.string.your_password_change_was_success, R.string.continuee) { dialogInterface, i ->
                finish()
            }
        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
        })
    }

    /**
     * Ejecución al comenzar una activity
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenChangePassword(this)
        Log.i("Act","ChangePasswordA")
    }
}
