package co.gov.cnsc.mobile.simo.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import co.gov.cnsc.mobile.simo.R
import kotlinx.android.synthetic.main.view_row_stage_convocatory.view.*

/**
 * Esta clase contiene la funcionalidad del layout para mostrar una etapa de convocatoria
 */
class RowStageConvocatoryView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {


    /**
     * Obtiene el layout y lo inicializa
     */
    init {
        LayoutInflater.from(context).inflate(R.layout.view_row_stage_convocatory, this, true)
        attrs?.let {

        }
    }

    /**
     * Retorna o muestra en el layout la descripción de la etapa
     */
    var description: String
        get() {
            return textViewDescription.text as String
        }
        set(value) {
            textViewDescription.text = value
        }

    /**
     * Retorna o muestra en el layout la fecha de inicio de la etapa
     */
    var dateStart: String
        get() {
            return textViewDateStart.text as String
        }
        set(value) {
            textViewDateStart.text = value
        }

    /**
     * Retorna o muestra en el layout la fecha de finalización de la etapa
     */
    var dateEnd: String
        get() {
            return textViewDateEnd.text as String
        }
        set(value) {
            textViewDateEnd.text = value
        }

    /**
     * Convierte los textos de la fila correspondiente en negrilla
     */
    fun setTextviewsToBold() {
        textViewDateEnd.setTypeface(textViewDateEnd.typeface, Typeface.BOLD)
        textViewDateStart.setTypeface(textViewDateStart.typeface, Typeface.BOLD)
        textViewDescription.setTypeface(textViewDescription.typeface, Typeface.BOLD)
    }


}