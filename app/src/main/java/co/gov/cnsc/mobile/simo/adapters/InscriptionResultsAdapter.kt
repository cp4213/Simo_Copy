package co.gov.cnsc.mobile.simo.adapters

import android.content.Context
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.extensions.inflate
import co.gov.cnsc.mobile.simo.models.InscriptionResult
import co.gov.cnsc.mobile.simo.models.PuntajesQueContinuan
import co.gov.cnsc.mobile.simo.models.TotalResult
import kotlinx.android.synthetic.main.item_view_inscription_result.view.*
import kotlinx.android.synthetic.main.item_view_scores_list.view.*
import kotlinx.android.synthetic.main.item_view_section_result.view.*
import kotlinx.android.synthetic.main.view_header_score_results.view.*
import java.util.*

/**
 * Esta clase contiene la funcionalidad para administrar un listado de resultados de inscripción en un recyclerview
 * @param context Contexto de la aplicación
 */
class InscriptionResultsAdapter(private val context: Context?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /**
     * Evento cuando se da tap sobre un item del listado
     */
    var listenerItemSelected: ((InscriptionResult) -> Unit)? = null

    /**
     * Evento cuando se da tap sobre un item del listado
     */
    /*var listenerScoreItemSelected: ((PuntajesQueContinuan) -> Unit)? = null*/

    /**
     * Listado de resultados de inscripción
     */
    var dataSource: ArrayList<Any> = mutableListOf<Any>() as ArrayList<Any>

    /**
     * Header de cada sección de inscripción
     */
    private val sectionHeader = TreeSet<Int>()

    /**
     * Header de cada sección de inscripción
     */
    //private val sectionScoreList = TreeSet<Int>()


    /**
     * Inicializa el ViewHolder
     * Pinta cada item dependiendo del tipo, si es el header, un titulo, o un item del listado
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            ViewHolderHeader(parent.inflate(R.layout.view_header_score_results))
        } else if (viewType == TYPE_SECTION_TITLE) {
            ViewHolderSectionTitle(parent.inflate(R.layout.item_view_section_result))
        } /*else if (viewType == TYPE_SCORES_LIST) {
            ViewHolderScoresList(parent.inflate(R.layout.item_view_scores_list))
        }*/ else {
            ViewHolderInscription(parent.inflate(R.layout.item_view_inscription_result))
        }
    }

    /**
     * Obtiene el número de elementos en la lista
     * Devuelve el tamaño de la colección que contiene los items que queremos mostrar
     */
    override fun getItemCount(): Int {
        return dataSource.size
    }

    /**
     * Método llamado por cada ViewHolder para enlazarlo al adapter
     * Aquí es donde pasaremos nuestros datos a nuestro ViewHolder
     * Pinta cada item dependiendo del tipo, si es el header, un titulo, o un item del listado
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder.itemViewType == TYPE_HEADER) {
            val headerHolder = holder as ViewHolderHeader
            headerHolder.bind(dataSource[position] as TotalResult)
        } else if (holder.itemViewType == TYPE_SECTION_TITLE) {
            val sectionHolder = holder as ViewHolderSectionTitle
            sectionHolder.bind(dataSource[position] as String)
        } /*else if (holder.itemViewType == TYPE_SCORES_LIST) {
            val scoresListHolder = holder as ViewHolderScoresList
            scoresListHolder.bind(dataSource[position] as PuntajesQueContinuan)
        }*/ else {
            val inscriptionHolder = holder as ViewHolderInscription
            inscriptionHolder.bind(dataSource[position] as InscriptionResult, listenerItemSelected)
        }
    }

    /**
     * Agrega el header de la sección, en este caso el resultado general
     */
    fun addItemHeader(item: TotalResult) {
        dataSource.add(item)
        notifyDataSetChanged()
    }

    /**
     * Agrega items al listado de resultados
     */
    fun addItems(items: List<Any>) {

        dataSource.addAll(items)
        notifyDataSetChanged()
    }

    /**
     * Agrega un tituLo de sección al listado
     */
    fun addItemSection(titleSection: String) {
        dataSource.add(titleSection)
        sectionHeader.add(dataSource.size - 1)
        notifyDataSetChanged()
    }

    /**
     * Agrega el header de la sección, en este caso el listado de puntajes que continúan en concurso
     */
    /*fun addItemsScoresList(items: List<Any>) {

        dataSource.addAll(items)
        notifyDataSetChanged()
    }*/

    /**
     * Limpia el listado de elementos
     */
    fun clean() {
        dataSource.clear()
        notifyDataSetChanged()
    }

    /**
     * Obtiene el tipo de item a mostrarse en el listado
     * Devuelve in entero que representa el el tipo de vista (view type)
     */
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            return TYPE_HEADER
        } else if (sectionHeader.contains(position)) {
            TYPE_SECTION_TITLE
        } /*else if (sectionHeader.contains(position)) {
            TYPE_SCORES_LIST
        }*/ else {
            TYPE_ITEM
        }
    }

    /**
     * Crea el view para el titulo de una sección
     */
    class ViewHolderSectionTitle(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: String) = with(itemView) {
            textTitleSection?.text = item
        }
    }

    /**
     * Crea el view para el resultado general: "Puntaje Total" y "Estado" (PUNTAJE TOTAL).
     */
    class ViewHolderHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: TotalResult) = with(itemView) {
            rowScore?.value = item.qualification.toString()
            rowStatus.value = item.status
            if (item.id == "-1") {
                itemView.visibility = View.GONE
            } else {
                itemView.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Crea el view para el listado de puntajes que continúan en concurso.
     */
    /*class ViewHolderScoresList(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: PuntajesQueContinuan) = with(itemView) {

            rowCompetitorScore?.value = item.idInscripcion.toString()
            rowIdInscription.value = item.puntaje

            if (item.idInscripcion == "-1") {
                itemView.visibility = View.GONE
            } else {
                itemView.visibility = View.VISIBLE
            }
        }
    }*/

    /**
     * Crea el view para un resultado de inscripción (RESULTADOS DE LAS PRUEBAS Y RECLAMACIONES).
     */
    class ViewHolderInscription(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: InscriptionResult, listener: ((InscriptionResult) -> Unit)?) = with(itemView) {

            rowDateLastUpdate.visibility = View.GONE
            rowPonderation.visibility = View.GONE
            rowLinkComplaints.visibility = View.GONE

            rowTest?.value = item.test
            rowCalification?.value = item.calification
            rowScoreAproval?.value = item.valueProbative
            rowDateLastUpdate?.value = item.datePublication
            rowPonderation?.value = item.ponderation.toString()

            if (item.isSummatoryScore) {

                rowScoreAproval?.visibility = View.VISIBLE
                rowPonderation?.visibility = View.VISIBLE
                //rowDateLastUpdate?.visibility = View.GONE
                //rowLinkComplaints?.visibility = View.GONE
                setOnClickListener(null)

            } else if (item.isResultTest) { //PONDERATION = NULL

                rowDateLastUpdate?.visibility = View.VISIBLE
                //rowPonderation?.visibility = View.GONE
                //rowCalification?.visibility = View.GONE
                rowScoreAproval?.visibility = View.GONE
                rowLinkComplaints?.visibility = View.VISIBLE
                //rowLinkComplaints?.textViewLabel?.paintFlags = rowLinkComplaints?.textViewLabel?.paintFlags!! or Paint.UNDERLINE_TEXT_FLAG
                //rowLinkComplaints?.textViewLabel?.paintFlags = rowLinkComplaints?.textViewLabel?.paintFlags!!
                setOnClickListener {

                    if (listener != null) {

                        listener(item)

                    }
                }
            }
        }
    }

    companion object {
        const val TYPE_HEADER = 2
        const val TYPE_SECTION_TITLE = 0
        const val TYPE_ITEM = 1
        //const val TYPE_SCORES_LIST = 3
        // comparar con InscriptionResultsAdapter de la copia del "12 de noviembre de 2019"
    }


}