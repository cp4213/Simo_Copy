package co.gov.cnsc.mobile.simo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.extensions.inflate
import co.gov.cnsc.mobile.simo.models.Inscription
import kotlinx.android.synthetic.main.item_view_inscription.view.*

/**
 * Esta clase contiene la funcionalidad para administrar un listado de trabajos inscritos, preincritos o favoritos en un listview
 * @param context Contexto de la aplicación
 * @param dataSource listado de Trabajos de inscritos, preincritos o favoritos
 */
class MyInscriptionsAdapter(private val context: Context, var dataSource: ArrayList<Inscription>) : BaseAdapter() {

    /**
     * Evento cuando se da tap sobre la convocatoria
     */
    lateinit var onConvocatoryListener: (inscription: Inscription, position: Int) -> Unit?

    /**
     * Evento cuando se da tap sobre la opción ver resultados
     */
    lateinit var onQueryResults: (inscription: Inscription, position: Int) -> Unit?

    /**
     * Evento cuando se da tap sobre descargar constancia
     */
    lateinit var onDownloadConstancyListener: (inscription: Inscription, position: Int) -> Unit?

    /**
     * Evento cuando se da tap sobre el botón de favorito
     */
    lateinit var onFavoriteListener: (inscription: Inscription, position: Int) -> Unit?

    /**
     * Evento cuando se da tap sobre alguno de los empleos
     */
    lateinit var onItemListener: (inscription: Inscription, position: Int) -> Unit?


    /**
     * Pinta los datos de cada trabajo en un layout
     */
    @SuppressLint("ViewHolder", "RestrictedApi")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val rowView = parent?.inflate(R.layout.item_view_inscription)
        val item = getItem(position)
        rowView?.rowCharge?.label = item.denominationName

        rowView?.rowConvocatory?.textViewValue?.text = item.convocatoryName
        rowView?.rowConvocatory?.textViewValue?.paintFlags = rowView?.rowConvocatory?.textViewLabel?.paintFlags!! or Paint.UNDERLINE_TEXT_FLAG
        rowView?.rowConvocatory?.textViewValue?.setOnClickListener {
            onConvocatoryListener(item, position)
        }

        rowView?.containerInscription?.setOnClickListener {
            onItemListener(item, position)
        }
        rowView?.rowEntity?.value = item.entityName
        rowView?.rowOPEC?.value = item.jobId
        rowView?.rowTotalInscriptions?.value = item.totalPeople?.toString()
        if (item.isFavorite) {
            rowView?.imageIconDecorative?.visibility = View.GONE
            rowView?.rowLinkResults?.visibility = View.GONE
            rowView?.rowLinkDownloadInscription?.visibility = View.GONE
            rowView?.buttonAction?.visibility = View.VISIBLE
            rowView?.buttonAction?.setImageResource(R.drawable.ic_favorite_24dp)
            rowView?.buttonAction?.setOnClickListener {
                onFavoriteListener(item, position)
            }
            rowView?.textViewAction?.visibility = View.VISIBLE
            rowView?.textViewAction?.setText(R.string.remove_favorites_ask)
        } else if (item.isPreApplicant) {
            rowView?.imageIconDecorative?.visibility = View.VISIBLE
            rowView?.imageIconDecorative?.setImageResource(R.drawable.ic_event_note_black_24dp)
            rowView?.rowLinkResults?.visibility = View.GONE
            rowView?.rowLinkDownloadInscription?.visibility = View.GONE
            rowView?.buttonAction?.visibility = View.GONE
            rowView?.textViewAction?.visibility = View.GONE
        } else if (item.isApplicant) {
            rowView?.imageIconDecorative?.visibility = View.VISIBLE
            rowView?.imageIconDecorative?.setImageResource(R.drawable.ic_event_available_black_24dp)
            rowView?.rowLinkResults?.visibility = View.VISIBLE
            //rowView?.rowLinkResults?.textViewLabel?.paintFlags = rowView?.rowConvocatory?.textViewLabel?.paintFlags!!
            rowView?.rowLinkResults?.setOnClickListener {
                onQueryResults(item, position)
            }
            rowView?.rowLinkDownloadInscription?.visibility = View.VISIBLE
            //rowView?.rowLinkDownloadInscription?.textViewLabel?.paintFlags = rowView?.rowConvocatory?.textViewLabel?.paintFlags!! or Paint.UNDERLINE_TEXT_FLAG
            //rowView?.rowLinkDownloadInscription?.textViewLabel?.paintFlags = rowView?.rowConvocatory?.textViewLabel?.paintFlags!!
            rowView?.rowLinkDownloadInscription?.setOnClickListener {
                onDownloadConstancyListener(item, position)
            }
            rowView?.buttonAction?.visibility = View.GONE
            rowView?.textViewAction?.visibility = View.GONE
        }
        return rowView
    }

    /**
     * Obtiene un trabajo de cierta posición
     */
    override fun getItem(position: Int): Inscription {
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
     * Quita un item de la lista
     */
    fun remove(position: Int?) {
        position?.let {
            dataSource.removeAt(position)
            notifyDataSetChanged()
        }
    }

}