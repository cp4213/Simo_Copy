package co.gov.cnsc.mobile.simo.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import co.gov.cnsc.mobile.simo.R
import kotlinx.android.synthetic.main.view_row_label_value.view.*

/**
 * Esta clase administra el comportamiento del view cuando hay información de tipo
 * etiqueta, valor
 */
class RowLabelValue(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    /**
     * Inicializa el layout dependiendo si se quiere que la información se muestre
     * en forma horizontal o vertical
     */
    init {
        attrs.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.RowLabelValue, 0, 0)
            val orientation = typedArray.getInteger(R.styleable.RowLabelValue_android_orientation, LinearLayout.HORIZONTAL)
            if (orientation == LinearLayout.HORIZONTAL) {
                LayoutInflater.from(context).inflate(R.layout.view_row_label_value, this, true)
            } else {
                LayoutInflater.from(context).inflate(R.layout.view_row_label_value_vertical, this, true)
            }
            val textLabel = resources.getText(typedArray
                    .getResourceId(R.styleable.RowLabelValue_textLabel, R.string.empty))
            val textValue = resources.getText(typedArray
                    .getResourceId(R.styleable.RowLabelValue_textValue, R.string.empty))
            val textLabelSize = typedArray.getDimensionPixelSize(R.styleable.RowLabelValue_textLabelSize, -1)
            val textValueSize = typedArray.getDimensionPixelSize(R.styleable.RowLabelValue_textValueSize, -1)
            val icon = typedArray.getResourceId(R.styleable.RowLabelValue_drawableIcon, 0)

            label = textLabel as String
            value = textValue as String
            drawableLeft = icon
            if (textLabelSize >= 0) {
                textViewLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, textLabelSize.toFloat())
            }
            if (textValueSize >= 0) {
                textViewValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, textValueSize.toFloat())
            }
            typedArray.recycle()
        }
    }

    /**
     * Obtiene el textview correspondiente al etiqueta o label de la información
     */
    val textViewLabel: TextView
        get() {
            return textRowLabel
        }

    /**
     * Obtiene el textview correspondiente al valor de la información
     */
    val textViewValue: TextView
        get() {
            return textRowValue
        }

    /**
     * Retorna o establece el texto de la etiqueta
     */
    var label: String?
        get() {
            return textRowLabel.text as String
        }
        set(value) {
            if (value != null)
                textRowLabel.text = value
        }

    /**
     * Retorna o establece el texto del valor
     */
    var value: String?
        get() {
            return textRowLabel.text as String
        }
        set(value) {
            if (value != null)
                textRowValue.text = value
        }

    /**
     * Pone una imagen al lado izquierdo de la fila
     */
    var drawableLeft: Int = 0
        set(value) {
            imageRowIcon.setImageResource(value)
        }

    /**
     * Muestra o escode el espacio para la imagen del lado izquierdo
     */
    var visibilityDrawableLeft: Int
        get() = imageRowIcon.visibility
        set(value) {
            imageRowIcon.visibility = value
        }
}