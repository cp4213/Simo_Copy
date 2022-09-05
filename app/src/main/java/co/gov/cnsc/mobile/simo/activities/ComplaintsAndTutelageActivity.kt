package co.gov.cnsc.mobile.simo.activities

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.adapters.AnswerAdapter
import co.gov.cnsc.mobile.simo.adapters.ComplaintsTutelageAdapter
import co.gov.cnsc.mobile.simo.databinding.ActivityComplaintsAndTutelageBinding
import co.gov.cnsc.mobile.simo.models.*
import co.gov.cnsc.mobile.simo.network.RestAPI
import kotlinx.android.synthetic.main.activity_complaints_and_tutelage.*
import kotlinx.android.synthetic.main.activity_complaints_and_tutelage.swipeRefresh
import kotlinx.android.synthetic.main.layout_asnwer_resume.view.*
import java.util.ArrayList

/**
 * Esta activity contiene la funcionalidad de la pantalla de 'Quejas y Reclamos'
 */
class ComplaintsAndTutelageActivity : SIMOActivity(), SwipeRefreshLayout.OnRefreshListener {


    var adapter: ComplaintsTutelageAdapter? = null
    private var idInscription: String? = null
    private var idTest: String? = null
    private lateinit var binding: ActivityComplaintsAndTutelageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityComplaintsAndTutelageBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

        adapter?.onAnswer ={item: ComplaintTutelage ->
            Answer(item.id!!)
        }
        recyclerViewComplaints?.adapter = adapter
    }

    private fun Answer(id:String) {

        //debería inflar
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_asnwer_resume,null)
        builder.setView(view)
        val viewer =builder.create()
        var adapterb: AnswerAdapter? = null

        view.swipeRefreshinflated?.isRefreshing = true
        RestAPI.getComplaintsResponses(id,{ result ->
            view.swipeRefreshinflated?.isRefreshing = false
            if (result.size>0){
            adapterb= AnswerAdapter(this,result[0].attachments as ArrayList<Document>)
                view.TextViewDetailAnswer.text=result[0].detail
                view.ListViewAnswer.adapter=adapterb
                adapterb?.onDocument={ item: Document-> //Listener de archivar una alerta
                    onDocument(item)
                }
                viewer.show()
            }else{
                Toast.makeText(this,R.string.no_answer,Toast.LENGTH_SHORT).show()
            }

        }, { fuelError ->
            view.swipeRefreshinflated?.isRefreshing = false
            viewer.hide()
        })


    }

    private fun onDocument(item: Document) {
        if (item.id != null) {
            SIMOApplication.checkIfConnectedByData(this) {
                SIMOApplication.checkPermissionsAndDownloadFile(this, "${RestAPI.HOST}/documents/get-document?docId=${item.id}",
                    "${item.id}.pdf")
            }
        }else{
            if(item.id == null)
                Toast.makeText(this,"No existe documento del reporte",Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * Obtiene el listado de quejas de una inscripción a través de un web service
     */
    private fun getComplaints() {
        swipeRefresh?.isRefreshing = true
        binding.empty?.visibility = View.GONE
        RestAPI.getComplaintsInscription(idInscription, idTest, { complaints ->
            swipeRefresh?.isRefreshing = false
            adapter?.clean()
            if (complaints.size == 0) {
                binding.empty?.visibility = View.VISIBLE
                binding.empty?.showEmptyState()
            } else {
                binding.empty?.visibility = View.GONE
                adapter?.addItems(complaints)
            }
        }, { fuelError ->
            swipeRefresh?.isRefreshing = false
            adapter?.clean()
            binding.empty?.visibility = View.VISIBLE
            binding.empty?.showConectionErrorState {
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
