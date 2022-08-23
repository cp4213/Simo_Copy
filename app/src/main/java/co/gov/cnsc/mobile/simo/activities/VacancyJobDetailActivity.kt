package co.gov.cnsc.mobile.simo.activities

import android.os.Bundle
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.models.Job
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import kotlinx.android.synthetic.main.activity_vacancy_detail.*
import kotlinx.android.synthetic.main.layout_content_vacancy_job.*


class VacancyJobDetailActivity: SIMOActivity() {

    //private var job: Job?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vacancy_detail)
        showToolbarBack()
        setTextTitleToolbar(R.string.vacancy_detail)
        GetJob()

    }

    private fun GetJob() {
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.getJob(intent?.extras?.getString("job_OPEC")!!, { job ->
            ProgressBarDialog.stopProgressDialog()
            ConfigureUI(job)
        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            showFuelError(fuelError)
            finish()
        })
    }

    private fun ConfigureUI(job: Job) {
        textCode.text=job?.codeJob
        textLevel.text=job?.gradeLevel?.levelName
        textGrade.text=job?.gradeLevel?.grade
        textVacancies.text=job?.vacancies?.size.toString()
        textArea.text=job?.area
        textSalary.text=job?.salary.toString()
        rowPurpose.value=job?.description
        rowVacancy.value=job?.allVacancies
        rowFunctions.value=job?.allFunctions
    }

}