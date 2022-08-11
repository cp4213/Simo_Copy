package co.gov.cnsc.mobile.simo.views

import android.content.Context
import android.graphics.ColorFilter
import android.graphics.Rect
import com.google.android.material.textfield.TextInputLayout
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.extensions.dp2Px
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Esta clase extiende las funcionalidades del componente gráfico TextInputLayout
 * para acomodarse a los requerimientos de SIMO
 */
class TextInputLayout(context: Context, attrs: AttributeSet?) : TextInputLayout(context, attrs) {

    /**
     * Si el mensaje de error del campo se muestra o no
     */
    var enableBackgroundError = true

    /**
     * Componente de texto que contiene el error del campo
     */
    private var collapsingTextHelper: Any? = null

    /**
     * Rectángulo que ocupa el texto de error en el componente
     */
    private var bounds: Rect? = null

    /**
     * Componente que contiene el texto hint del campo
     */
    private var recalculateMethod: Method? = null

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.TextInputLayout, 0, 0)
            enableBackgroundError = typedArray.getBoolean(R.styleable.TextInputLayout_enableBackgroundError, true)
            typedArray.recycle()

        }
        configureHint()
    }

    /**
     * Muestra un mensaje de error de color rojo debajo del campo de texto
     * @param error Error que se muestra debajo del campo
     */
    override fun setError(error: CharSequence?) {
        val defaultColorFilter = getBackgroundDefaultColorFilter()
        super.setError(error)
        if (!enableBackgroundError) updateBackgroundColorFilter(defaultColorFilter)
        isErrorEnabled = error != null
    }

    /**
     * Habilita o deshabilita el mensaje de error
     */
    override fun setErrorEnabled(enabled: Boolean) {
        super.setErrorEnabled(enabled)

        if (!enabled) {
            this.setPadding(paddingLeft, paddingTop, paddingRight, 8.dp2Px(context))
            return
        }

        try {
            val errorViewField = TextInputLayout::class.java.getDeclaredField("mErrorView")
            errorViewField.isAccessible = true
            val errorView = errorViewField.get(this) as TextView
            this.setPadding(paddingLeft, paddingTop, paddingRight, 0.dp2Px(context))
            if (errorView != null) {
                errorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            }
        } catch (e: Exception) {
            // At least log what went wrong
            e.printStackTrace()
        }

    }

    /**
     * Colorea el componente si está en estado error o normal
     */
    override fun drawableStateChanged() {
        val defaultColorFilter = getBackgroundDefaultColorFilter()
        super.drawableStateChanged()
        //Reset EditText's background color to default.
        if (!enableBackgroundError) updateBackgroundColorFilter(defaultColorFilter)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        adjustBounds()
    }

    /**
     * Colorea el elemento gráfico dependiendo si está mostrando un error o no
     */
    private fun updateBackgroundColorFilter(colorFilter: ColorFilter?) {
        if (editText != null && editText!!.background != null)
            editText!!.background.colorFilter = colorFilter
    }

    /**
     * Obtiene el ColorFilter del campo de texto para ser coloreado
     */
    private fun getBackgroundDefaultColorFilter(): ColorFilter? {
        var defaultColorFilter: ColorFilter? = null
        if (editText != null && editText!!.background != null)
            defaultColorFilter = DrawableCompat.getColorFilter(editText!!.background)
        return defaultColorFilter
    }

    /**
     * Configura los espacios para mostrar u ocultar el mensaje de error
     */
    private fun configureHint() {
        try {
            val cthField = TextInputLayout::class.java.getDeclaredField("mCollapsingTextHelper")
            cthField.isAccessible = true
            collapsingTextHelper = cthField.get(this)

            val boundsField = collapsingTextHelper!!::class.java.getDeclaredField("mCollapsedBounds")
            boundsField.isAccessible = true
            bounds = boundsField.get(collapsingTextHelper) as Rect?

            recalculateMethod = collapsingTextHelper!!::class.java.getDeclaredMethod("recalculate")
        } catch (e: NoSuchFieldException) {
            collapsingTextHelper = null
            bounds = null
            recalculateMethod = null
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            collapsingTextHelper = null
            bounds = null
            recalculateMethod = null
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            collapsingTextHelper = null
            bounds = null
            recalculateMethod = null
            e.printStackTrace()
        }

    }

    /**
     * Ajusta el espacio ocupado por el texto de error para mostrarlo o desaparecerlo según
     * sea el caso
     */
    private fun adjustBounds() {
        if (collapsingTextHelper == null) {
            return
        }
        try {
            bounds?.left = editText!!.left + editText!!.paddingLeft
            recalculateMethod?.invoke(collapsingTextHelper)
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

    }
}