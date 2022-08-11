package co.gov.cnsc.mobile.simo.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.models.Audience
import com.applandeo.materialcalendarview.CalendarView
import kotlinx.android.synthetic.main.activity_audience_jobs_vacancy.*
import java.util.*

class AudienceVacancyJobs:SIMOActivity() {

    private var audience:Audience?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audience_jobs_vacancy)
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

        //Adapter del ListView
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
        builder.setView(view)
        val viewer =builder.create()
        viewer.show()
    }


}