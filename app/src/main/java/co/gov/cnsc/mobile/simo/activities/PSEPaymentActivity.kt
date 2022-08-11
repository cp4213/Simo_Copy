package co.gov.cnsc.mobile.simo.activities

import android.os.Bundle
import android.webkit.WebViewClient
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import kotlinx.android.synthetic.main.activity_pse_payment.*


/**
 * Contiene la funcionalidad de la pantalla de pago PSE
 */

class PSEPaymentActivity: SIMOActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_pse_payment)

        //showToolbarBack()

        setTextTitleToolbar(R.string.pse_process_title)
        //webviewpsepayment.setBackgroundColor(Color.YELLOW)

        var bundle :Bundle ?=intent.extras
        //var message = bundle!!.getString("value") // 1
        var strUser: String = intent.getStringExtra("payload").toString()
        //Toast.makeText(this, strUser, Toast.LENGTH_LONG).show()

        //Cargando webViewClient
        webviewpsepayment.setWebViewClient(WebViewClient())
        webviewpsepayment.settings.javaScriptEnabled = true

        //For production environment
        webviewpsepayment.loadUrl(strUser)

        //For testing environment before - but is good; por lo menos it let me pass
        //webviewpsepayment.loadUrl("http://181.225.68.224/" + strUser)

        //For testing environment ¿actually? I do not think so because the activity loaded is in blank
        //webviewpsepayment.loadUrl(strUser)

        //webviewpsepayment.loadUrl("https://registro.pse.com.co/PSEUserRegister/StartTransaction.htm?enc=tnPcJHMKlSnmRpHM8fAbuxQlX0zGOup9gf1d12uT7QYTS%2bCFI%2fRuE%2fUn67wVpdVt")
        //webviewpsepayment.canGoBack()
    }

    override fun onBackPressed() {
        //super.onBackPressed()

        /*val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)*/

        goToMainActivity()
        //Toast.makeText(getApplicationContext(), "Verifique el estado de su pago en el menú principal > Mis Pagos> \n¡Si el Estado de la Transacción es APROBADA ya puede Inscribir el Empleo! \n(Búsquelo en el menu principal > Mis Empleos > Confirmados >, y al abrir el detalle del mismo, haga click en el botón > Inscribir Empleo >", Toast.LENGTH_LONG).show();

    }

    /**
     * Ejecución al comenzar una activity
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenPSEPayment(this)
    }
}