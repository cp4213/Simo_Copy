package co.gov.cnsc.mobile.simo.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOApplication
import com.github.kittinunf.fuel.core.FuelError
import kotlinx.android.synthetic.main.view_empty_state.view.*

/**
 * Esta clase contiene la funcionalidad de los empty state en los listados de
 * items de la aplicación
 */
class EmptyStateView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    /**
     * Inicializa la vista con el layout del estado vacío
     */
    init {
        LayoutInflater.from(context).inflate(R.layout.view_empty_state, this, true)
        attrs?.let {

        }
    }

    /**
     * Obtiene o asigna un texto a la pantalla de estado vacío
     */
    var text: String
        get() {
            return textViewEmptyState?.text as String
        }
        set(value) {
            textViewEmptyState?.text = value
        }

    /**
     * Muestra la vista cuando no hay items en el listado
     */
    fun showEmptyState() {
        show()
        imageEmptyState?.setImageResource(R.drawable.no_data)
        textViewEmptyState?.setText(R.string.there_is_not_elements_to_show)
        buttonRetry?.visibility = View.GONE
    }

    /**
     * Muestra un mensaje de error cuando se obtiene un error de conexión
     * @param messageError mensaje de error a mostrar
     * @param retryAction acción a ejecutarse cuando se de tap al botón reintentar
     */
    fun showConectionErrorState(messageError: String? = "", retryAction: () -> Unit?) {
        show()
        imageEmptyState?.setImageResource(R.drawable.broken_connection)
        textViewEmptyState?.setText(R.string.there_is_a_conection_problem)
        buttonRetry?.visibility = View.VISIBLE
        buttonRetry?.setOnClickListener {
            retryAction()
        }
        textViewError?.text = messageError
    }

    /**
     * Muestra un mensaje de error cuando un servicio web retorna un error
     * @param fuelError error del servicio web
     * @param retryAction acción a ejecutarse cuando se de tap al botón reintentar
     */
    fun showConectionErrorState(fuelError: FuelError?, retryAction: () -> Unit?) {
        val stringError = SIMOApplication.getFuelError2String(context, fuelError)
        showConectionErrorState(stringError, retryAction)
    }

    /**
     * Esconde la vista de estado vacío
     */
    fun hide() {
        constraintEmpty?.visibility = View.INVISIBLE
    }

    /**
     * Muestra la vista de estado vacío
     */
    private fun show() {
        constraintEmpty?.visibility = View.VISIBLE
    }

}