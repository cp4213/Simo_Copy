package co.gov.cnsc.mobile.simo.activities

import android.os.Bundle
import android.webkit.WebView
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import kotlinx.android.synthetic.main.activity_legal_advice.*

/**
 * Esta clase contiene la funcionalidad de la pantalla 'Aviso Legal'
 */
class LegalAdviceActivity : SIMOActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legal_advice)
        showToolbarBack()
        setTextTitleToolbar(R.string.legal_advice)
        configureUI()
    }

    /**
     * Configura los elementos de la interfaz gráfica
     */
    fun configureUI() {
        val settings = webview.settings
        settings.builtInZoomControls = false
        webview.scrollBarStyle = WebView.SCROLLBARS_INSIDE_OVERLAY
        webview.isScrollbarFadingEnabled = false
        webview.loadUrl("file:///android_asset/policy_privacy.html")
    }

    /**
     * Ejecución al comenzar la activity
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenTermsService(this)
    }
}
