package co.gov.cnsc.mobile.simo.models

import co.gov.cnsc.mobile.simo.R

/**
 * Datos de los slides del inicio de la aplicación
 */
data class AdviceTutorial(
        val imageRes: Int? = R.drawable.logo_cnsc_hrznt_pos_trnsp,
        val descriptionRes: Int? = 0,
        val imageBackground: Int? = R.drawable.primer_slide
)