package co.gov.cnsc.mobile.simo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings.Global.getString
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.extensions.inflate
import co.gov.cnsc.mobile.simo.models.Document
import kotlinx.android.synthetic.main.item_view_answer.view.*
import java.util.*

class AnswerAdapter (private val context: Context?, var dataSource: ArrayList<Document>) : BaseAdapter() {

    lateinit var onDocument: (item: Document) -> Unit? //Lístener del documento a descargar
    /**
     * Pinta los datos de cada respuesta del listado en el layout "item_view_answer" (
     */
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = parent?.inflate(R.layout.item_view_answer)
        val item = getItem(position)
        rowView?.rowDetailAnswer?.value= item.name
        if(!item?.pathFile.isNullOrBlank())
            rowView?.rowDownloadAnswer?.setOnClickListener { onDocument(item)
            }
        return rowView!!
    }

    /**
     * Obtiene una alerta en cierta posición
     */
    override fun getItem(position: Int): Document {
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