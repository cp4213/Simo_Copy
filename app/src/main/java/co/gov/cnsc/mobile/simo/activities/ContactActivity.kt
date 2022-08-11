package co.gov.cnsc.mobile.simo.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import co.gov.cnsc.mobile.simo.BuildConfig
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.extensions.intentSendEmail
import kotlinx.android.synthetic.main.activity_contact.*
import android.util.Log
import androidx.fragment.app.FragmentActivity
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.extensions.intentOpenUrl


/**
 * Esta activity contiene la funcionalidad para la pantalla de 'Contacto'
 */
class ContactActivity : SIMOActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        showToolbarBack()
        setTextTitleToolbar(co.gov.cnsc.mobile.simo.R.string.contact)
        configureUI()
    }

    /**
     * Configura los elementos UI del layout y pinta la información en la pantalla
     */
    fun configureUI() {
        textViewAddress.text = getString(co.gov.cnsc.mobile.simo.R.string.address_simo, BuildConfig.ADDRESS)
        textViewPbx.text = getString(co.gov.cnsc.mobile.simo.R.string.pbx_simo, BuildConfig.PBX)
        textViewFax.text = getString(co.gov.cnsc.mobile.simo.R.string.fax_simo, BuildConfig.FAX)
        textViewTelephone.text = getString(co.gov.cnsc.mobile.simo.R.string.telephone_simo, BuildConfig.TELEPHONE)
        //textViewEmail.text = getString(R.string.email_simo, BuildConfig.EMAIL)
        textViewEmailNotifications.text = getString(co.gov.cnsc.mobile.simo.R.string.subtitulo_email_atencion, BuildConfig.EMAIL_ATENCION)
    }

    /**
     * Cuando se da tap sobre la dirección, abre la aplicación de mapas del dispositivo en la dirección deseada
     */
    fun onAddress(button: View) {
        val gmmIntentUri = Uri.parse("geo:0,0?q=1600 ${BuildConfig.ADDRESS}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    /**
     * Cuando se da tap sobre Pbx, abre la aplicación correspondiente para marcar el número
     */
    fun onPbx(button: View) {
        val phone = "${BuildConfig.PBX_FROM_SMARTPHONE}".trim().replace("(", "").replace(")", "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
        startActivity(intent)
    }

    /**
     * Cuando se da tap sobre el número telefónico, abre el dialer del dispositivo
     */
    /*fun onTelephone(button: View) {
        val phone = "${BuildConfig.TELEPHONE}".trim()
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
        startActivity(intent)
    }*/

    /**
     * Cuando se da tap sobre el correo eletrónico, abre la aplicación de correo electrónico
     */
    /*fun onEmail(button: View) {
    this.intentSendEmail(BuildConfig.EMAIL, getString(R.string.contact_simo_mobile), "")
    }*/

    /**
     * Cuando se da tap sobre el correo de Notificaciones Judiciales, abre la aplicación de correo electrónico
     */
    /*fun onEmailJudNotifications(button: View) {
        this.intentSendEmail(BuildConfig.EMAIL_JUD_NOTIFICATIONS, getString(co.gov.cnsc.mobile.simo.R.string.contact_simo_mobile), "")
    }*/

    fun onFB(button:View) {
        val facebookId = "fb://page/320603784759677"
        val urlPage = "https://es-la.facebook.com/CNSCColombia/"

        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(facebookId)))
        } catch (e: Exception) {
            Log.e(FragmentActivity.ACTIVITY_SERVICE, "Aplicación no instalada.")
            //Abre url de pagina.
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlPage)))
        }
    }

    fun onTW(button:View) {
        val twitterId = "twitter://user?screen_name=CNSCColombia"
        val urlPage = "https://twitter.com/CNSCColombia/"

        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(twitterId)))
        } catch (e: Exception) {
            Log.e(FragmentActivity.ACTIVITY_SERVICE, "Aplicación no instalada.")
            //Abre url de pagina.
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlPage)))
        }
    }

    fun onYT(button:View) {
        val youtubeId = "https://www.youtube.com/channel/UCokziF7dNEGao2dsOMMTHJw"
        val urlPage = "https://www.youtube.com/channel/UCokziF7dNEGao2dsOMMTHJw"

        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(youtubeId)))
        } catch (e: Exception) {
            Log.e(FragmentActivity.ACTIVITY_SERVICE, "Aplicación no instalada.")
            //Abre url de pagina.
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlPage)))
        }
    }

    /**
     * Evento cuando se da tap sobre el logo de la CNSC
     */
    fun onWSCNSC(button: View) {
        val urlPage = "https://www.cnsc.gov.co/"
        //Abre url de pagina.
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlPage)))
    }

    /**
     * Evento cuando se da tap sobre el texto Chat
     */
    fun onChatCNSC(button: View) {
        val urlPage = "https://www.cnsc.gov.co/index.php/atencion-al-ciudadano/chat"
        //Abre url de pagina.
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlPage)))
    }

    /**
     * Ejecución al comenzar una activity
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenContact(this)
    }

}