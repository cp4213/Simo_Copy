package co.gov.cnsc.mobile.simo.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import co.gov.cnsc.mobile.simo.BuildConfig
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.extensions.intentSendEmail
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import kotlinx.android.synthetic.main.activity_help_us_to_improve.*


/**
 * Esta clase contiene la funcionalidad de la pantalla 'Ayúdanos a Mejorar'
 */
class HelpUsToImproveActivity : SIMOActivity() {
    private var bul=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_us_to_improve)
        showToolbarBack()
        setTextTitleToolbar(R.string.help_us_to_improve)
    }

    /**
     * Valida los campos y abre la app de email para
     * enviar un email con el comentario del usuario
     */
    fun onSendComment(button: View) {
        inputLayoutComment?.error = null
        val validateRadio = radioGroup.checkedRadioButtonId
        if (editComment?.text.isNullOrBlank()) {
            inputLayoutComment?.error = getString(R.string.empty_field)
        } else if (validateRadio < 0) {
            Toast.makeText(this, R.string.please_select_option, Toast.LENGTH_LONG).show()
        } else {
            val radioButton = findViewById<RadioButton>(radioGroup.checkedRadioButtonId)
            this.intentSendEmail(BuildConfig.EMAIL,
                    radioButton.text.toString(), editComment.text.toString())
            bul=true
        }
    }

    /**
     * Ejecución al comenzar la activity
     */
    override fun onResume() {
        super.onResume()
        if (bul){
            SIMOApplication.showAlertDialog(this, R.string.confirm, R.string.mail_sended, R.string.capcontinuee,
                { dialogInterface, i ->
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            )
            bul=false
        }
        //SIMOApplication.showAlertDialog(this, R.string.start_download,
         //   "Mundo", R.string.accept_button_dialog, { dialogInterface, which ->
         //   })

    }

    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenHelpUsImprove(this)
    }
}
