package co.gov.cnsc.mobile.simo.views

import android.content.Context

import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import co.gov.cnsc.mobile.simo.R
import kotlinx.android.synthetic.main.view_footer_list.view.*

/**
 * Cuando el scroll de una lista de items llegue al final, se muestra un cargando
 * para cargar los siguientes items
 */
class FooterLisView(context: Context) : ConstraintLayout(context) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_footer_list, this, true)
    }

    /**
     * Muestra el circle de cargando
     */
    fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    /**
     * Esconde el circle de cargando
     */
    fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    /**
     * Retorna un boolean indicando si se están cargando o no los siguientes items
     */
    val isLoading: Boolean
        get() {
            return progressBar.visibility == View.VISIBLE
        }


}