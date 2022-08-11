package co.gov.cnsc.mobile.simo.activities

import android.os.Bundle
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity

class VacancyJobDetailActivity: SIMOActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vacancy_detail)
        showToolbarBack()
        setTextTitleToolbar(R.string.vacancy_detail)
    }
}