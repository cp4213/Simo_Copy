package co.gov.cnsc.mobile.simo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.extensions.inflate
import co.gov.cnsc.mobile.simo.models.AudienceVacancy
import kotlinx.android.synthetic.main.item_view_vacancies_selected.view.*

class AudienceVacancyAdapter (private val context: Context?, var dataSource: ArrayList<AudienceVacancy>) : BaseAdapter(){

    /**
     * Evento cuando se da tap sobre ver empleo
     */
    lateinit var onVacancyclick: (item: AudienceVacancy, position: Int) -> Unit?

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): AudienceVacancy {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = parent?.inflate(R.layout.item_view_vacancies_selected)
        val item = getItem(position)
        rowView?.rowOpec?.value=item.numeroOPEC
        rowView?.rowVacancyNumber?.value=item.idVacante
        rowView?.rowPlazasnumber?.value=item.numeroPlazas
        rowView?.rowDependence?.value=item.dependencia
        rowView?.rowDenomination?.value=item.denominacion
        rowView?.rowDepartament?.value=item.departamento
        rowView?.rowCity?.value=item.municipio
        rowView?.rowEmploy?.value=item.descripcionEmpleo
        rowView?.rowEmployConsult?.setOnClickListener {
            onVacancyclick(item, position)
        }
        return rowView!!
    }
}