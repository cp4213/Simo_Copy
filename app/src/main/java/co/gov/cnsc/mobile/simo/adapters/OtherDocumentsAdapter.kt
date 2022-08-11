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
import kotlinx.android.synthetic.main.item_view_other_document.view.*


/**
 * Esta clase contiene la funcionalidad para administrar un listado de 'otros documentos' en un listview
 * @param context Contexto de la aplicación
 * @param dataSource listado de 'otros documentos' a mostrar
 */
class OtherDocumentsAdapter(private val context: Context, private val dataSource: ArrayList<Credential>) : BaseAdapter() {

    /**
     * Evento cuando se da tap sobre la opción descargar soporte
     */
    lateinit var onDownloadListener: (item: Credential, position: Int) -> Unit?


    /**
     * Pinta los datos de cada 'otro documento' en un layout
     */
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = parent?.inflate(R.layout.item_view_other_document)
        val item = getItem(position)
        view?.rowDocumentType?.value = item.typeInformation?.name
        val content = SpannableString(context.getString(R.string.download))
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        view?.rowDownloadLink?.textViewLabel?.text = content
        view?.rowDownloadLink?.setOnClickListener {
            if (onDownloadListener != null)
                onDownloadListener(item, position)
        }
        return view!!
    }

    /**
     * Obtiene un 'otro documento' de cierta posición
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