package co.gov.cnsc.mobile.simo.activities

import android.os.Bundle
import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.adapters.ComplaintsTutelageAdapter
import co.gov.cnsc.mobile.simo.network.RestAPI
import kotlinx.android.synthetic.main.activity_complaints_and_tutelage.*

/**
 * Esta activity contiene la funcionalidad de la pantalla de 'Quejas y Reclamos'
 */
class ComplaintsAndTutelageActivity : SIMOActivity(), SwipeRefreshLayout.OnRefreshListener {


    var adapter: ComplaintsTutelageAdapter? = null
    private var idInscription: String? = null
    private var idTest: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complaints_and_tutelage)
        showToolbarBack()
        setTextTitleToolbar(R.string.complaints_tutelage_exclusions, true)
        idInscription = intent?.extras?.getString("id_inscription")
        idTest = intent?.extras?.getString("id_test")
        configureUI()
        getComplaints()
    }

    /**
     * Configura los elementos UI del layout
     */
    private fun configureUI() {
        swipeRefresh?.setOnRefreshListener(this)
        adapter = ComplaintsTutelageAdapter(this)
        adapter?.listenerItemSelected = { complaintTutelage ->

        }
        recyclerViewComplaints?.adapter = adapter
    }

    /**
     * Obtiene el listado de quejas de una inscripción a través de un web service
     */
    private fun getComplaints() {
        swipeRefresh?.isRefreshing = true
        empty?.visibility = View.GONE
        RestAPI.getComplaintsInscription(idInscription, idTest, { complaints ->
            swipeRefresh?.isRefreshing = false
            adapter?.clean()
            if (complaints.size == 0) {
                empty?.visibility = View.VISIBLE
                empty?.showEmptyState()
            } else {
                empty?.visibility = View.GONE
                adapter?.addItems(complaints)
            }
        }, { fuelError ->
            swipeRefresh?.isRefreshing = false
            adapter?.clean()
            empty?.visibility = View.VISIBLE
            empty?.showConectionErrorState {
                getComplaints()
            }
        })
    }

    /**
     * Cuando se da la acción swipe refresh para actualizar la información
     */
    override fun onRefresh() {
        getComplaints()
    }
}
