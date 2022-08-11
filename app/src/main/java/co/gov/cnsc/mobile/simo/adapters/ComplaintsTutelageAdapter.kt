package co.gov.cnsc.mobile.simo.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.extensions.inflate
import co.gov.cnsc.mobile.simo.models.ComplaintTutelage
import kotlinx.android.synthetic.main.item_view_complaint_tutelage.view.*

/**
 * Esta clase contiene la funcionalidad para administrar un listado de quejas y reclamos en un recyclerview
 * @param context Contexto de la aplicación
 */
class ComplaintsTutelageAdapter(private val context: Context?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /**
     * Listener cuando un item de la lista es seleccionado
     */
    var listenerItemSelected: ((ComplaintTutelage) -> Unit)? = null

    /**
     * Listado de quejas y/o reclamos
     */
    private var dataSource = mutableListOf<ComplaintTutelage>()


    /**
     * Crea un item ui apartir de un layout
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(parent.inflate(R.layout.item_view_complaint_tutelage))
    }

    /**
     * Obtiene el número de elementos en el listado
     */
    override fun getItemCount(): Int {
        return dataSource.size
    }

    /**
     * Pinta la información en el layout
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val headerHolder = holder as ViewHolder
        headerHolder.bind(dataSource[position])
    }

    /**
     * Agrega items al listado de quejas
     */
    fun addItems(items: List<ComplaintTutelage>) {
        dataSource.addAll(items)
        notifyDataSetChanged()
    }


    /**
     * Limpia el listado de quejas
     */
    fun clean() {
        dataSource.clear()
        notifyDataSetChanged()
    }


    /**
     * Obtiene cada uno de los widget del layout y pinta en ellos la información
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: ComplaintTutelage) = with(itemView) {
            rowComplaintType?.value = item.typecomplaint?.name
            rowNumberComplaint?.value = item.id
            rowDateRegister?.value = item.date
            rowSubject?.value = item.subject
            rowDetailComplaint?.value = item.detail
            rowStatus?.value = item.status?.status
            //rowAttachment?.value = item.attachments?.toString()
        }
    }

}