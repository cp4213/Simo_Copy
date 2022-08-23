package co.gov.cnsc.mobile.simo.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.models.IdDescription
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.storage.SIMOResources
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import kotlinx.android.synthetic.main.activity_forgot_password.*
/**
 * Esta clase contiene la funcionalidad para recuperar la contraseña '¿Olvidó su clave?'
 * En caso de que el usuario la haya olvidado
 */
class ForgotPasswordActivity : SIMOActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        showToolbarBack()
        setTextTitleToolbar(R.string.did_you_forget_your_password)
        ConfigureUI()
    }

    private fun ConfigureUI() {
        //Añade el spinner de opciones de recuperación de contraseña
        var adapter= ArrayAdapter<IdDescription>(this, android.R.layout.simple_spinner_dropdown_item)
        adapter.addAll(SIMOResources.options)
        TipoSolicitud.adapter =adapter
    }

    /**
     * Cuando se da tap sobre el botón recuperar contraseña
     */
    fun onRemmember(button: View) {
        if (TipoSolicitud.selectedItemPosition !=0)
            validateForm()
    }

    /**
     * Valida los campos del formulario para continuar con el llamado al servidor
     */
    private fun validateForm() {
        textInputEmail.error = null
        if (editTextEmail?.text.toString().isNotBlank()) {
            sendVerifyCode()
        } else {
            textInputEmail?.error = getString(R.string.empty_field)
        }
    }

    /**
     * Envía el código de verificación y el nombre de usuario al servidor para
     * enviar un correo al usuario
     */
    private fun sendVerifyCode() {
        val username = editTextEmail?.text.toString()
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.forgotPassword((TipoSolicitud.selectedItem as IdDescription).id!!,username, { json ->
            ProgressBarDialog.stopProgressDialog()
            goToVerifyEmail(username)
        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
        })
    }

    /**
     * Va a la pantalla de verificar email
     */
    private fun goToVerifyEmail(username: String?) {
        val intent = Intent(this, VerifyEmailActivity::class.java)
        intent.putExtra("username", username)
        intent.action = VerifyEmailActivity.ACTION_FORGOT_PASSWORD
        startActivity(intent)
    }

    /**
     * Ejecución al comenzar una activity
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenForgotPassword(this)
        Log.i("Act","ForgotPasswordA")
    }
}
