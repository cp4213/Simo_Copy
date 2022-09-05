package co.gov.cnsc.mobile.simo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.extensions.inflate
import co.gov.cnsc.mobile.simo.models.Alert
import kotlinx.android.synthetic.main.item_view_alert.view.*

/**
 * Esta clase contiene la funcionalidad para administrar un listado de alertas en un listview
 * @param context Contexto de la aplicación
 * @param dataSource listado de alertas a mostrar
 */
class AlertsAdapter(private val context: Context?, var dataSource: ArrayList<Alert>) : BaseAdapter() {

    lateinit var onArchive: (item: Alert, position: Int) -> Unit? //Lístener de la imagen archivar
    /**
     * Pinta los datos de cada alerta del listado en el layout "item_view_alert" (description es el parámetro clave)
     */
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = parent?.inflate(R.layout.item_view_alert)
        val item = getItem(position)
        if(!item.status.equals("Eliminada")) {
            rowView?.imagedelete?.setOnClickListener {
                onArchive(item, position)
            }
            if (item.access?.delete.equals("false")) {
                rowView?.imagedelete?.visibility = View.GONE
            } else {
                rowView?.imagedelete?.visibility = View.VISIBLE
                rowView?.imagedelete?.setImageResource(R.drawable.ic_close)
            }
            rowView?.rowDate?.value = item.notification?.dateSchedule
            rowView?.rowSubject?.value = item.notification?.subject
            val html = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(item.notification?.description, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(item.notification?.description)
            }
            if (item.isRead) {
                rowView?.containerAlert?.setBackgroundColor(Color.WHITE)
                rowView?.imageReadNotRead?.visibility = View.VISIBLE
                rowView?.imageReadNotRead?.setImageResource(R.drawable.ic_twotone_drafts_24px)
            } else {
                //rowView?.containerAlert?.setBackgroundColor(ContextCompat.getColor(context!!, R.color.gray_background))
                rowView?.containerAlert?.setBackgroundColor(Color.WHITE)
                rowView?.imageReadNotRead?.visibility = View.VISIBLE
                rowView?.imageReadNotRead?.setImageResource(R.drawable.ic_twotone_markunread_24px)
            }
            //rowView?.rowSeeDetails?.textViewLabel?.paintFlags = rowView?.rowSeeDetails?.textViewLabel?.paintFlags!! or Paint.UNDERLINE_TEXT_FLAG
            rowView?.rowSeeDetails?.textViewLabel?.paintFlags =
                rowView?.rowSeeDetails?.textViewLabel?.paintFlags!!
        }
        return rowView!!
    }

    /**
     * Obtiene una alerta en cierta posición
     */
    override fun getItem(position: Int): Alert {
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