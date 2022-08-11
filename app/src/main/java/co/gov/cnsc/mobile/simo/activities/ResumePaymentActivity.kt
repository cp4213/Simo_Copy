package co.gov.cnsc.mobile.simo.activities

import android.app.AlertDialog
import android.content.Intent

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.extensions.toFormatCurrency
import co.gov.cnsc.mobile.simo.models.*
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import kotlinx.android.synthetic.main.activity_resume_payment.*
import kotlinx.android.synthetic.main.layout_popup_window_validations.view.*
import org.json.JSONObject



class ResumePaymentActivity : SIMOActivity() {

    var workOffer: WorkOffer? = null
    var idInscripcion: String = ""
    var idEmpleo: String = ""
    var Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(co.gov.cnsc.mobile.simo.R.layout.activity_resume_payment)

        showToolbarBack()
        setTextTitleToolbar(co.gov.cnsc.mobile.simo.R.string.paymen_resume_title)

        //var bundle :Bundle ?=intent.extras
        //var message = bundle!!.getString("value") // 1
        idInscripcion = intent.getStringExtra("idInscripcion").toString()
        idEmpleo      = intent.getStringExtra("idEmpleo").toString()
        workOffer     = intent?.extras?.getParcelable("workOffer")


        getUser()
        asyncValidations()

        resume_confirmar_btn.setOnClickListener {v ->
            confirmarPago()
        }

        resume_cancel_btn.setOnClickListener{v ->
            onBackPressed()
        }
    }

    /**
     * Obtiene toda la información correspondiente al usuario logeado
     */
    private fun getUser() {
        val session = SIMO.instance.session
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.getUser(session?.username!!, { user ->
            ProgressBarDialog.stopProgressDialog()
            resume_nombres_txt.text          = "Nombres: " + user.name
            resume_apellidos_txt.text        = "Apellidos: " +user.lastName
            resume_dni_txt.text              = "Número de identificación: " + user.identifier
            resume_convocatoria_txt.text = "Convocatoria: " + workOffer?.job?.convocatory.toString()
            resume_salary_txt.text = "Asignación salarial: " + workOffer?.job?.salary?.toDouble()?.toFormatCurrency()
            resume_nivel_txt.text            = "Nivel: " + workOffer?.job?.gradeLevel?.levelName
            resume_empleo_txt.text       = "Cargo: " + workOffer?.job?.denomination?.name
            resume_opec_txt.text       = "Número Empleo (OPEC): " + workOffer?.job?.id

        }) { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            SIMOApplication.showErrorConectionAlertToTry(this) {
                getUser()
            }
        }
    }

    var existPayment    = false
    var soonDueDate     = false
    private val handler = Handler()

    private fun asyncValidations(){
        Handler().postDelayed({
            ProgressBarDialog.startProgressDialog(this,"Finalizando validaciones","Espere")

            RestAPI.validateExistAnotherPayment(idEmpleo, { json ->
                existPayment = validatePayment(json)
                handler.postDelayed({ needsShowsDialog() }, 2000)
            }, { fuelError ->
                //Si se requiere rastrear un error, use un Log.d("err", fuelError.toString())
            })

            RestAPI.validatePaymentDueDate(idInscripcion, { json ->
                val obj = JSONObject(json)
                val value : Boolean = obj.getBoolean("valor")
                soonDueDate = value
                //handler.postDelayed({ needsShowsDialog() }, 2000)
            }, { fuelError ->
                //Si se requiere rastrear un error, use un Log.d("err", fuelError.toString())
            })

        }, 1000)
    }

    private fun validatePayment(json: String): Boolean {
        if(json.contains("id")){ return true }
        return false
    }

    private fun needsShowsDialog(){
        Log.d("DEV", ">>>>> Executed after 2 secs >>> exist payment?: " + existPayment)
        Log.d("DEV", ">>>>> Executed after 2 secs >>> soon date?: " + soonDueDate)
        ProgressBarDialog.stopProgressDialog()

        if(existPayment){
            showDialog("Usted ya realizó un pago para concursar en un empleo perteneciente a ésta misma convocatoria.\nEs su responsabilidad continuar con éste pago; no habrán devoluciones.\nRecuerde que solo podrá inscribirse a un (1) empleo por convocatoria.")
        }

        if(soonDueDate){
            showDialog("La fecha de cierre de inscripciones para éste empleo vence hoy a las 11:59:59 p.m.\nRecuerde que luego del pago debe validar que todos sus soportes estén completos e inscribir el empleo (calcule su tiempo).")
        }

        if(soonDueDate && existPayment){
            showDialog("Usted ya realizó un pago para concursar en un empleo perteneciente a ésta misma convocatoria.\nAdicionalmente la fecha de cierre de inscripciones vence hoy a las 11:59:59 p.m.")
        }
    }

    /**
     * Muestra el CustomDialog
     */
    private fun showDialog(Message: String){
        val customDialog= LayoutInflater.from(Context).inflate(co.gov.cnsc.mobile.simo.R.layout.layout_popup_window_validations, null)
        val textMessage = customDialog.cerrar_val_btn
        val closeButton = customDialog.cerrar_btn

        textMessage.text = Message

        val dialogBuilder = AlertDialog.Builder(Context)
                .setView(customDialog)
                .setTitle("¡Aviso importante!")
        val alertDialog = dialogBuilder.show()

        closeButton.setOnClickListener {v ->
            alertDialog.hide()
        }
    }

    private fun confirmarPago(){
        ProgressBarDialog.startProgressDialog(this,"Cargando PSE","Espere...")
        RestAPI.registerPayment(idInscripcion , { json ->

            if(json.contains("errorinesperado")){
                val msg : String = JSONObject(json).getString("errorinesperado")
                Toast.makeText(applicationContext,msg ,Toast.LENGTH_LONG).show()
                //Log.d("DEV","error inesperado :(" + json.toString())
                ProgressBarDialog.stopProgressDialog()
            } else {
                val payload: String = JSONObject(json).getString("valor")
                val intent = Intent(Context, PSEPaymentActivity::class.java)
                intent.putExtra("payload", payload)
                ProgressBarDialog.stopProgressDialog()
                startActivity(intent)
            }

        }, { fuelError ->
            // to-do
            /*Log.d("Dev", "payment response: " + fuelError.toString());
            */
            ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
            //Toast.makeText(applicationContext," Msg: " + fuelError.toString() ,Toast.LENGTH_LONG).show()
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        /*val intent = Intent(this, MyJobsFragment::class.java)
        startActivity(intent)*/
    }

    /**
     * Ejecución al comenzar una activity
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenResumePayment(this)
    }
}
