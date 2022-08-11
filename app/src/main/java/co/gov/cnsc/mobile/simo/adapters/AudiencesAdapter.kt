package co.gov.cnsc.mobile.simo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.extensions.inflate
import co.gov.cnsc.mobile.simo.models.Audience
import kotlinx.android.synthetic.main.item_view_audiences.view.*

class AudiencesAdapter(private val context: Context?, var dataSource: ArrayList<Audience>) : BaseAdapter() {

    /**
     * Evento cuando se da tap sobre la opción ver ofertas de esta convocatoria
     */
    lateinit var onConsultJob: (item: Audience, position: Int) -> Unit?
    lateinit var setPrioriry: (item: Audience, position: Int) -> Unit?
    lateinit var reportConsult: (item: Audience, position: Int) -> Unit?

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Audience {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = parent?.inflate(R.layout.item_view_audiences)
        val item = getItem(position)
        rowView?.rowAudienceName?.value=item.name
        rowView?.rowS_P_Name?.value=item.selectionProces
        rowView?.rowDateIni?.value=item.dateIni
        rowView?.rowDate_Termination?.value=item.dateEnd
        rowView?.rowjob_colsult?.setOnClickListener {
            onConsultJob(item, position)
        }
        rowView?.rowsetPriority?.setOnClickListener {
            setPrioriry(item, position)
        }
        rowView?.rowReport?.setOnClickListener {
            reportConsult(item, position)
        }

        //Aquí van todas las audiencias

        //rowView?.rowAudienceName?.value=item.id
        return rowView!!
    }
}