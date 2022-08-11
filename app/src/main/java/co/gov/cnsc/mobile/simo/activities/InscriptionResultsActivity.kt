package co.gov.cnsc.mobile.simo.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.adapters.InscriptionResultsAdapter
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.extensions.toFormatCurrency
import co.gov.cnsc.mobile.simo.models.Job
import co.gov.cnsc.mobile.simo.models.TotalResult
import co.gov.cnsc.mobile.simo.models.WorkOffer
import co.gov.cnsc.mobile.simo.network.RestAPI
import kotlinx.android.synthetic.main.activity_detail_work_offer.*
import kotlinx.android.synthetic.main.activity_inscription_results.*
import kotlinx.android.synthetic.main.layout_content_app_bar_detail_work.*
import org.json.JSONObject

class InscriptionResultsActivity : SIMOActivity(), SwipeRefreshLayout.OnRefreshListener {

    var idJob: String? = null
    var idInscription: String? = null

    var job: Job? = null
    var workOffer: WorkOffer? = null
    var totalResult: TotalResult? = null

    var adapter: InscriptionResultsAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inscription_results)
        idJob = intent?.extras?.getString("id_job")
        idInscription = intent?.extras?.getString("id_inscription")
        showToolbarBack()
        setTextTitleToolbar(R.string.results, true)
        configureUI()
        getWorkOffer()
    }


    @SuppressLint("RestrictedApi")
    fun configureUI() {

        buttonLeft.visibility = View.GONE
        labelButtonLeft.visibility = View.GONE
        labelButtonRight.visibility = View.GONE
        imageEmptyState.visibility = View.INVISIBLE

        swipeRefresh?.setOnRefreshListener(this)

        adapter = InscriptionResultsAdapter(this)
        adapter?.listenerItemSelected = { inscriptionResult ->
            goToComplaintsAndTutelage(idInscription, inscriptionResult.idTest)
        }
        recyclerViewResults.adapter = adapter
    }

    private fun goToComplaintsAndTutelage(idInscription: String?, idTest: String?) {
        val intent = Intent(this, ComplaintsAndTutelageActivity::class.java)
        intent.putExtra("id_inscription", idInscription)
        intent.putExtra("id_test", idTest)
        startActivity(intent)
    }

    /**
     * Obtiene el detalle del trabajo desde el webservice y pinta los datos en pantalla
     */
    private fun getWorkOffer() {
        adapter?.clean()
        swipeRefresh?.isRefreshing = true
        RestAPI.getJob(idJob!!, { job ->
            this.job = job
            RestAPI.getJobDueDate(idJob!!, { json ->
                if(json == "null"){
                    textCloseInscriptions.text = "Por Definir"}
                else {
                    var obj = JSONObject(json)
                    textCloseInscriptions.text = obj.get("fechaFin").toString()!!
                }
                }, {fuelError ->
                    showFuelError(fuelError)
            })
            paint()
            getTotalResult()
        }) { fuelError ->
            swipeRefresh?.isRefreshing = false
            SIMOApplication.showErrorConectionConfirmToTry(this, {
                getWorkOffer()
            }, {
                finish()
            })
        }
    }

    /**
     * Obtiene el "Puntaje Total" y el "Estado" en el concurso
     */
    private fun getTotalResult() {
        RestAPI.getTotalResultInscription(idJob, idInscription, { totalResult ->
            this.totalResult = totalResult
            if (totalResult.id != "-1") {
                adapter?.addItemHeader(totalResult)
            }
            //getTestResults()
            getSummationScores()
        }, { fuelError ->
            swipeRefresh?.isRefreshing = false
            if (fuelError.response.statusCode == 404) {
                adapter?.addItemHeader(TotalResult(id = "-1", qualification = 0f, continueCompetition = false, status = null, place = null, classReclamation = null))
                //getTestResults()
                getSummationScores()
            } else {
                SIMOApplication.showErrorConectionConfirmToTry(this, {
                    getTotalResult()
                }, {
                    finish()
                })
            }
        })
    }

    /**
     * Obtiene la "Sumatoria de Puntajes" con Nombre de Prueba, Puntaje Aprobatorio, Resultado Parcial y Ponderación
     */
    private fun getSummationScores() {
        RestAPI.getSummationScoresInscription(idInscription, { summationScores ->
            swipeRefresh?.isRefreshing = false
            if (summationScores.isNotEmpty()) {
                adapter?.addItemSection(getString(R.string.summation_scores))
                adapter?.addItems(summationScores)
            }
            if (adapter?.itemCount == 0) {
                recyclerViewResults?.visibility = View.INVISIBLE
                textViewNoResults.visibility = View.VISIBLE
                imageEmptyState.visibility = View.VISIBLE
            } else if (adapter?.itemCount == 1 && adapter?.dataSource?.get(0) is TotalResult) {
                val total = adapter?.dataSource?.get(0) as TotalResult
                if (total.id == "-1") {
                    recyclerViewResults?.visibility = View.INVISIBLE
                    textViewNoResults.visibility = View.VISIBLE
                    imageEmptyState.visibility = View.VISIBLE
                } else {
                    recyclerViewResults?.visibility = View.VISIBLE
                    textViewNoResults.visibility = View.INVISIBLE
                    imageEmptyState.visibility = View.INVISIBLE
                }
            } else {
                recyclerViewResults?.visibility = View.VISIBLE
                textViewNoResults.visibility = View.INVISIBLE
            }
            getTestResults()
        }, { fuelError ->
            swipeRefresh?.isRefreshing = false
            SIMOApplication.showErrorConectionConfirmToTry(this, {
                getSummationScores()
            }, {
                finish()
            })
        })
    }

    /**
     * Obtiene el "Resultado de las Pruebas y Reclamaciones"
     */
    private fun getTestResults() {
        RestAPI.getTestResultsInscription(idInscription, { testResults ->
            if (testResults.isNotEmpty()) {
                adapter?.addItemSection(getString(R.string.results_of_test_ad_complaints))
                adapter?.addItems(testResults)
            }
            //getSummationScores()
            //getListOfEnrollment()
        }, { fuelError ->
            swipeRefresh?.isRefreshing = false
            SIMOApplication.showErrorConectionConfirmToTry(this, {
                getTestResults()
            }, {
                finish()
            })
        })
    }

    /**
     * Obtiene el "Listado de   Puntajes que continúan en concurso"
     */

    /*private fun getListOfEnrollment(){
        RestAPI.getListOfEnrollmentScores(idJob, { puntajesQueContinuan ->
            swipeRefresh?.isRefreshing = false
            if (puntajesQueContinuan.isNotEmpty()) {
                adapter?.addItemSection(getString(R.string.scores_list_s))
                //adapter?.addItemScoresList(puntajesQueContinuan)
            }
        }, { fuelError ->
            swipeRefresh?.isRefreshing = false
            SIMOApplication.showErrorConectionConfirmToTry(this, {
                getListOfEnrollment()
            }, {
                finish()
            })
        })
    }*/

    // copia de getListOfEnrollment con cambios

    /*private fun getListOfEnrollment() {
        RestAPI.getListOfEnrollmentScores(idJob, { puntajesQueContinuan ->
            swipeRefresh?.isRefreshing = false
            if (puntajesQueContinuan.isNotEmpty()) {
                adapter?.addItemSection(getString(R.string.scores_list_s))
                adapter?.addItemsScoresList(puntajesQueContinuan)
            }
            if (adapter?.itemCount == 0) {
                recyclerViewResults?.visibility = View.INVISIBLE
                textViewNoResults.visibility = View.VISIBLE
                imageEmptyState.visibility = View.VISIBLE
            } else if (adapter?.itemCount == 1 && adapter?.dataSource?.get(0) is TotalResult) {
                val scores = adapter?.dataSource?.get(0) as TotalResult
                if (scores.id == "-1") {
                    recyclerViewResults?.visibility = View.INVISIBLE
                    textViewNoResults.visibility = View.VISIBLE
                    imageEmptyState.visibility = View.VISIBLE
                } else {
                    recyclerViewResults?.visibility = View.VISIBLE
                    textViewNoResults.visibility = View.INVISIBLE
                    imageEmptyState.visibility = View.INVISIBLE
                }
            } else {
                recyclerViewResults?.visibility = View.VISIBLE
                textViewNoResults.visibility = View.INVISIBLE
            }
            //getTestResults()
        }, { fuelError ->
            swipeRefresh?.isRefreshing = false
            SIMOApplication.showErrorConectionConfirmToTry(this, {
                getListOfEnrollment()
            }, {
                finish()
            })
        })
    }*/


    /*Pinta el cabezote de resumen de "Empleo Inscrito" en la sección de Resultados*/
    private fun paint() {
        if (this.job != null) {
            textWorkOfferCharge?.text = job?.denomination?.name
            textConvocatory.text = job?.convocatory?.name
            textOpec.text = job?.id
            textLevel.text = job?.gradeLevel?.levelName
            textGrade.text = job?.gradeLevel?.grade
            textVacancies.text = job?.totalVancancies.toString()
            //textSalary.text = job?.salary.toString()
            textSalary.text = job?.salary?.toDouble()?.toFormatCurrency()
            textCloseInscriptions.text = workOffer?.dateInscription
           // rowCloseInscription.value      = this.workOffer?.dateInscription
        }
    }

    override fun onRefresh() {
        getWorkOffer()
    }

    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenResultsInscriptions(this)
        Log.d("DEV","InscriptionResultsA")
    }

}
