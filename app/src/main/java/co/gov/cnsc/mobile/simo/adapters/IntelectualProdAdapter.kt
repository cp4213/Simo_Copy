package co.gov.cnsc.mobile.simo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.extensions.inflate
import co.gov.cnsc.mobile.simo.models.Credential
import kotlinx.android.synthetic.main.item_view_prod_intelectual.view.*

/**
 * Esta clase contiene la funcionalidad para administrar un listado de producciones intelectuales en un listview
 * @param context Contexto de la aplicación
 * @param dataSource listado de producciones intelectuales a mostrar
 */
class IntelectualProdAdapter(private val context: Context, private val dataSource: ArrayList<Credential>) : BaseAdapter() {

    /**
     * Evento cuando se da tap sobre la opción descargar soporte
     */
    lateinit var onDownloadListener: (item: Credential, position: Int) -> Unit?

    /**
     * Pinta los datos de cada produccion intelectual en un layout
     */
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = parent?.inflate(R.layout.item_view_prod_intelectual)
        val item = getItem(position)
        rowView?.rowTypeProduct?.value = item.typeProdIntelectual?.description
        rowView?.rowIdentifierNumber?.value = item.identifier
        rowView?.rowBibliograficCitation?.value = item.bibliographicRecord
        val content = SpannableString(context.getString(R.string.download))
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        rowView?.rowDownloadLink?.textViewLabel?.text = content
        rowView?.rowDownloadLink?.setOnClickListener {
            if (onDownloadListener != null)
                onDownloadListener(item, position)
        }
        return rowView!!
    }

    /**
     * Obtiene una produccion intelectual de cierta posición
     */
    override fun getItem(position: Int): Credential {
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