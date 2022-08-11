package co.gov.cnsc.mobile.simo.activities

import android.content.Intent
import android.os.Bundle
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import kotlinx.android.synthetic.main.activity_transfer_payment.*
import android.view.View
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import org.json.JSONObject


class TransferPaymentActivity : SIMOActivity() {

    var idEmpleoActual: String = ""
    var idInscripcion: String = ""

    var idEmpleoNuevo: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_payment)

        showToolbarBack()
        setTextTitleToolbar(R.string.payment_transfer_conditions_title)

        idEmpleoActual = intent.getStringExtra("idEmpleo").toString()
        idInscripcion = intent.getStringExtra("idInscripcion").toString()

        textViewActualOpecNumber.text = idEmpleoActual

        resume_employment_card_view.visibility = View.GONE
        buttonSaveNewOPEC.visibility = View.GONE

    }

    fun onPaymentTransferValidation(button: View) {
        cleanErrors()
        val validate = validateForm()
        if (validate) {
            paymentTransferValidation()
        } else {
            TextViewValidationMessage.text = ""
        }

    }

    /**
     * Esconde los labels de errores en cada uno de los campos de formulario
     */
    fun cleanErrors() {
        textInputNewOpecNumber?.error = null
    }

    /**
     * Valida la información en el formulario
     */
    private fun validateForm(): Boolean {
        var validate = true
        if (editNewOpecNumber?.text?.matches(SIMOApplication.REGEX_FOR_OPEC_NUMBER.toRegex()) == false) {
            textInputNewOpecNumber?.error = getString(R.string.opec_number_requirements)
            validate = false
        }
        return validate
    }

    fun paymentTransferValidation() {
        idEmpleoNuevo = editNewOpecNumber.text.toString()

        RestAPI.validatePaymentTransfer(idEmpleoActual, idEmpleoNuevo, { json ->

            if (json == "null") {
                resume_employment_card_view.visibility = View.GONE
                buttonSaveNewOPEC.visibility = View.GONE
                TextViewValidationMessage.text = getString(R.string.negative_payment_transfer_validation_message)
            } else {

                val obj = JSONObject(json)
                val denominacion = obj.getJSONObject("denominacion")
                val cargo = denominacion.getString("nombre")

                val numeroOPEC = obj.getString("id")

                val gradoNivel = obj.getJSONObject("gradoNivel")
                val grado = gradoNivel.getString("grado")

                val asignacionSalarial = obj.getString("asignacionSalarial")

                TextViewValidationMessage.text = getString(R.string.positive_payment_transfer_validation_message)

                rowEmplymentName.value = cargo
                rowOPEC.value = numeroOPEC
                rowGrade.value = grado
                rowSalary.value = asignacionSalarial

                resume_employment_card_view.visibility = View.VISIBLE
                buttonSaveNewOPEC.visibility = View.VISIBLE

            }

        }, { fuelError ->
            showFuelError(fuelError)
            finish()

        })

    }

    //Acción cuando se hace tap en el botón Transferir Pago
    fun onPaymentTransferConfirmation(button: View) {
        confirmPaymentTransferDialog()

    }

    //Función que activa ventana emergente de notificación para Transferir el Pago a Otro Empleo
    fun confirmPaymentTransferDialog(){
        SIMOApplication.showConfirmDialog(this, R.string.payment_transfer_conditions_title,
                R.string.payment_transfer_dialog, R.string.accept_button_dialog, { dialogInterface, which ->

            paymentTransferConfirmation()

        }, R.string.cancel_button_dialog, { dialogInterface, which ->

        })
    }

    fun paymentTransferConfirmation() {
        //idEmpleoNuevo = editNewOpecNumber.text.toString()
        RestAPI.confirmPaymentTransfer(idEmpleoActual, idEmpleoNuevo, idInscripcion, { json ->
            ProgressBarDialog.stopProgressDialog()
            //val message = response.obj().getString("mensajeRespuestaRestOk")
            SIMOApplication.showAlertDialog(this, R.string.payment_transfered_messasge_title,
                    R.string.payment_transfered_messasge_body, R.string.continue_button_dialog) { dialogInterface, i ->

                /*var idEmpleoDWO: String? = null
                //var isFavorite: String? = null
                var idInscripcionDWO: String? = null
                var statusInscriptionDWO: String? = null
                //var workoffer: String? = null

                idEmpleoDWO =idEmpleoNuevo
                //isFavorite =idEmpleoActual
                idInscripcionDWO =idInscripcion
                statusInscriptionDWO = "PI"
                //workoffer =idEmpleoActual

                val intent = Intent(this, DetailWorkOfferActivity::class.java)
                intent.putExtra("id", idEmpleoDWO)
                //intent.putExtra("parameterTo", isFavorite)
                intent.putExtra("id_inscription", idInscripcionDWO)
                intent.putExtra("status_inscription", statusInscriptionDWO)
                //intent.putExtra("parameterTo", workoffer)
                //inscription.jobId, workoffer.job null, inscription.isFavorite, inscription.id, inscription.statusInscription*/

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

                //finish()

            }
        }, { fuelError ->
            showFuelError(fuelError)
            finish()

        })

    }



    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenTransferPayment(this)
    }
}