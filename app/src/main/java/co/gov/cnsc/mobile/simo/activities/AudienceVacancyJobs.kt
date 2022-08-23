package co.gov.cnsc.mobile.simo.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.adapters.AudienceVacancyAdapter
import co.gov.cnsc.mobile.simo.databinding.ActivityAudienceJobsVacancyBinding
import co.gov.cnsc.mobile.simo.databinding.ActivityWorkOffersBinding
import co.gov.cnsc.mobile.simo.models.Audience
import co.gov.cnsc.mobile.simo.models.AudienceVacancy
import co.gov.cnsc.mobile.simo.models.IdName
import co.gov.cnsc.mobile.simo.network.RestAPI
import com.applandeo.materialcalendarview.CalendarView
import com.github.kittinunf.fuel.core.Request
import kotlinx.android.synthetic.main.activity_audience_jobs_vacancy.*
import kotlinx.android.synthetic.main.fragment_my_alerts.*
import kotlinx.android.synthetic.main.layout_avalible_vacancy_search.view.*
import java.util.*

class AudienceVacancyJobs:SIMOActivity() {

    private var audience:Audience?=null
    private var adapter: AudienceVacancyAdapter? = null
    /**
     * Request que se realiza al servidor para traer el listado de convocatorias
     */
    private var request: Request? = null
    private lateinit var binding: ActivityAudienceJobsVacancyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudienceJobsVacancyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_audience_jobs_vacancy)
        showToolbarBack()
        setTextTitleToolbar(R.string.employs_audience)
        audience=intent?.extras?.getParcelable("audience")
        configureUI()
    }

    private fun configureUI() {
        textViewAudienceName.text=getString(R.string.audience_name_f,audience?.name)
        textViewS_P_Name.text=getString(R.string.S_P_Name,audience?.selectionProces)
        textViewInitDate.text=getString(R.string.initial_date_f,audience?.dateIni)
        textViewFinalDate.text=getString(R.string.final_date_f,audience?.dateEnd)
        getAudienceVacancy(audience?.id!!)
        adapter = AudienceVacancyAdapter(this, ArrayList<AudienceVacancy>())
        adapter?.onVacancyclick = { item: AudienceVacancy, position: Int ->
            //Toast.makeText(this,"onVacancyclick", Toast.LENGTH_SHORT).show()
            //goToVacancyJobs(item)
            //SIMOApplication.goToWorkOffers(context = context!!, convocatory = item)
            goToEmploy(item)
        }
    }

    private fun goToEmploy(item: AudienceVacancy) {
        val intent = Intent(this, VacancyJobDetailActivity::class.java)
        intent.putExtra("job_OPEC",item.numeroOPEC)
        intent.putExtra("job_id",item.id)
        startActivity(intent)
    }

    private fun getAudienceVacancy(id:String) {
        swipeRefresh?.isRefreshing = true
        request = RestAPI.getVacancyAudiences(id,{ vacancy ->
            swipeRefresh?.isRefreshing = false
            binding.empty?.showEmptyState()
            adapter?.dataSource = vacancy as ArrayList<AudienceVacancy>
            vacancySelected?.adapter = adapter
            adapter?.notifyDataSetChanged()
        }, { fuelError ->
            swipeRefresh?.isRefreshing = false
            binding.empty?.showConectionErrorState(fuelError) {
                getAudienceVacancy(audience?.id!!)
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.advanced_search, menu)
        return true
    }

    /**
     * Administra las opciones del menú en caso de ser seleccionadas
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.menu_search -> {
                goToAdvancedSearch()
            }
            else -> return super.onOptionsItemSelected(item)

        }
        return true
    }

    private fun goToAdvancedSearch() {
        //debería inflar
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_avalible_vacancy_search,null)
        //view.spinnerDependencia=a
        builder.setView(view)
        val viewer =builder.create()
        var adapterb= ArrayAdapter<IdName>(this, android.R.layout.simple_spinner_dropdown_item)
        var adapterc= ArrayAdapter<IdName>(this, android.R.layout.simple_spinner_dropdown_item)


        swipeRefresh?.isRefreshing = true
        request = RestAPI.getVacancyDependencies(audience!!.id,{ result ->
            swipeRefresh?.isRefreshing = false
            adapterb.addAll(result)
            view.spinnerDependencia.adapter=adapterb
        }, { fuelError ->
            swipeRefresh?.isRefreshing = false

        })
        request = RestAPI.getVacancyCities(audience!!.id,{ result ->
            swipeRefresh?.isRefreshing = false
            adapterc.addAll(result)
            view.spinnerCity.adapter=adapterc
        }, { fuelError ->
            swipeRefresh?.isRefreshing = false
        })


        view.buttonFind.setOnClickListener {
            if (!view.editTextVacancy.text.isNullOrBlank()){
                request = RestAPI.getVacancySearch(audience!!.id,
                    (view.spinnerDependencia.selectedItem as IdName).id,
                    (view.spinnerCity.selectedItem as IdName).id,
                    view.editTextVacancy.text.toString(),{ vacancy ->
                        swipeRefresh?.isRefreshing = false
                        binding.empty?.showEmptyState()
                        adapter?.dataSource = vacancy as ArrayList<AudienceVacancy>
                        vacancySelected?.adapter = adapter
                        adapter?.notifyDataSetChanged()
                        viewer.hide()
                    }, { fuelError ->
                        swipeRefresh?.isRefreshing = false
                        binding.empty?.showConectionErrorState(fuelError) {
                            getAudienceVacancy(audience?.id!!)

                        }
                        viewer.hide()
                    })
            }else{
                request = RestAPI.getVacancySearch(audience!!.id,
                    (view.spinnerDependencia.selectedItem as IdName).id,
                    (view.spinnerCity.selectedItem as IdName).id,{ vacancy ->
                        swipeRefresh?.isRefreshing = false
                        binding.empty?.showEmptyState()
                        adapter?.dataSource = vacancy as ArrayList<AudienceVacancy>
                        vacancySelected?.adapter = adapter
                        adapter?.notifyDataSetChanged()
                        viewer.hide()
                    }, { fuelError ->
                        swipeRefresh?.isRefreshing = false
                        binding.empty?.showConectionErrorState(fuelError) {
                            getAudienceVacancy(audience?.id!!)

                        }
                        viewer.hide()
                    })
            }

        }


        view.buttonClean.setOnClickListener {
            view.spinnerDependencia.setSelection(0)
            view.spinnerCity.setSelection(0)
            view.editTextVacancy.text = null
            request = RestAPI.getVacancyAudiences(audience!!.id,{ vacancy ->
                swipeRefresh?.isRefreshing = false
                binding.empty?.showEmptyState()
                adapter?.dataSource = vacancy as ArrayList<AudienceVacancy>
                vacancySelected?.adapter = adapter
                adapter?.notifyDataSetChanged()
                viewer.hide()
            }, { fuelError ->
                swipeRefresh?.isRefreshing = false
                binding.empty?.showConectionErrorState(fuelError) {
                    getAudienceVacancy(audience?.id!!)
                }
                viewer.hide()
            })
        }
        viewer.show()

    }


}