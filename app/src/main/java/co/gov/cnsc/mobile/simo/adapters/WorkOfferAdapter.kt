package co.gov.cnsc.mobile.simo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.extensions.inflate
import co.gov.cnsc.mobile.simo.extensions.toFormatCurrency
import co.gov.cnsc.mobile.simo.models.WorkOffer
import kotlinx.android.synthetic.main.item_view_work_offer.view.*

/**
 * Esta clase contiene la funcionalidad para administrar un listado de ofertas laborales en un listview
 * @param context Contexto de la aplicación
 * @param dataSource listado de ofertas a mostrar
 * @param showFavorite si se muestra o no el boton favoritos en el listado
 */
class WorkOfferAdapter(private val context: Context, private val dataSource: ArrayList<WorkOffer>, private val showFavorite: Boolean) : BaseAdapter() {

    /**
     * Cuando se da tap sobre la convocatoria en la oferta
     */
    lateinit var onConvocatoryListener: (workOffer: WorkOffer, position: Int) -> Unit?

    /**
     * Cuando se da tap sobre el botón favorito de la oferta
     */
    lateinit var onFavoriteListener: (workOffer: WorkOffer, position: Int) -> Unit?

    /**
     * Evento cuando se da tap sobre descargar manual de funciones
     */
    lateinit var onDownloadFunctionsListener: (workOffer: WorkOffer, position: Int) -> Unit?

    /**
     * Pinta los datos de cada oferta laboral en el layout "Listado OPEC"
     */
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = parent?.inflate(R.layout.item_view_work_offer)
        val item = getItem(position)
        rowView?.rowCharge?.value = item.job?.denomination?.name!!

        rowView?.rowConvocatory?.textViewValue?.text = item.job?.convocatory?.fullNameConvocatory
        //Estas dos lineas las adicioné para validar la posibilidad de incluir el link en el
        /*rowView?.rowConvocatory?.value = item.job?.convocatory?.name!!
        rowView?.rowConvocatory?.textViewValue?.text = item.job?.convocatory?.name*/
        rowView?.rowConvocatory?.textViewValue?.paintFlags = rowView?.rowConvocatory?.textViewLabel?.paintFlags!! or Paint.UNDERLINE_TEXT_FLAG
        rowView?.rowConvocatory?.setOnClickListener {
            onConvocatoryListener(item,position)
        }

        rowView?.rowOPEC?.value = item.job?.id.toString()
        rowView?.rowVancancy?.value = item.job?.totalVancancies.toString()
        rowView?.rowSalary?.value = item.job?.salary?.toDouble()?.toFormatCurrency()

        if (item.dateInscription != null && item.dateInscription.isNotBlank()) {
            rowView?.rowCloseInscription?.value = item.dateInscription
        } else {
            rowView?.rowCloseInscription?.value = context.getString(R.string.to_define)
        }

        /*val visibilityFav = if (showFavorite && item.isFavoriteEnable) {
            View.VISIBLE
        } else {
            View.GONE
        }*/


        /**
         * Muestra el link textual para descargar el manual de funciones del empleo (si existe)
         */
        if (item?.job?.document != null) {
            rowView?.rowAttachment?.textViewLabel?.paintFlags = rowView?.rowAttachment?.textViewValue?.paintFlags!! or Paint.UNDERLINE_TEXT_FLAG
            rowView?.rowAttachment?.visibility = View.VISIBLE
            //rowView?.rowAttachment?.value = item.job?.document?.id
            rowView?.rowAttachment?.setOnClickListener {
                onDownloadFunctionsListener(item,position)
            }
        } else {
            rowView?.rowAttachment?.visibility = View.GONE
        }




        //Pinta los íconos en layout Listado OPEC cuando trae los empleos encontrados

        if (item.isFavoriteEnable && item.favorite) {
            rowView?.imageProcessJob?.setImageResource(R.drawable.ic_twotone_favorite_24px)
        } else if (item.isPreInscriptionEnable) {
            rowView?.imageProcessJob?.setImageResource(R.drawable.ic_twotone_event_note_24px)
        } else if (item.isInscriptionEnable) {
            rowView?.imageProcessJob?.setImageResource(R.drawable.ic_twotone_event_available_24px)
        } else {
            rowView?.imageProcessJob?.setImageResource(R.drawable.ic_outline_explore_24px)
        }
        return rowView!!
    }

    /**
     * Obtiene una oferta laboral de cierta posición
     */
    override fun getItem(position: Int): WorkOffer {
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
     * Elimina todos los elementos del listado
     */
    fun clear() {
        dataSource.clear()
    }

    /**
     * Agrega un listado de items al listado
     */
    fun addItems(items: List<WorkOffer>?) {
        if (items != null && items.size > 0) {
            dataSource.addAll(items)
        }
        notifyDataSetChanged()
    }

}