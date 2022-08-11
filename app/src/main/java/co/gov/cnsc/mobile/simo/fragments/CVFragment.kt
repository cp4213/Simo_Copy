package co.gov.cnsc.mobile.simo.fragments

import co.gov.cnsc.mobile.simo.SIMOFragment
import com.github.kittinunf.fuel.core.Request

/**
 * Esta clase padre contiene funcionalidades comunes para los fragments de CV
 */
open class CVFragment : SIMOFragment() {

    /**
     * Request para traer información de backend
     */
    var request: Request? = null

    /**
     * Al cerrar o destruir el fragment el llamado al servidor se cancela
     */
    override fun onDestroy() {
        super.onDestroy()
        //request?.cancel()
    }
}