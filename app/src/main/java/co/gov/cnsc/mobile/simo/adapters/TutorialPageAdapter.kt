package co.gov.cnsc.mobile.simo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.models.AdviceTutorial
import kotlinx.android.synthetic.main.item_view_tutorial_advice.view.*
import java.util.*

/**
 * Esta clase contiene la funcionalidad para administrar las pestañas de los consejos y/o tips en la pantalla inicial
 * @param context Contexto de la aplicación
 * @param dataSource listado de consejos o tips para mostrar
 */
class TutorialPageAdapter(private val context: Context, private val dataSource: ArrayList<AdviceTutorial>) : PagerAdapter() {


    /**
     * Pinta un consejo/tip en un layout
     */
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val item = dataSource[position]
        val view = LayoutInflater.from(container.context)
                .inflate(R.layout.item_view_tutorial_advice, container, false)
        view.textAdvice.setText(item.descriptionRes!!)
        container.addView(view)
        return view
    }

    /**
     * Destruye un layout/consejo/tip si no se está usando o mostrando en el momento
     */
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    /**
     * Obtiene el total de consejos/tips a mostrarse
     */
    override fun getCount(): Int {
        return dataSource.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

}
