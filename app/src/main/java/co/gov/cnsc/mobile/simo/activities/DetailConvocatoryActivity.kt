package co.gov.cnsc.mobile.simo.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.adapters.NormativityAdapter
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.models.Convocatory
import co.gov.cnsc.mobile.simo.models.Normativity
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import co.gov.cnsc.mobile.simo.views.RowLabelValue
import co.gov.cnsc.mobile.simo.views.RowStageConvocatoryView
import kotlinx.android.synthetic.main.activity_detail_convocatory.*

/**
 * Esta clase contiene la funcionalidad para mostrar el detalle de una convocatoria
 */
class DetailConvocatoryActivity : SIMOActivity() {

    /**
     * Parte superior de la pantalla, cabezota
     */
    private var header: View? = null

    /**
     * Contiene el adapter de normatividad para ser mostrado en un listado
     */
    private var adapterNormativity: NormativityAdapter? = null

    /**
     * Convocatoria a ser mostrada en pantalla
     */
    private var convocatory: Convocatory? = null

    /**
     * Litado de etapas de convocatoria a ser mostrada
     */
    private var stages: List<Convocatory.Stage>? = null

    /**
     * Contiene el listado de normatividad para ser mostrado en un listado
     */
    private var normativities: List<Normativity>? = null

    /**
     * Id de la convocatoria a mostrar
     */
    private var id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_convocatory)
        showToolbarBack()
        setTextTitleToolbar(R.string.detail_convocatory)
        id = intent?.extras?.getString("id")
        header = layoutInflater.inflate(R.layout.view_header_convocatory, listViewNormativity, false)
        listViewNormativity.addHeaderView(header, null, false)
        adapterNormativity = NormativityAdapter(this, ArrayList<Normativity>())
        adapterNormativity?.onAttachmentListener = { normativity, position ->
            SIMOApplication.checkIfConnectedByData(this) {
                if (normativity.urlDocument != null && normativity.nameDocument != null)
                    SIMOApplication.checkPermissionsAndDownloadFile(this, normativity.urlDocument!!, normativity.nameDocument!!)
            }
        }
        listViewNormativity.adapter = adapterNormativity
        getConvocatory()
    }


    /**
     * Obtiene los datos de una convocatoria a través de un web service
     */
    private fun getConvocatory() {
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.getConvocatory(id!!, { convocatory ->
            ProgressBarDialog.stopProgressDialog()
            this.convocatory = convocatory
            paintBasic()
            getStages()
        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
            finish()
        })
    }

    /**
     * Obtiene todas las etapas de una convocatoria a través de un web service
     */
    private fun getStages() {
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.getStages(id!!, { stages ->
            ProgressBarDialog.stopProgressDialog()
            this.stages = stages
            paintStages()
            getNormativity()
        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
        })
    }

    /**
     * Obtiene la 'Normatividad' relacionada con una convocatoria
     */
    private fun getNormativity() {
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.getNormativity(id!!, { normativities ->
            ProgressBarDialog.stopProgressDialog()
            this.normativities = normativities
            paintNormativity()
        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
        })
    }

    /**
     * Pinta los datos básicos de la convocatoria
     */
    private fun paintBasic() {
        header?.findViewById<RowLabelValue>(R.id.rowConvocatory)?.value = this.convocatory?.fullNameConvocatory
        header?.findViewById<RowLabelValue>(R.id.rowEntity)?.value = this.convocatory?.entity?.name!!
    }


    /**
     * Pinta las etapas de una convocatoria
     */
    private fun paintStages() {
        val rowStageTitle = header?.findViewById<RowStageConvocatoryView>(R.id.rowStageTitle)
        rowStageTitle?.dateStart = "Fecha Inicio"
        rowStageTitle?.dateEnd = "Fecha Fin"
        rowStageTitle?.description = "Descripción"
        rowStageTitle?.setTextviewsToBold()
        val linearStages = header?.findViewById<LinearLayout>(R.id.linearStages)
        stages?.forEach { stage ->
            val view = RowStageConvocatoryView(this, null)
            view.dateStart = stage.dateStart
            view.dateEnd = stage.dateEnd
            view.description = stage.description
            linearStages?.addView(view)
        }
    }

    /**
     * Pinta el listado de normas en la pantalla
     */
    private fun paintNormativity() {
        adapterNormativity?.addItems(normativities)
    }

    /**
     * Ejecución al comenzar una activity
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenDetailConvocatory(this)
        Log.i("Act","DetailConvocatoryA")
    }
}
