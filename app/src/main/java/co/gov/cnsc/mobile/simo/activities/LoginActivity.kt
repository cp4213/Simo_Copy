package co.gov.cnsc.mobile.simo.activities

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import co.gov.cnsc.mobile.simo.BuildConfig
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.auth.AccountUtils
import co.gov.cnsc.mobile.simo.models.SIMO
import co.gov.cnsc.mobile.simo.models.User
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.github.kittinunf.fuel.core.FuelManager
import kotlinx.android.synthetic.main.activity_login.*


/**
 * Esta clase contiene la funcionalidad de la pantalla de Login
 */
class LoginActivity : SIMOActivity() {

    /**
     * Validador del formulario
     */
    var mAwesomeValidation: AwesomeValidation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        showToolbarBack()
        setTextTitleToolbar(R.string.login_SIMO)
        settingValidationsForm()
        if (BuildConfig.DEBUG) {
            //editUser.setText("MargaritaRosa")
            //editUser.setText("monigar_77")
            //editPassword.setText("9876543$*")
        }
        val usernameAuto = intent?.extras?.getString("username")
        val passwordAuto = intent?.extras?.getString("password")
        usernameAuto?.let {
            editUser.setText(usernameAuto)
        }
        passwordAuto?.let {
            editPassword.setText(passwordAuto)
        }
        if (usernameAuto != null && passwordAuto != null) {
            login()
        }
    }

    /**
     * Configura el validador del formulario
     */
    private fun settingValidationsForm() {
        buttonRegister?.paintFlags = buttonLogin?.paintFlags!! or Paint.UNDERLINE_TEXT_FLAG
        mAwesomeValidation = AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT)
        //mAwesomeValidation?.setTextInputLayoutErrorTextAppearance(R.style.TextInputLayoutError)
        AwesomeValidation.disableAutoFocusOnFirstFailure()
        mAwesomeValidation?.addValidation(this, R.id.textInputEmail, SIMOApplication.REGEX_EMPTY_FIELD, R.string.empty_field)
        mAwesomeValidation?.addValidation(this, R.id.textInputPassword, SIMOApplication.REGEX_EMPTY_FIELD, R.string.empty_field)
    }

    /**
     * Evento cuando se da tap sobre el botón Login
     */
    fun onLogin(button: View) {
        val validate = mAwesomeValidation?.validate()
        if (validate!!) {
            login()
        }
    }

    /**
     * Envia el usuario y la contraseña al servidor para que el usuario se logee
     * en el sistema
     */
    fun login() {
        val username = editUser.text.toString()
        val password = editPassword.text.toString()
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.login(username, password, { user ->
            ProgressBarDialog.stopProgressDialog()
            val cookie = FuelManager.instance.baseHeaders?.get("Cookie")
            val session = User.Session(user.id!!, user.username, user.name, cookie!!, user.urlPhoto)
            SIMO.instance.session = session
            AccountUtils.setAuthAccount(this, session, password)
            getUser()
        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
        })
    }

    /**
     * Obtiene toda la información correspondiente al usuario logeado
     */
    private fun getUser() {
        val session = SIMO.instance.session
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.getUser(session?.username!!, { user ->
            ProgressBarDialog.stopProgressDialog()
            session.imageUrl = user.urlPhoto
            finish()
            goToMainActivity(true)
        }) { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            SIMOApplication.showErrorConectionAlertToTry(this) {
                getUser()
            }
        }
    }

    /**
     * Abre la pantalla de Registro
     */
    fun onRegister(button: View) {
        goToRegisterActivity(RegisterActivity.ACTION_FIRST, null)
    }


    /**
     * Abre la pantalla de '¿Olvidaste la contraseña?' cuando se da tap sobre el link
     */
    fun onForgotPassword(button: View) {
        goToForgotPassword()
    }

    /**
     * Abre la pantalla de '¿Olvidaste la contraseña?'
     */
    private fun goToForgotPassword() {
        val intent = Intent(this, ForgotPasswordActivity::class.java)
        startActivity(intent)
    }

    /**
     * Ejecución al comenzar la activity
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenLogin(this)
    }
}
