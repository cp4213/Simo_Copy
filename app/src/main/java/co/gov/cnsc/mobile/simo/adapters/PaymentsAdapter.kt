package co.gov.cnsc.mobile.simo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.extensions.inflate
import co.gov.cnsc.mobile.simo.models.Payment
import kotlinx.android.synthetic.main.item_view_payment.view.*

/**
 * Esta clase contiene la funcionalidad para administrar un listado de pagos en un listview
 * @param context Contexto de la aplicación
 * @param dataSource listado de pagos a mostrar
 */
class PaymentsAdapter(private val context: Context?, var dataSource: ArrayList<Payment>) : BaseAdapter() {


    /**
     * Pinta los datos de un pago  en un layout
     */
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = parent?.inflate(R.layout.item_view_payment)
        val item = getItem(position)
        rowView?.rowConvocatory?.value = item.workOffer?.job?.convocatory?.name
        rowView?.rowEmploy?.value = item.workOffer?.job?.id
        rowView?.rowValue?.value = item.valueCurrency
        rowView?.rowTransactionStatus?.value = item.status?.name
        rowView?.rowDatePayment?.value = item.datePay
        rowView?.rowTypePayment?.value = item.type?.name
        rowView?.rowBank?.value = item.bank
        rowView?.rowReferencePayment?.value = item.id
        rowView?.rowTrackCode?.value = item.trackCode
        return rowView!!
    }

    /**
     * Obtiene un pago de cierta posición
     */
    override fun getItem(position: Int): Payment {
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