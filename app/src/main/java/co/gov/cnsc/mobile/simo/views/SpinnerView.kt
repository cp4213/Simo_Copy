package co.gov.cnsc.mobile.simo.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.SpinnerAdapter
import co.gov.cnsc.mobile.simo.R
import kotlinx.android.synthetic.main.view_spinner_view.view.*

/**
 * Esta clase hace referencia al view que muestra posibles valores en un formulario
 * como por ejemplo el tipo de Documento
 */
class SpinnerView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    /**
     * Listado de opciones posibles para mostrar
     */
    private var itemsSpinner: List<Any>? = listOf()

    /**
     * Obtiene el layout del xml correspondiente
     */
    init {
        LayoutInflater.from(context).inflate(R.layout.view_spinner_view, this, true)
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.SpinnerView, 0, 0)
            val hint = resources.getText(typedArray
                    .getResourceId(R.styleable.SpinnerView_android_hint, R.string.empty))
            spinnerHint?.setText(hint)
        }
    }

    /**
     * Retorna o asigna los valores del listado de opciones
     */
    var items: List<Any>?
        set(value) {
            if (value != null) {
                itemsSpinner = value
                val adapter = ArrayAdapter<Any>(context,
                        R.layout.spinner_item_dropdown, value)
                adapter.setDropDownViewResource(android.R.layout
                        .simple_spinner_dropdown_item)
                spinnerSIMO.adapter = adapter
            }
        }
        get() = itemsSpinner


    /**
     * Retorna o establece la opción a mostrar
     */
    var selectedItem: Any?
        get() {
            return spinnerSIMO.selectedItem
        }
        set(value) {
            val item = itemsSpinner?.filter { item -> item == value }?.firstOrNull()
            val position = itemsSpinner?.indexOf(item)
            if (position != null && position >= 0) {
                setSelection(position, false)
            }
        }

    /**
     * Adapter que contiene el litado de opciones
     */
    val adapter: SpinnerAdapter?
        get() = spinnerSIMO.adapter


    /**
     * Selecciona una opción de la lista según la posición de la misma
     */
    fun setSelection(position: Int, animate: Boolean) {
        spinnerSIMO.setSelection(position, animate)
    }

}