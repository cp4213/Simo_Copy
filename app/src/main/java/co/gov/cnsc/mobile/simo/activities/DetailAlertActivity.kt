package co.gov.cnsc.mobile.simo.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.models.Alert
import co.gov.cnsc.mobile.simo.network.RestAPI
import kotlinx.android.synthetic.main.activity_detail_alert.*

/**
 * Esta pantalla muestra el detalle de una alerta seleccionada
 */
class DetailAlertActivity : SIMOActivity() {

    /**
     * Alerta seleccionada
     */
    var alert: Alert? = null

    /**
     * Posición de dicha alerta en el listado
     */
    var position: Int? = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_alert)
        showToolbarBack()
        setTextTitleToolbar(R.string.alert_detail)
        position = intent?.extras?.getInt("position", -1)
        alert = intent?.extras?.getParcelable("alert")
        readAlert()
        paint()
    }


    /**
     * Ejecuta un web service para que la alerta quede en estado leído en el servidor
     */
    private fun readAlert() {
        RestAPI.readAlert(alert?.id, { json ->
            Log.d(SIMOApplication.TAG, json.toString())
        }, { fuelError ->
            showFuelError(fuelError)
        })
    }

    /**
     * Pinta los datos de la alerta en el layout de detalle "activity_detail_alert" (trae el contenido más importante de la alerta)
     */
    fun paint() {
        textViewDate?.text = alert?.notification?.dateSchedule
        textViewTitle?.text = alert?.notification?.subject
        val html = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(alert?.notification?.description, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(alert?.notification?.description)
        }
        textViewDescription?.text = html
        webviewAlertDescription.loadData(alert?.notification?.description!!, "text/html", "utf-8")
    }

    /**
     * Vuelve a la pantalla anterior, notificando el cambio de estado de la alerta en el listado
     */
    private fun setResult() {
        val intent = Intent()
        intent.putExtra("position", position)
        setResult(RESULT_OK, intent)
    }

    /**
     * Cuando esta pantalla se cierra devuelve la posición al listado
     */
    override fun finish() {
        setResult()
        super.finish()
    }

    /**
     * Ejecución al comenzar una activity
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenDetailAlert(this)
    }
}
