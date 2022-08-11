package co.gov.cnsc.mobile.simo.activities

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import kotlinx.android.synthetic.main.activity_enroll_employment.*
import org.json.JSONArray
import org.json.JSONObject

class EnrollEmploymentActivity : SIMOActivity() {

    val context : Context = this
    var idInscripcion: String = ""
    var idEmpleo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enroll_employment)

        showToolbarBack()
        setTextTitleToolbar(R.string.enroll_employment_details)

        idInscripcion = intent.getStringExtra("idInscripcion").toString()
        idEmpleo      = intent.getStringExtra("idEmpleo").toString()

        Log.d("DEV", "-> Id inscripción: $idInscripcion")
        getSupportResumeEnroll(idInscripcion)

        /*enroll_cancel_btn?.setOnClickListener {
            super.onBackPressed()
        }*/

        enroll_cv_btn?.setOnClickListener {
            val intent = Intent(context, MyCVActivity::class.java)
            startActivity(intent)
        }

        enroll_btn?.setOnClickListener {
            enrollEmployment(idEmpleo)
        }

        cat_link_txt?.setOnClickListener {
            getDownloadPDFEnroll(idInscripcion)
        }
    }

    /**
     * Función para realizar la inscripción de un empleo
     */
    private fun enrollEmployment(idEmpleo: String){
        ProgressBarDialog.startProgressDialog(this,"Inscribiendo Empleo","Espere...")
        return RestAPI.enrollmentToEmployment(idEmpleo, { json ->

            ProgressBarDialog.stopProgressDialog()
            if(json.equals("null")){
                customDialog("¡Empleo inscrito exitosamente!\n\nPara consultar sus empleos inscritos vaya al menú > Mis Empleos > INSCRITOS >", "Continuar")
            } else{
                customDialog("¡Ya tiene inscrito el empleo!", "Salir")
            }

        }, { fuelError ->
            //to-do
            ProgressBarDialog.stopProgressDialog()
            Log.d("DEV", "error" + fuelError.toString())
        })
    }

    /**
     * Función para traer la información del resumen de los soportes
     */
    private fun getSupportResumeEnroll(idInscipcion: String){
        return RestAPI.getSupportResumeEnroll(idInscipcion, {json ->

            val array = JSONArray(json)

            cant_items_num_txt.text  = "" + JSONObject(array[0].toString()).get("registrados")
            cant_items_num_txt2.text = "" + JSONObject(array[1].toString()).get("registrados")
            cant_items_num_txt3.text = "" + JSONObject(array[2].toString()).get("registrados")
            cant_items_num_txt4.text = "" + JSONObject(array[3].toString()).get("registrados")

        }, { fuelError ->
            Log.d("DEV", "-> err!" + fuelError.toString())
        })
    }

    /**
     * Función que trae el .pdf como descarga
     */
    private fun getDownloadPDFEnroll(idInscipcion: String){
        val url = "${RestAPI.HOST}/reporte/inscripcionPDF/${idInscipcion}"
        SIMOApplication.checkIfConnectedByData(this) {
                SIMOApplication.checkPermissionsAndDownloadFile(this, url,"${idInscipcion}.pdf")
        }
    }

    /**
     * Dialog personalizado para mensajes a consideración
     */
    fun customDialog(Message: String, ButtonText: String){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Aviso importante")
        builder.setMessage(Message)

        when(ButtonText){
            "Continuar" -> builder.setPositiveButton(ButtonText){dialog, which ->
                             dialog.dismiss()
                             val intent = Intent(context, MainActivity::class.java)
                             startActivity(intent)
                           }
            "Ir a inicio" -> builder.setPositiveButton(ButtonText){dialog, which ->
                               dialog.dismiss()
                               val intent = Intent(context, MainActivity::class.java)
                               startActivity(intent)
                             }
            "Ok" -> builder.setPositiveButton(ButtonText){dialog, which ->
                       dialog.dismiss()
                    }
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    /**
     * Vuelve a la pantalla anterior envía la experiencia y la posición en el listado
     */
    /*private fun returnToBack(credential: Credential?) {
        setResult(RESULT_OK, intent)
        finish()
    }*/

    /**
     * Ejecución al comenzar una activity
     */
    override fun onStart() {
        Log.i("Act","EnrollEmploymentA")
        super.onStart()
        AnalyticsReporter.screenEnrollEmployment(this)
    }
}
