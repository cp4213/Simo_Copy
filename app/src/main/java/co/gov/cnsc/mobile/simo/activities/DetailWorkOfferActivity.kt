package co.gov.cnsc.mobile.simo.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.SIMOApplication.Companion.showConfirmDialog
import co.gov.cnsc.mobile.simo.adapters.DeptosAdapter
import co.gov.cnsc.mobile.simo.adapters.MncipiosAdapter
import co.gov.cnsc.mobile.simo.adapters.PaymentsAdapter
import co.gov.cnsc.mobile.simo.adapters.TestsAdapter
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.auth.AccountUtils
import co.gov.cnsc.mobile.simo.extensions.intentShareTextLink
import co.gov.cnsc.mobile.simo.extensions.toFormatCurrency
import co.gov.cnsc.mobile.simo.models.*
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.util.ExtrasUtils
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import kotlinx.android.synthetic.main.activity_detail_work_offer.*
import kotlinx.android.synthetic.main.fragment_payments.*
import kotlinx.android.synthetic.main.layout_content_app_bar_detail_work.*
import kotlinx.android.synthetic.main.layout_departmens_popup.view.*
import kotlinx.android.synthetic.main.layout_departmens_popup.view.textView3
import org.json.JSONObject
import org.json.JSONArray

/**
 * Esta clase contiene la funcionalidad para mostrar el detalle de una oferta laboral
 */
class DetailWorkOfferActivity : SIMOActivity() {

    /**
     * Id del empleo como variable global
     */
    var idEmployment : String = ""
    var request: Request? = null
    /**
     * Id de la Inscripción
     */
    var idInscripcionAux : String = ""

    /**
     * Id de la Prueba
     */
    var idPruebaAux : String = ""

    /**
     * Id del Departamento
     */
    var idDeptoAux : String = ""

    /**
     * Id del Municipio
     */
    var idMcipioAux : String = ""

    /**
     * Id de la Prueba del Ciudadano
     */
    var idPruebaCiudadanoAux : String = ""

    /**
     * Total pruebas encontrada (Número de registros en el ListView)
     */
    var totalLineasPruebas : Int = 0

    /**
     * Posición de dicha prueba
     */
    var position: Int? = -1

    /**
     * Oferta laboral a ser mostrada
     */
    var workOffer: WorkOffer? = null

    /**
     * Fecha limite de la Oferta laboral
     */
    var jobDueDate: String = ""

    /**
     * Total Inscritos en la Oferta laboral
     */
    var totalCitizensEnrolled: String = ""

    /**
     * Id de la oferta laboral a ser mostrada
     */
    var id: String? = null

    /**
     * ¿El usuario está autenticado?
     */
    var isAutheticated : Boolean = false

    var adapter: PaymentsAdapter? = null

    /**
     * ExtrasUtils class para usar diferentes métodos utiles
     * Ej. validar si la fecha de la convocatoria es valida o no
     */
    var utils               = ExtrasUtils()
    var listView: ListView? = null
    var Context             = this
    var STEP_TAG            = "confirmar"

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_work_offer)
        showToolbarBack()
        setTextTitleToolbar(R.string.employ_detail)

        id = intent?.extras?.getString("id")
        val isFavorite: Boolean? = intent?.extras?.getBoolean("is_favorite", false)
        val idInscription: String? = intent?.extras?.getString("id_inscription")
        val statusInscription: String? = intent?.extras?.getString("status_inscription")
        workOffer = intent?.extras?.getParcelable("work_offer")
        //Log.d("DEV",workOffer?.toString())
        workOffer?.favorite = isFavorite == true
        workOffer?.inscriptionId = idInscription
        workOffer?.statusInscription = statusInscription

        listView = findViewById<View>(R.id.testListView) as ListView?

        val cookie = FuelManager.instance.baseHeaders?.get("Cookie")
        isAutheticated = utils.isAuthenticated(cookie!!)
        Log.i("DEV", " >>>>>>>> 1_isAuthenticate: " + isAutheticated)
        configureUI()
        Log.i("DEV", "----configureUI()")
        getWorkOffer()

        listView!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, i, id ->
            // Se captura el id de la prueba y se asigna a la variable item
            val item = listView!!.adapter.getItemId(i).toString()
            // Se asigna la variable item en una variable global idPruebaAux para ser usada fuera del scope
            idPruebaAux     = item
            //Toast.makeText(Context, "Item id: " + item, Toast.LENGTH_SHORT).show()
            getDepartments(idPruebaAux)
        }

        listView!!.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                //v.getParent().requestDisallowInterceptTouchEvent(true);
                //    return false;
                v?.parent?.requestDisallowInterceptTouchEvent(true)
                return false
                //return v?.onTouchEvent(event) ?: true
            }
        })
    }

    /**
     * Configura los elementos gráficos en la pantalla
     */
    @SuppressLint("RestrictedApi")
    fun configureUI() {
        //buttonLeft.visibility = View.VISIBLE //AAA
        buttonLeft?.setOnClickListener {
            setOrRemoveFromFavorites()
        }

        var isConfirmed : Boolean? = this.workOffer?.isPreInscriptionEnable
        //var isEnrolled : Boolean? = this.workOffer?.isInscriptionEnable
        idEmployment               = this.workOffer?.id.toString()
        //Log.d("DEV", "==>> Is confirmed: $isConfirmed");
        //Log.d("DEV", "==>> Id empleo: $idEmployment");

        showPanelTests(isConfirmed!!)
        getExpiredSessionStatus(idEmployment)
    }

    /**
     * Ocultar o mostrar el Card de pruebas dependiendo si el empleo ha sido confirmado o no
     */
    fun showPanelTests(isConfirmed: Boolean){
        if (isConfirmed == true){
            tests_card_view.visibility = View.VISIBLE
            getInscriptionStatus(idEmployment)
        } else{
            tests_card_view.visibility = View.GONE
        }
    }

    /**
     * Agrega o quita la oferta laboral del listado de empleos favoritos
     */

    private fun setOrRemoveFromFavorites() {
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.setOrRemoveAsFavorite(workOffer?.id, workOffer?.inscriptionId, !workOffer?.favorite!!,
            { workOffer ->
                ProgressBarDialog.stopProgressDialog()
                this.workOffer?.favorite = workOffer.favorite
                this.workOffer?.inscriptionId = workOffer.inscriptionId
                paint()
            }, { fuelError ->
                ProgressBarDialog.stopProgressDialog()
                showFuelError(fuelError)
                finish()
            })
    }

    /**
     * Permite confirmar la oferta laboral desde el detalle del empleo (favorito o no favorito)
     */
    @SuppressLint("RestrictedApi")
    private fun confirmEmployment() {
        val idEmployment = this.workOffer?.job?.id
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.confirmEmployment(idEmployment, { json ->
            ProgressBarDialog.stopProgressDialog()

            imageViewLeft.setImageResource(R.drawable.baseline_event_note_white_24)
            imageViewLeft.visibility = View.VISIBLE
            labelButtonLeft.setText(R.string.confirmed_employment)

            buttonLeft.visibility = View.GONE

            //buttonCenter.visibility = View.GONE
            //labelButtonCenter.visibility = View.GONE

            buttonRight.setImageResource(R.drawable.baseline_payment_white_18)
            labelButtonRight.setText(R.string.employment_payment_ask)

            //Mensaje de confirmación de preinscripcion al empleo
            STEP_TAG = "pagar"
            Log.d("DEV", "STEP TAG: " + STEP_TAG)
            accessButtonAction()
            confirmedEmploymentDialog()

        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
            Log.d("DEV", "error" + fuelError.toString())
            finish()
        })
    }

    /**
     * Pinta la ventana emergente que confirma que el empleo ha sido preinscrito exitosamente
     * e invita al ciudadano a actualizar su hoja de vida o a continuar con el proceso de pago
     */
    private fun confirmedEmploymentDialog() {
        showConfirmDialog(this, R.string.success_op_dialog,
            R.string.the_employment_has_been_confirmed_dialog, R.string.update_cv_button_dialog, { dialogInterface, which ->
                val intent = Intent(this, MyCVActivity::class.java)
                //Toast.makeText(this, R.string.historial_deleted, Toast.LENGTH_SHORT).show()
                reloadTestData(idEmployment)
                //accessButtonAction()
                startActivity(intent)

            }, R.string.continue_button_dialog, { dialogInterface, which ->

                reloadTestData(idEmployment)
            })
    }


    /**
     * Acción para actualizar información de H.V / Docs
     */
    @SuppressLint("RestrictedApi")
    private fun updateDocuments() {
        val idEmployment = this.workOffer?.job?.id
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.updateDocuments(idEmployment, { json ->
            ProgressBarDialog.stopProgressDialog()

            imageViewLeft.setImageResource(R.drawable.baseline_event_available_white_24)
            imageViewLeft.visibility = View.VISIBLE
            labelButtonLeft.setText(R.string.registered_employment)

            buttonLeft.visibility = View.GONE

            //buttonCenter.visibility = View.GONE
            //labelButtonCenter.visibility = View.GONE

            buttonRight.setImageResource(R.drawable.baseline_restore_page_white_18)
            labelButtonRight.setText(R.string.update_documents)

            rowTestPlace.visibility = View.VISIBLE
            rowNameTest.visibility = View.VISIBLE


            //Mensaje de confirmación de preinscripcion al empleo
            updatedDocumentsDialog()

        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
            finish()
        })
    }

    /**
     * Ventana de dialogos para actualizar información de H.V / Docs
     */
    private fun updatedDocumentsDialog() {
        showConfirmDialog(this, R.string.success_op_dialog,
            R.string.updated_documents,
            R.string.update_cv_button_dialog, { dialogInterface, which ->
                val intent = Intent(this, MyCVActivity::class.java)
                startActivity(intent)
            },
            R.string.accept_button_dialog, { dialogInterface, which ->
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            })
    }

    /**
     * Obtiene el estado de la inscripción
     */
    private fun getInscriptionStatus(idEmployment: String) : Unit {
        //ProgressBarDialog.startProgressDialog(this)
        return RestAPI.getInscriptionStatus(idEmployment, { json ->
            //ProgressBarDialog.stopProgressDialog()
            val obj = JSONObject(json)

            var idInscription = obj.get("idInscripcion")
            if (idInscription.equals("null")) {
                //nothing to do
            } else {
                idInscripcionAux = idInscription.toString()
                getTestsData(idInscription.toString())
                //Log.d("DEV", "==>> Id inscripcion: $idInscripcionAux");
            }
        }, { fuelError ->
            //to-do
            /*ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
            finish()*/
        })
    }

    /**
     * Recarga el listado de pruebas cuando es necesario en la cadena de eventos
     */
    private fun reloadTestData(idEmployment: String){
        //ProgressBarDialog.startProgressDialog(this)
        return RestAPI.getInscriptionStatus(idEmployment, { json ->
            //ProgressBarDialog.stopProgressDialog()
            val obj = JSONObject(json)
            var existsInscription:Boolean = obj.getBoolean("preInscripcion")
            showPanelTests(existsInscription)
        }, { fuelError ->
            //to-do
            /*ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
            finish()*/
        })
    }

    /**
     * Obtiene el listado de pruebas y las entrega a la funcion generateData
     */
    private fun getTestsData(idInscription: String){
        //ProgressBarDialog.startProgressDialog(this)
        RestAPI.getTests(idInscription, { json ->
            //ProgressBarDialog.stopProgressDialog()
            val arrayObj      = JSONArray(json)                                     // Obtengo el arrayObject
            var adapter       = TestsAdapter(this, generateData(arrayObj))
            listView?.adapter = adapter
            adapter.notifyDataSetChanged()
            totalLineasPruebas = listView?.adapter!!.count
        }, {fuelError ->
            //to-do
            /*ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
            finish()*/
        })
    }

    /**
     * Esta función obtiene el objeto JSON y lo renderiza en el listview: listView
     */
    private fun generateData(dataArrayObj: JSONArray): ArrayList<Tests> {
        var result = ArrayList<Tests>()

        for (i in 0..(dataArrayObj.length()-1)) {
            val objJson = JSONObject(dataArrayObj[i].toString())    // Obtengo cada elemento del Array
            var pruebaJson = JSONObject(objJson.get("prueba").toString())    // Obtengo el Objeto "prueba"
            var mcipioString = objJson.get("municipio").toString()

            // Para saber si una prueba contiene una selección previa de Municipio y Departamento
            // Es preciso revisar el objeto-nodo "municipio" y obtener su contenido,
            // si es 'null' es preciso asumir que NO existen elementos internos
            // de lo contrario para esa prueda existe una inscripción de lugar de prueba

            if(mcipioString.equals("null")){
                Log.d("DEV", "null return in municipio json node")
                var user        = Tests(pruebaJson.get("id").toString(), pruebaJson.get("descripcion").toString(), 0, "N/A", 0, "N/A")  // Obtengo el id y la descripción
                result.add(user)

            }else{
                Log.d("DEV", "mcipio long =>" + mcipioString)
                var mcipioJson = JSONObject(objJson.get("municipio").toString())
                var deptoJson  = JSONObject(mcipioJson.get("departamento").toString())
                var user       = Tests( pruebaJson.get("id").toString(),
                    pruebaJson.get("descripcion").toString(),
                    mcipioJson.getInt("id"),
                    mcipioJson.getString("nombre"),
                    deptoJson.getInt("id"),
                    deptoJson.getString("nombre")
                )
                result.add(user)
            }
        }

        return result
    }

    /**
     * Esta función obtiene el objeto JSON de los departamentos de pruebas y abre el dialog
     */
    private fun getDepartments(idPrueba: String){
        //ProgressBarDialog.startProgressDialog(this)
        RestAPI.getPresentationDepartments(idPrueba, { json ->
            //ProgressBarDialog.stopProgressDialog()
            showDialog(json)
        }, { fuelError ->
            //to-do
            /*ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
            finish()*/
        })
    }

    /**
     * Obtiene los detalles de la oferta laboral a través de un web service
     */
    private fun getWorkOffer() {
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.getJob(id!!, { job ->
            ProgressBarDialog.stopProgressDialog()
            this.workOffer?.job = job
            paint()
        }, { fuelError ->
            Log.d("DEV","Error getWorkOffer")
            ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
            finish()
        })
    }

    /**
     * Obtiene el estado de la sesión si esta expirada o no, y oculta los botones de acciones
     */
    private fun getExpiredSessionStatus(idEmployment: String) : Unit {
        return RestAPI.getInscriptionStatus(idEmployment, { json ->

            if (json.contains("No ha habido actividad por un tiempo prolongado. \\¡Cierre sesión e ingrese nuevamente!")) {
                linearOptions.visibility = View.GONE
                linearOptions2.visibility = View.GONE
                linearOptions3.visibility = View.GONE
                RestAPI.logout({ response ->
                    AccountUtils.removeAuthAccount(this, SIMO.instance.session)
                    goToStartActivity()
                }, { fuelError ->
                    AccountUtils.removeAuthAccount(this, SIMO.instance.session)
                    goToStartActivity()
                })
            } else {
                //nothing  to-do
            }
        }, { fuelError ->
            //to-do
        })
    }


    /**
     * Pinta los datos de la oferta laboral en la pantalla (sin botones de acción)
     */
    @SuppressLint("RestrictedApi")
    private fun paint() {
        Log.i("DEV","Paint()")
        Log.d("DEV","Paint Context :")
        //Labels y valores de la parte azul del detalle del empleo
        textWorkOfferCharge.text       = this.workOffer?.job?.denomination?.name
        textConvocatory.text           = this.workOffer?.job?.convocatory?.name

        textOpec.text                  = this.workOffer?.job?.id
        textLevel.text                 = this.workOffer?.job?.gradeLevel?.levelName
        textGrade.text                 = this.workOffer?.job?.gradeLevel?.grade
        textVacancies.text             = this.workOffer?.job?.totalVancancies.toString()
        //textSalary.text                = this.workOffer?.job?.salary.toString()
        textSalary.text                = this.workOffer?.job?.salary?.toDouble()?.toFormatCurrency()

        // Fecha de Cierre para el Detalle del Empleo en etapa de Inscripción desde Listado OPEC y Ver empleos de convocatoria
        textCloseInscriptions.text     = this.workOffer?.dateInscription

        rowCharge.label                = this.workOffer?.job?.denomination?.name
        rowConvocatory.label           = this.workOffer?.job?.convocatory?.name

        rowOPEC.value                  = this.workOffer?.job?.codeJob
        rowLevel.value                 = this.workOffer?.job?.gradeLevel?.levelName
        rowGrade.value                 = this.workOffer?.job?.gradeLevel?.grade
        rowVacancy.value               = this.workOffer?.job?.totalVancancies.toString()
        rowSalary.value                = this.workOffer?.job?.salary.toString()
        //rowSalary.value                = this.workOffer?.job?.salary?.toDouble()?.toFormatCurrency()
        //rowCloseInscription.value      = this.workOffer?.dateInscription

        //Labels y valores de la parte blanca del detalle del empleo
        rowPurpose.value               = this.workOffer?.job?.description
        rowFunctions.value             = this.workOffer?.job?.allFunctions
        rowRequirementStudy.value      = this.workOffer?.job?.allRequirementsStudy
        rowRequirementExperience.value = this.workOffer?.job?.allRequirementsExperience

        val studyAlternatives  = this.workOffer?.job?.allRequirementsStudyAlternatives

        if (studyAlternatives != null && studyAlternatives.isNotBlank()) {
            rowRequirementStudyAlternative.visibility = View.VISIBLE
            rowRequirementStudyAlternative.value = studyAlternatives
        }

        val experienceAlternatives = this.workOffer?.job?.allRequirementsExperienceAlternatives

        if (experienceAlternatives != null && experienceAlternatives.isNotBlank() && !experienceAlternatives.toString().equals("null")) {
            rowRequirementExperienceAlternative.visibility = View.VISIBLE
            rowRequirementExperienceAlternative.value = experienceAlternatives
        }

        val studyEquivalences = this.workOffer?.job?.allRequirementsStudyEquivalences

        if (studyEquivalences != null && studyEquivalences.isNotBlank()) {
            rowRequirementStudyEquivalence.visibility = View.VISIBLE
            rowRequirementStudyEquivalence.value = studyEquivalences
        }

        val experienceEquivalences = this.workOffer?.job?.allRequirementsExperienceEquivalences

        if (experienceEquivalences != null && experienceEquivalences.isNotBlank()) {
            rowRequirementExperienceEquivalence.visibility = View.VISIBLE
            rowRequirementExperienceEquivalence.value = experienceEquivalences
        }

        //Inicio Prepensionados
        rowTotalVacancies.value = this.workOffer?.job?.totalVancancies.toString()
        //CardView Ubicación y distribución de vacantes
        rowPrep1.value = this.workOffer?.job?.totalCharges.toString()

        workOffer?.job?.vacancies?.forEach { vacancy ->
            vacancy?.quantity
            rowPowers.value = this.workOffer?.job?.allVacancies
        }
        accessButtonAction()
        //Fin Prepensionados

    }

    /**
     * Muestra u oculta los botones, labels e imageviews de acuerdo con la etapa de la convocatoria
     */
    @SuppressLint("RestrictedApi")
    private fun accessButtonAction(){
        Log.d("DEV", " >>>>>>>> isAuthenticate: " + isAutheticated)
        if(!isAutheticated){
            buttonLeft.visibility       = View.GONE
            labelButtonLeft.visibility  = View.GONE
            buttonRight.visibility      = View.GONE
            labelButtonRight.visibility = View.GONE

            /*buttonCenter.visibility = View.GONE
            labelButtonCenter.visibility = View.GONE*/

            totalEnrolled_card_view.visibility = View.GONE
        }

        else {
            Log.i("DEV","idEmployment :$idEmployment")
            //ProgressBarDialog.startProgressDialog(this)

            RestAPI.getInscriptionStatus(idEmployment, { json ->
                Log.i("DEV","--->IN getInscriptionStatus")
                //ProgressBarDialog.stopProgressDialog()
                val obj = JSONObject(json)
                val existeInscripcion = obj.getBoolean("existeInscripcion")
                val reporteInscripcion = obj.getString("reporteInscripcion")

                val access = obj.getJSONObject("access")
                //var pagar = access.getBoolean("pagar")
                val pago_pendiente  = access.getBoolean("pago_pendiente")
                val cambiarEmpleo = access.getBoolean("cambiarEmpleo")
                // var ver_pago = access.getBoolean("ver_pago")
                val pagarPSE = access.getBoolean("pagarPSE")
                val actualizar = access.getBoolean("actualizar")
                val lugarPresentacion = access.getBoolean("lugarPresentacion")
                val inscribir = access.getBoolean("inscribir")
                val preinscribir = access.getBoolean("preinscribir")
                // val tipoProceso = this?.workOffer?.job?.convocatory?.procesType
                //  NOTA verParticipantesAscensos	true

                Log.d("DEV", ">>>> existeInscripción: " + existeInscripcion + " >>>> reporteInscripcion " + reporteInscripcion + " >>>>>>>> Access node details: " + access.toString())
                /**
                 * Funcionalidad preinscribir = true, inscribir= false (ya que si está inscrito, debe omitirse este if)
                 * Agregar o Quitar de Favoritos y Confirmar Empleo
                 * */

                if(preinscribir && !inscribir){
                    Log.d("DEV", ">>>>>>>> Entró a confirmar")
                    STEP_TAG = "confirmar"
                    Log.d("DEV", "STEP TAG: " + STEP_TAG)

                    //LEFT
                    //show button and label
                    buttonLeft.visibility  = View.VISIBLE
                    buttonLeft.setImageResource(R.drawable.ic_favorite_border_24dp)
                    labelButtonLeft.visibility = View.VISIBLE
                    labelButtonLeft.setText(R.string.add_favorites)

                    //hide imageView
                    imageViewLeft.visibility = View.GONE
                    imageViewLeft.setImageResource(R.drawable.baseline_event_note_white_24)

                    //CENTER
                    //hide button and label
                    /*buttonCenter.visibility = View.GONE
                    labelButtonCenter.visibility = View.GONE*/

                    //RIGHT
                    //show button and label
                    buttonRight.visibility = View.VISIBLE
                    buttonRight.setImageResource(R.drawable.baseline_calendar_today_white_24)
                    labelButtonRight.visibility = View.VISIBLE
                    labelButtonRight.setText(R.string.confirm_employment_ask)


                    /**
                     * Funcionalidad de agregar o quitar favorito
                     * */
                    if (this.workOffer?.isFavoriteEnable == true) {
                        Log.d("DEV", ">>>>>>>> Favoritos")
                        buttonLeft.visibility  = View.VISIBLE
                        buttonRight.visibility = View.VISIBLE

                        //buttonCenter.visibility = View.GONE
                        //labelButtonCenter.visibility = View.GONE

                        if (this.workOffer?.favorite == true) {
                            buttonLeft.setImageResource(R.drawable.ic_favorite_24dp)
                            labelButtonLeft.setText(R.string.remove_favorites)
                            //Toast.makeText(this, R.string.employment_deleted_from_favorites, Toast.LENGTH_SHORT).show()
                        }

                        else {
                            buttonLeft.setImageResource(R.drawable.ic_favorite_border_24dp)
                            labelButtonLeft.setText(R.string.add_favorites)
                            //Toast.makeText(this, R.string.employment_added_to_favorites, Toast.LENGTH_SHORT).show()
                        }
                    }

                    buttonRight?.setOnClickListener {
                        rightButtonAction(STEP_TAG)
                    }
                }

                /**
                 * Funcionalidad pagarPSE = true
                 * Empleo Confirmado y ¿Pagar?
                 * Se activa el botón pagar y sus respectivas funcionalidades
                 * */
                if(pagarPSE){
                    Log.d("DEV", ">>>>>>>> Entró a pagar")
                    STEP_TAG = "pagar"
                    Log.d("DEV", "STEP TAG: " + STEP_TAG)

                    //LEFT
                    //hide button and label
                    buttonLeft.visibility  = View.GONE
                    labelButtonLeft.visibility = View.GONE

                    //LEFT
                    //show imageView and label
                    imageViewLeft.visibility = View.VISIBLE
                    imageViewLeft.setImageResource(R.drawable.baseline_event_note_white_24)

                    labelButtonLeft.visibility = View.VISIBLE
                    labelButtonLeft.setText(R.string.confirmed_employment)


                    //RIGHT
                    //show button and label
                    buttonRight.visibility = View.VISIBLE
                    buttonRight.setImageResource(R.drawable.baseline_payment_white_18)

                    labelButtonRight.visibility = View.VISIBLE
                    labelButtonRight.setText(R.string.employment_payment_ask)

                    //botón derecho habilitado
                    buttonRight?.setOnClickListener {
                        rightButtonAction(STEP_TAG)
                    }
                }

                /**
                 * Funcionalidad pago_pendiente = true
                 * Empleo Confirmado y Pendiente de Confirmación del Pago
                 * Se activa el botón de información y sus respectivas funcionalidades
                 * */
                if(pago_pendiente){
                    Log.d("DEV", ">>>>>>>> Entró a pagar")
                    STEP_TAG = "espera_respuesta_banco"
                    Log.d("DEV", "STEP TAG: " + STEP_TAG)

                    //LEFT
                    //hide button and label
                    buttonLeft.visibility  = View.GONE
                    labelButtonLeft.visibility = View.GONE

                    //LEFT
                    //show imageView and label
                    imageViewLeft.visibility = View.VISIBLE
                    imageViewLeft.setImageResource(R.drawable.baseline_event_note_white_24)

                    labelButtonLeft.visibility = View.VISIBLE
                    labelButtonLeft.setText(R.string.confirmed_employment)

                    //RIGHT
                    //show button and label
                    buttonRight.visibility = View.VISIBLE
                    buttonRight.setImageResource(android.R.drawable.ic_dialog_info)

                    labelButtonRight.visibility = View.VISIBLE
                    labelButtonRight.setText(R.string.employment_processing_payment)

                    //botón derecho inhabilitado
                    rightButtonAction(STEP_TAG)
                }

                /**
                 * Funcionalidad inscribir = true
                 * Empleo Pagado, Transferir Pago a Otro Empleo e ¿Inscribir?
                 * Se ha inscrito el empleo y permite actualizar información de H.V y docs
                 * */
                if(inscribir) {
                    Log.d("DEV", ">>>>>>>> Entró a inscribir")
                    STEP_TAG = "inscribir_empleo"
                    Log.d("DEV", "STEP TAG: " + STEP_TAG)

                    //LEFT
                    //hide button
                    buttonLeft.visibility  = View.GONE

                    //show imageView and label
                    imageViewLeft.visibility = View.VISIBLE
                    imageViewLeft.setImageResource(R.drawable.baseline_payment_white_18)

                    labelButtonLeft.visibility = View.VISIBLE
                    labelButtonLeft.setText(R.string.employment_payed)

                    /*//CENTER
                    //show button and label
                    buttonCenter.visibility = View.VISIBLE
                    labelButtonCenter.visibility = View.VISIBLE

                    //botón central habilitado
                    buttonCenter?.setOnClickListener {
                        rightButtonAction("cambiar_empleo")
                    }*/

                    //RIGHT
                    //show button and label
                    buttonRight.visibility = View.VISIBLE
                    buttonRight.setImageResource(R.drawable.baseline_event_available_white_24)

                    labelButtonRight.visibility = View.VISIBLE
                    labelButtonRight.setText(R.string.registered_employment_ask)

                    //botón derecho habilitado
                    buttonRight?.setOnClickListener {
                        rightButtonAction(STEP_TAG)
                    }
                }

                /**
                 * Funcionalidad inscribir = true
                 * Empleo Pagado, Transferir Pago a Otro Empleo e ¿Inscribir?
                 * Se ha inscrito el empleo y permite actualizar información de H.V y docs
                 * */
                if(cambiarEmpleo) {
                    Log.d("DEV", ">>>>>>>> Entró a cambiar empleo")
                    STEP_TAG = "cambiar_empleo"
                    Log.d("DEV", "STEP TAG: " + STEP_TAG)

                    //LEFT
                    //hide button
                    buttonLeft.visibility  = View.GONE

                    //show imageView and label
                    imageViewLeft.visibility = View.VISIBLE
                    imageViewLeft.setImageResource(R.drawable.baseline_payment_white_18)

                    labelButtonLeft.visibility = View.VISIBLE
                    labelButtonLeft.setText(R.string.employment_payed)

                    //CENTER
                    //show button and label
                    buttonCenter.visibility = View.VISIBLE
                    labelButtonCenter.visibility = View.VISIBLE

                    //botón central habilitado
                    buttonCenter?.setOnClickListener {
                        rightButtonAction(STEP_TAG)
                    }

                    //RIGHT
                    //show button and label
                    buttonRight.visibility = View.VISIBLE
                    buttonRight.setImageResource(R.drawable.baseline_event_available_white_24)

                    labelButtonRight.visibility = View.VISIBLE
                    labelButtonRight.setText(R.string.registered_employment_ask)

                    //botón derecho habilitado
                    buttonRight?.setOnClickListener {
                        rightButtonAction("inscribir_empleo")
                    }
                }

                /**
                 * Funcionalidad existeInscripcion = true && reporteInscripcion != null
                 * Empleo Inscrito
                 * Se ha inscrito el empleo y no permite actualizaciones
                 * */
                if(existeInscripcion && reporteInscripcion != "null"){
                    Log.d("DEV", ">>>>>>>> Entró a empleo inscrito sin actualizar")
                    STEP_TAG = "empleo_inscrito_no_actualiza"
                    Log.d("DEV", "STEP TAG: " + STEP_TAG)

                    //LEFT AND RIGHT
                    //hide buttons
                    buttonLeft.visibility  = View.GONE

                    buttonRight.visibility = View.GONE
                    buttonRight.setImageResource(R.drawable.baseline_event_available_white_24)

                    labelButtonRight.visibility = View.GONE
                    labelButtonRight.setText(R.string.enroll_employment_button_title)

                    imageViewLeft.visibility = View.VISIBLE
                    imageViewLeft.setImageResource(R.drawable.baseline_event_available_white_24)

                    labelButtonLeft.visibility = View.VISIBLE
                    labelButtonLeft.setText(R.string.enroll_employment_ready)

                    //No actions
                }

                /**
                 * Funcionalidad existeInscripcion = true && reporteInscripcion != null && actualizar =  true
                 * Empleo Inscrito y ¿Actualizar?
                 * Se ha inscrito el empleo y permite actualizaciones de documentos
                 * */
                if(existeInscripcion && reporteInscripcion != "null" && actualizar){
                    Log.d("DEV", ">>>>>>>> Entró a empleo inscrito con opcion de actualizacion")
                    STEP_TAG = "empleo_inscrito_con_actualizacion"
                    Log.d("DEV", "STEP TAG: " + STEP_TAG)

                    //LEFT
                    //hide button
                    buttonLeft.visibility  = View.GONE

                    //LEFT
                    //show imageView and label
                    imageViewLeft.visibility = View.VISIBLE
                    imageViewLeft.setImageResource(R.drawable.baseline_event_available_white_24)

                    labelButtonLeft.visibility = View.VISIBLE
                    labelButtonLeft.setText(R.string.enroll_employment_ready)

                    //RIGHT
                    //show button and label
                    buttonRight.visibility = View.VISIBLE
                    buttonRight.setImageResource(R.drawable.baseline_restore_page_white_18)

                    labelButtonRight.visibility = View.VISIBLE
                    labelButtonRight.setText(R.string.update_documents)

                    //botón derecho habilitado
                    buttonRight?.setOnClickListener {
                        rightButtonAction(STEP_TAG)
                    }
                }

                /**
                 * Funcionalidad existeInscripcion = true && reporteInscripcion = null
                 * Ya existe un empleo inscrito en la misma convocatoria. No debe mostrar ninguno de los botones
                 * */
                if(existeInscripcion && reporteInscripcion == "null"){
                    //confirmprevInscriptions() //Quitar
                    //LEFT AND RIGHT
                    //hide buttons and labels
                    buttonRight.visibility = View.GONE
                    labelButtonRight.visibility = View.GONE
                    if(intent?.extras?.getString("status_inscription")=="I"){
                        buttonLeft.visibility  = View.GONE
                        imageViewLeft.visibility = View.VISIBLE
                        imageViewLeft.setImageResource(R.drawable.baseline_event_available_white_24)
                        labelButtonLeft.visibility = View.VISIBLE
                        labelButtonLeft.setText(R.string.enroll_employment_ready)
                    }else{
                        buttonLeft.visibility  = View.GONE
                        labelButtonLeft.visibility = View.GONE
                        if(intent?.extras?.getString("status_inscription")!="F"){
                            prevAprobPaymentDialog()
                        }
                    }
                }

                /*RestAPI.getJob_json(id!!, { json ->
                    var obje= JSONObject(json)
                    var accessjob = obje.getJSONObject("access")
                    var participantesAscensos= accessjob.getBoolean("verParticipantesAscensos")
                    Log.i("DEV","participantesAscensos : $participantesAscensos")
                    if(participantesAscensos){
                        buttonLeft.visibility  = View.GONE
                        labelButtonLeft.visibility = View.GONE

                        buttonRight.visibility = View.GONE
                        labelButtonRight.visibility = View.GONE
                    }
                }, { fuelError ->
                    showFuelError(fuelError)
                    Log.i("DEV","Error---> getJob_json")
                })*/

                /**
                 * Funcionalidad existeInscripcion = true && reporteInscripcion = null && pagarPSE = true
                 * Ya existe un empleo inscrito en la misma convocatoria, sin embargo se le informa al usuario
                 * que dicho empleo ha sido pagado
                 * */
                if(existeInscripcion && reporteInscripcion == "null" && pagarPSE){

                    //LEFT
                    //hide button and label
                    buttonLeft.visibility  = View.GONE
                    labelButtonLeft.visibility = View.GONE

                    //RIGHT
                    //hide button and label
                    buttonRight.visibility = View.GONE
                    labelButtonRight.visibility = View.GONE

                    //hide TestPlace
                    tests_card_view.visibility = View.GONE

                    //LEFT
                    //show imageView and label
                    imageViewLeft.visibility = View.VISIBLE
                    imageViewLeft.setImageResource(R.drawable.baseline_payment_white_18)

                    labelButtonLeft.visibility = View.VISIBLE
                    labelButtonLeft.setText(R.string.employment_payed)

                    //no actions
                    Toast.makeText(Context, "El empleo ha sido pagado, sin embargo ya existe una inscripción a otro empleo en la misma convocatoria.", Toast.LENGTH_LONG).show()
                }

                /**
                 * Funcionalidad existeInscripcion = true && reporteInscripcion = null && lugarPresentacion = true
                 * Ya existe un empleo inscrito en la misma convocatoria, sin embargo no se muestra el
                 * lugar de presentación de las pruebas
                 * */
                if(existeInscripcion && reporteInscripcion == "null" && lugarPresentacion){

                    //LEFT AND RIGHT
                    //hide buttons and labels
                    buttonLeft.visibility  = View.GONE
                    labelButtonLeft.visibility = View.GONE

                    buttonRight.visibility = View.GONE
                    labelButtonRight.visibility = View.GONE

                    //hide TestPlace
                    tests_card_view.visibility = View.GONE
                }

                /**
                 * Funcionalidad existeInscripcion = true && reporteInscripcion = null && lugarPresentacion = true
                 * Ya existe un empleo inscrito en la misma convocatoria, sin embargo se muestra el
                 * lugar de presentación de las pruebas
                 * */
                if(existeInscripcion && reporteInscripcion != "null" && lugarPresentacion){

                    //LEFT AND RIGHT
                    //hide buttons and labels
                    buttonLeft.visibility  = View.GONE
                    labelButtonLeft.visibility = View.VISIBLE

                    buttonRight.visibility = View.GONE
                    labelButtonRight.visibility = View.GONE

                }

            }, {fuelError ->

                RestAPI.getPreAvalible(idEmployment, { json ->
                    // La solicitud obtiene un objeto tipo Json, de la cual, requerimos sacar el valor de la llave
                    // Preinsc. que se encuentra dentro de la llave access
                    // ((json.array()[0] as JSONObject).get("access") as JSONObject).get("preinscribir")
                    if(json.content !="[]") {
                        if (((json.array()[0] as JSONObject).get("access") as JSONObject).get("preinscribir") as Boolean) {
                            buttonLeft.visibility = View.VISIBLE
                            buttonLeft.setImageResource(R.drawable.ic_favorite_border_24dp)
                            labelButtonLeft.visibility = View.VISIBLE
                            labelButtonLeft.setText(R.string.add_favorites)
                            buttonRight.visibility = View.VISIBLE
                            buttonRight.setImageResource(R.drawable.baseline_calendar_today_white_24)
                            labelButtonRight.visibility = View.VISIBLE
                            labelButtonRight.setText(R.string.confirm_employment_ask)
                            /**
                             * Funcionalidad de agregar o quitar favorito
                             * */
                            if (this.workOffer?.isFavoriteEnable == true) {
                                buttonLeft.visibility = View.VISIBLE
                                buttonRight.visibility = View.VISIBLE
                                if (this.workOffer?.favorite == true) {
                                    buttonLeft.setImageResource(R.drawable.ic_favorite_24dp)
                                    labelButtonLeft.setText(R.string.remove_favorites)
                                } else {
                                    buttonLeft.setImageResource(R.drawable.ic_favorite_border_24dp)
                                    labelButtonLeft.setText(R.string.add_favorites)
                                }
                            }

                            buttonRight?.setOnClickListener {
                                rightButtonAction(STEP_TAG)
                            }
                        }
                    }
                }, {fuelError ->

                }
                )
            })

            //ProgressBarDialog.startProgressDialog(this)
            RestAPI.getJobDueDate(idEmployment, { json ->
                //ProgressBarDialog.stopProgressDialog()
                if(json == "null"){

                    jobDueDate = "Por Definir"

                    //LEFT
                    //show button and label
                    buttonLeft.visibility  = View.VISIBLE
                    buttonLeft.setImageResource(R.drawable.ic_favorite_border_24dp)
                    labelButtonLeft.visibility = View.VISIBLE
                    labelButtonLeft.setText(R.string.add_favorites)

                    //hide imageView
                    imageViewLeft.visibility = View.GONE
                    imageViewLeft.setImageResource(R.drawable.baseline_event_note_white_24)


                    buttonRight.visibility = View.VISIBLE
                    buttonRight.setImageResource(R.drawable.baseline_calendar_today_white_24)

                    labelButtonRight.visibility = View.VISIBLE
                    labelButtonRight.setText(R.string.confirm_employment_ask)

                    //botón derecho visible pero deshabilitado
                    buttonRight.isEnabled = false
                    buttonRight.background.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray), PorterDuff.Mode.MULTIPLY)

                    Toast.makeText(this, R.string.convocatory_date_undefined, Toast.LENGTH_LONG).show()


                    /**
                     * Funcionalidad de agregar o quitar favorito
                     * */
                    if (this.workOffer?.isFavoriteEnable == true) {
                        Log.d("DEV", ">>>>>>>> Favoritos")

                        // Fecha de Cierre para el Detalle del Empleo en etapa de divulgación (fecha de inscripción Por Definir consultada desde Mis Empleos)
                        textCloseInscriptions.text = this.jobDueDate

                        //LEFT AND RIGHT
                        //show buttons
                        buttonLeft.visibility  = View.VISIBLE
                        buttonRight.visibility = View.VISIBLE

                        //botón derecho visible pero deshabilitado
                        buttonRight.isEnabled = false
                        buttonRight.background.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray), PorterDuff.Mode.MULTIPLY)

                        if (this.workOffer?.favorite == true) {


                            //LEFT
                            //show button and label Quitar
                            buttonLeft.setImageResource(R.drawable.ic_favorite_24dp)
                            labelButtonLeft.setText(R.string.remove_favorites)
                            //Toast.makeText(this, R.string.employment_deleted_from_favorites, Toast.LENGTH_SHORT).show()
                        }

                        else {


                            //LEFT
                            //show button and label Agregar
                            buttonLeft.setImageResource(R.drawable.ic_favorite_border_24dp)
                            labelButtonLeft.setText(R.string.add_favorites)
                            //Toast.makeText(this, R.string.employment_added_to_favorites, Toast.LENGTH_SHORT).show()
                        }
                    }

                } else {
                    var obj = JSONObject(json)
                    var isDateClosed : Boolean = utils.isConvocatoryClosed(obj.get("fechaFin").toString())!!
                    //var isDateOpened : Boolean = utils.isConvocatoryOpened(obj.get("fechaInicio").toString())!!

                    jobDueDate = obj.get("fechaFin").toString()!!


                    //if (!isDateClosed && isDateOpened) {
                    if(!isDateClosed){
                        //RIGHT
                        //show button background - habilita el botón

                        // Fecha de Cierre para el Detalle del Empleo en etapa de inscripciones (desde Mis Empleos, pestaña Favoritos)
                        textCloseInscriptions.text = this.jobDueDate

                        buttonRight.background.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.MULTIPLY)
                        buttonRight.isEnabled = true


                        //accessButtonAction()
                    } //else if (isDateClosed && isDateOpened) {
                    else {
                        Log.i("DEV","Fecha cerrada")
                        // Fecha de Cierre para el Detalle del Empleo con etapa de Inscripción finalizada (desde Mis Empleos, pestaña Inscritos)
                        textCloseInscriptions.text = this.jobDueDate

                        //LEFT
                        //hide buttons and labels
                        buttonLeft.visibility  = View.GONE
                        labelButtonLeft.visibility = View.GONE

                        buttonRight.visibility = View.GONE
                        labelButtonRight.visibility = View.GONE
                        Log.d("DEV","Fecha expirada")

                        //buttonRight.isEnabled = false
                        //buttonRight.background.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray), PorterDuff.Mode.MULTIPLY)

                        tests_card_view.visibility = View.GONE
                        //Toast.makeText(this, R.string.convocatory_date_ended_message, Toast.LENGTH_LONG).show()
                    }


                }
            }, {fuelError ->
                //to-do
                /*ProgressBarDialog.stopProgressDialog()
                showFuelError(fuelError)
                finish()*/
            })

            //ProgressBarDialog.startProgressDialog(this)
            RestAPI.getTotalCitizensEnrolled(id, { json ->
                //ProgressBarDialog.stopProgressDialog()
                var obj = JSONObject(json)
                totalCitizensEnrolled = obj.get("total").toString()!!
                rowTotalCitizensReg.value = totalCitizensEnrolled

            }, {fuelError ->
                /*ProgressBarDialog.stopProgressDialog()
                showFuelError(fuelError)
                finish()*/
            })
        }
    }


    /**
     * Pinta la ventana emergente que valida la selección del lugar de presentación para todas las pruebas (antes de proceder con el pago)
     */
    private fun payEmploymentDialog() {
        showConfirmDialog(this,
            R.string.select_city_for_test_dialog,
            R.string.before_pay_select_city_for_test_dialog,
            R.string.continue_button_dialog, { dialogInterface, which ->
            },
            R.string.cancel_button_dialog, { dialogInterface, which ->
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)}
        )
    }

    /**
     * Pinta la ventana emergente que valida si ya se cuenta con un pago confirmado (antes de proceder con el pago)
     */
    private fun prevAprobPaymentDialog() {
        SIMOApplication.showAlertDialog(this,
            R.string.prevpayment_dialog,
            R.string.before_prevpayment_dialog_test_dialog,
            R.string.accept_button_dialog, { dialogInterface, which ->
            }
        )
    }

    /*  private fun prevPendientePaymentDialog() {
          SIMOApplication.showAlertDialog(this,
              R.string.prevpayment_dialog,
              R.string.before_prevpayment2_dialog_test_dialog,
              R.string.accept_button_dialog, { dialogInterface, which ->
              }
          )
      }
  */

    /**
     * Muestra el CustomDialog
     */
    private fun showDialog(DeptoData: String){
        val customDialog         = LayoutInflater.from(Context).inflate(R.layout.layout_departmens_popup, null)
        var deptoTxt: TextView          = customDialog.textView3
        var deptoSpinner: Spinner       = customDialog.spinnerDepto
        var mcipioTxt: TextView         = customDialog.textView4
        var mpioSpinner: Spinner        = customDialog.spinner
        var confirmTestPlaceBtn: Button = customDialog.confirmar_lugar_btn
        var mensajeTxt: TextView        = customDialog.mensaje_txt
        var aceptarBtn: Button          = customDialog.aceptar_btn

        val arrayObj          = JSONArray(DeptoData)                                            // Obtengo el arrayObject
        var adapter           = DeptosAdapter(this, utils.generateDataDeptos(arrayObj))
        deptoSpinner.adapter  = adapter
        adapter.notifyDataSetChanged()

        val dialogBuilder = AlertDialog.Builder(Context)
            .setView(customDialog)
            .setTitle("Selección del lugar de pruebas")
        val alertDialog = dialogBuilder.show()

        deptoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onNothingSelected(parent: AdapterView<*>?) { }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                val item = deptoSpinner.adapter.getItemId(position).toString()

                //Toast.makeText(Context, "Depto id: " + item, Toast.LENGTH_SHORT).show()
                idDeptoAux = item


                RestAPI.getPresentationProvincies(idPruebaAux, item, { json ->

                    val arrayObj         = JSONArray(json)                                                // Obtengo el arrayObject
                    var adapter          = MncipiosAdapter(Context, utils.generateDataMncipios(arrayObj))
                    mpioSpinner.adapter  = adapter
                }, { fuelError ->
                    //to-do

                })
            }
        }

        mpioSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {  }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val item = mpioSpinner.adapter.getItemId(position).toString()
                //Toast.makeText(Context, "Mcipio id: " + item, Toast.LENGTH_SHORT).show()

                idMcipioAux = item
            }
        }

        confirmTestPlaceBtn.setOnClickListener { v ->
            deptoTxt.visibility            = View.INVISIBLE
            deptoSpinner.visibility        = View.INVISIBLE
            mcipioTxt.visibility           = View.INVISIBLE
            mpioSpinner.visibility         = View.INVISIBLE
            confirmTestPlaceBtn.visibility = View.INVISIBLE
            mensajeTxt.visibility          = View.VISIBLE
            aceptarBtn.visibility          = View.VISIBLE
            //Log.d("DEV", "Prueba : " + idPruebaAux + " Depto " + idDeptoAux + " Mcipio " + idMcipioAux, Toast.LENGTH_SHORT).show();

            //ProgressBarDialog.startProgressDialog(this)
            RestAPI.testPlaceRegistration(idInscripcionAux, idPruebaAux, idMcipioAux, {json ->
                //ProgressBarDialog.stopProgressDialog()
                Log.d("Dev", " Resultado inscripción lugar -> " + json)
                var obj = JSONObject(json)
                var objPrueba = JSONObject(obj.get("prueba").toString())
                var objMunicipio = JSONObject(obj.get("municipio").toString())
                mensajeTxt.text = "¡Registro exitoso! \n\nLugar de prueba: " + objMunicipio.get("nombre").toString() + " \n\nPrueba: " + objPrueba.get("descripcion").toString()
                idPruebaCiudadanoAux = obj.get("id").toString()
            }, { fuelError ->
                //Si se requiere rastrear un error, use un Log.d()
                /*ProgressBarDialog.stopProgressDialog()
                showFuelError(fuelError)
                finish()*/
            })
        }

        aceptarBtn.setOnClickListener {v ->
            alertDialog.hide()
            getTestsData(idInscripcionAux)
        }
    }

    /**
     * Dialog personalizado para mensajes a consideración
     */
    /*fun customDialog(Message: String, ButtonText: String){
        val builder = AlertDialog.Builder(Context)
        builder.setTitle("¡Aviso importante!")
        builder.setMessage(Message)
        builder.setCancelable(false)

        builder.setPositiveButton(ButtonText){dialog, which ->
            //Toast.makeText(applicationContext,"Ok, we change the app background.",Toast.LENGTH_SHORT).show()
            pagarPrueba();
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar"){dialog,which ->
            //Toast.makeText(applicationContext,"You are not agree.",Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }*/

    /**
     * Pinta la ventana emergente que confirma que el empleo ha sido preinscrito exitosamente
     * e invita al ciudadano a actualizar su hoja de vida o a continuar con el proceso de pago
     */
    private fun startPaymentTransferDialog() {
        showConfirmDialog(this, R.string.payment_transfer_conditions_title,
            R.string.payment_transfer_conditions, R.string.continue_button_dialog, { dialogInterface, which ->

                //intent
                val intent = Intent(Context, TransferPaymentActivity::class.java)
                intent.putExtra("idInscripcion", idInscripcionAux)
                intent.putExtra("idEmpleo", idEmployment)
                startActivity(intent)

            }, R.string.cancel_button_dialog, { dialogInterface, which ->

                //reloadTestData(idEmployment)


            })
    }

    private fun rightButtonAction(action : String){
        when(action){
            "confirmar"              -> confirmEmployment()
            "pagar"                  -> confirmPayment()
            "espera_respuesta_banco" -> Toast.makeText(Context, R.string.waiting_for_bank_response, Toast.LENGTH_LONG).show()
            "inscribir_empleo"       -> confirmInscription()
            "actualizar_docs"        -> updateDocuments()
            "evitar_seleccion_lugar" -> listView!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, i, id ->
                Toast.makeText(Context, "Usted ya no puede seleccionar o inscribir ningún lugar de pruebas.", Toast.LENGTH_LONG).show()
            }
            "empleo_inscrito_no_actualiza" -> ""
            "empleo_inscrito_con_actualizacion" -> updateDocuments()
            "cambiar_empleo" -> startPaymentTransferDialog()
        }
    }

    /**
     * Usa el endpoint serviciosMnobile/procesoInscripcion/${idInscripcion}/validarLugarPresentacio
     * ver metodo RestAPI.validateExistAnotherPayment()
     * para verificar si ya se han registrado las pruebas y lugares necesarios para contnuar con el
     * pago. El webservice responde `NULL si todo ha ido bien, de lo contrario responde con otro mensaje tipo
     * { "mensajeerror": "..."}`
     *
     * Posteriormente se valida lo siguiente:
     * Indica si existe un pago a otro empleo en la misma convocatoria,
     * Valida que no esté pronto el cierre del pago de derechos de inscripción
     */
    private fun confirmPayment(){
        //ProgressBarDialog.startProgressDialog(this)
        Log.d("DEV", ">>>>>>>> Ingresó por Confirmar Pago // Inscripción: $idInscripcionAux ")

        // Validar todas las pruebas hayan sido registradas correctamente
        // Si el valor retornado el null, quiere decir que el usuario ha inscrito las pruebas necesarias y podrá continuar
        // de lo contrario, debe seguir inscribiendo las pruebas hasta que el backend indique lo contrario.

        //confirmprevInscriptions()
        RestAPI.validateDefinedTestPlace(idInscripcionAux, { json ->
            validateExistTestPlace(json)
        }, { fuelError ->
            empty?.showConectionErrorState(fuelError){}
            /*ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
            finish()*/
            //Si se requiere rastrear un error, use un Log.d("err", fuelError.toString())
        })

    }

    /**Función que comprueba si existe un proceso de pago anterior aprobado o pendiente para evitar duplicidad en pagos
     *
     */
    /*
    private fun confirmprevInscriptions(){
        val idUser = SIMO.instance.session?.idUser
        request = RestAPI.getPayments(idUser, { convocatories ->
            var comprobe_var = "pass"
            for (pos in convocatories.indices){
                if(convocatories.get(pos).status.toString() =="APROBADA"){
                    comprobe_var = "APROBADA"
                    break
                }
                if(convocatories.get(pos).status.toString() =="PENDIENTE"){
                    comprobe_var = "PENDIENTE"
                    break
                }
            }/*
            if(comprobe_var=="APROBADA"){
                Log.i("DEV","------------- Existe un pago previo! :")
                prevAprobPaymentDialog()
            }else if (comprobe_var=="PENDIENTE"){
                Log.i("DEV","------------- Pendiente pago previo!")
                prevPendientePaymentDialog()
            }else{
                Log.i("DEV","------------- No existe un pago previo!")
                RestAPI.validateDefinedTestPlace(idInscripcionAux, { json ->
                    validateExistTestPlace(json)
                }, { fuelError ->
                    empty?.showConectionErrorState(fuelError){}
                    /*ProgressBarDialog.stopProgressDialog()
                    showFuelError(fuelError)
                    finish()*/
                    //Si se requiere rastrear un error, use un Log.d("err", fuelError.toString())
                })
            } */
        }, { fuelError ->
            empty?.showConectionErrorState(fuelError) {
            }
            //SIMOApplication.showFuelError(activity,fuelError)
        })

    }*/

    private fun validateExistTestPlace(existTestPlace : String){
        // Validación si existe un lugar de prueba registrado
        //Log.d("DEV", ">>>>>>>> Pruebas inscritas?: " + existTestPlace)
        if(existTestPlace == "null"){
            //customDialog("Se ha registrado el lugar de prueba exitosamente.\n\n¿Desea continuar con el resumen del pago?", "Aceptar")
            pagarPrueba()
        }
        // Validación si el registro de lugar de prueba NO SE HA REALIZADO
        if(existTestPlace != "null"){
            payEmploymentDialog()
        }
    }

    private fun confirmInscription(){
        //ProgressBarDialog.startProgressDialog(this)
        Log.d("DEV", ">>>>>>>> Ingresó por Confirmar Inscripción // Inscripción: $idInscripcionAux ")

        // Validar todas las pruebas hayan sido registradas correctamente
        // Si el valor retornado es null, quiere decir que el usuario ha inscrito las pruebas necesarias y podrá continuar
        // de lo contrario, debe seguir inscribiendo las pruebas hasta que el backend indique lo contrario.
        RestAPI.validateDefinedTestPlace(idInscripcionAux, { json ->
            //ProgressBarDialog.stopProgressDialog()
            validateExistTestPlaceForInscription(json)
        }, { fuelError ->
            /*ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
            finish()*/
            //Si se requiere rastrear un error, use un Log.d("err", fuelError.toString())
        })
    }

    private fun validateExistTestPlaceForInscription(existTestPlace : String){
        // Validación si existe un lugar de prueba registrado
        //Log.d("DEV", ">>>>>>>> Pruebas inscritas?: " + existTestPlace)
        if(existTestPlace == "null"){
            //customDialog("Se ha registrado el lugar de prueba exitosamente.\n\n¿Desea continuar con el resumen del pago?", "Aceptar")
            enrollEmployment()
        }
        // Validación si el registro de lugar de prueba NO SE HA REALIZADO
        if(existTestPlace != "null"){
            payEmploymentDialog()
        }
    }

    /**
     * Procesar pago PSE
     */
    private fun pagarPrueba(){
        val intent = Intent(Context, ResumePaymentActivity::class.java)
        intent.putExtra("idInscripcion", idInscripcionAux)
        intent.putExtra("idEmpleo", idEmployment)
        intent.putExtra("workOffer", workOffer)
        startActivity(intent)
    }

    /**
     * Inscribir empleo
     */
    private fun enrollEmployment(){
        val intent = Intent(Context, EnrollEmploymentActivity::class.java)
        intent.putExtra("idInscripcion", idInscripcionAux)
        intent.putExtra("idEmpleo", idEmployment)
        startActivity(intent)
    }


    /**
     * Pone el menú de la oferta laboral en la parte superior derecha
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.share, menu)
        return true
    }

    /**
     * Cuando se da tap sobre la opción del menú 'Compartir'
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.menu_share -> {
                this.intentShareTextLink(title = workOffer?.job?.denomination?.name,
                    titleChooser = getString(R.string.share),
                    errorShare = getString(R.string.error_to_share),
                    description = getMessageToShare(),
                    urlLink = "")
            }
            else -> return super.onOptionsItemSelected(item)

        }
        return true
    }

    /**
     * Construye el texto a compartir acerca de la oferta laboral
     */
    private fun getMessageToShare(): String {
        return getString(R.string.message_to_share,
            this.workOffer?.job?.denomination?.name,
            this.workOffer?.job?.convocatory?.name,
            this.workOffer?.job?.id,
            this.workOffer?.job?.gradeLevel?.levelName,
            this.workOffer?.job?.totalVancancies,
            this.workOffer?.job?.salary?.toDouble()?.toFormatCurrency(),
            this.jobDueDate)
    }

    /**
     * Si hay datos para ser devueltos a la pantalla de listado de ofertas laborales
     */
    private fun returnToBack() {
        val intent = Intent()
        setResult(RESULT_OK, intent)
    }

    /**
     * Finish method
     */
    override fun finish() {
        returnToBack()
        super.finish()
    }

    /**
     * Ejecución al comenzar una activity
     */
    override fun onStart() {
        super.onStart()
        Log.i("DEV","DetailWorkOfferA")
        AnalyticsReporter.screenJobDetail(this)
        //Log.d("DEV", "Hey I'm starting (1st time) this thing here :)")
        //customDialog("", "", false)
    }

    /**
     * Ejecución al reanudar una activity
     */
    /*override fun onResume() {
        super.onResume()
        //Log.d("DEV", "Hey I'm resuming this thing here :)")
        //customDialog("", "", false)
    }*/
}
