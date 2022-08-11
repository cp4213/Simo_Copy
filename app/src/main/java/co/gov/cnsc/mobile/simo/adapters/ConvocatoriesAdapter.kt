package co.gov.cnsc.mobile.simo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.extensions.inflate
import co.gov.cnsc.mobile.simo.models.Convocatory
import kotlinx.android.synthetic.main.item_view_convocatory.view.*

/**
 * Esta clase contiene la funcionalidad para administrar un listado de convocatorias en un listview
 * @param context Contexto de la aplicación
 * @param dataSource listado de alertas a mostrar
 */
class ConvocatoriesAdapter(private val context: Context?, var dataSource: ArrayList<Convocatory>) : BaseAdapter() {

    /**
     * Evento cuando se da tap sobre la opción ver ofertas de esta convocatoria
     */
    lateinit var onSeeJobsListener: (item: Convocatory, position: Int) -> Unit?

    /**
     * Pinta los datos de cada convocatoria en un layout
     */
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = parent?.inflate(R.layout.item_view_convocatory)
        val item = getItem(position)
        rowView?.textViewTitle?.text = item.name
        rowView?.rowSeeJobs?.visibilityDrawableLeft = View.GONE
        //rowView?.rowSeeJobs?.textViewLabel?.paintFlags = rowView?.rowSeeJobs?.textViewLabel?.paintFlags!! or Paint.UNDERLINE_TEXT_FLAG
        rowView?.rowSeeJobs?.textViewLabel?.paintFlags = rowView?.rowSeeJobs?.textViewLabel?.paintFlags!!
        //rowView.rowSeeDetails?.textViewLabel?.paintFlags = rowView.rowSeeDetails?.textViewLabel?.paintFlags!! or Paint.UNDERLINE_TEXT_FLAG
        rowView.rowSeeDetails?.textViewLabel?.paintFlags = rowView.rowSeeDetails?.textViewLabel?.paintFlags!!
        rowView.rowSeeDetails?.visibilityDrawableLeft = View.GONE
        rowView.rowSeeJobs?.setOnClickListener {
            onSeeJobsListener(item, position)
        }
        return rowView
    }

    /**
     * Obtiene una convocatoria de cierta posición
     */
    override fun getItem(position: Int): Convocatory {
        return dataSource[position]
    }

    /**
     * Obtiene el id de un item
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * Obtiene el número de elementos en la lista
     */
    override fun getCount(): Int {
        return dataSource.size
    }
}