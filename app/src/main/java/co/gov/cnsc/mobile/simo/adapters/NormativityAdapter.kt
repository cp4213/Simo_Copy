package co.gov.cnsc.mobile.simo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.extensions.inflate
import co.gov.cnsc.mobile.simo.models.Normativity
import kotlinx.android.synthetic.main.item_view_normativity.view.*


/**
 * Esta clase contiene la funcionalidad para administrar un listado de normas en un listview
 * @param context Contexto de la aplicación
 * @param dataSource listado de normas a mostrar
 */
class NormativityAdapter(private val context: Context, private val dataSource: ArrayList<Normativity>) : BaseAdapter() {

    /**
     * Evento cuando se da tap sobre Descarga soporte
     */
    lateinit var onAttachmentListener: (item: Normativity, position: Int) -> Unit?

    /**
     * Pinta los datos de una normatividad en un layout
     */
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = parent?.inflate(R.layout.item_view_normativity)
        val item = getItem(position)
        if (item.number != null)
            rowView?.rowNumber?.value = item.number
        if (item.date != null)
            rowView?.rowDate?.value = item.date
        if (item.description != null)
            rowView?.rowDescription?.value = item.description
        if (item.document.name != null) {
            rowView?.rowAttachment?.textViewLabel?.paintFlags = rowView?.rowAttachment?.textViewValue?.paintFlags!! or Paint.UNDERLINE_TEXT_FLAG
            rowView.rowAttachment?.setOnClickListener {
                onAttachmentListener(item, position)
            }
        }
        return rowView!!
    }

    /**
     * Obtiene una normatividad en cierta posición
     */
    override fun getItem(position: Int): Normativity {
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

    /**
     * Agrega items nuevos al listado
     */
    fun addItems(items: List<Normativity>?) {
        if (items != null)
            dataSource.addAll(items)
        notifyDataSetChanged()
    }
}