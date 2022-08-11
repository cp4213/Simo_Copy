package co.gov.cnsc.mobile.simo.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

/**
 * Listado de Funciones que agregan una funcionalidad a una clase del sistema
 */

/**
 * Crea un objeto view a partir de un layout xml
 * @param layoutRes id del layout xml
 */
fun ViewGroup.inflate(layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}


/**
 * Agrega la posibilidad de obtener no solo el index seleccionado sino el
 * objeto que hace referencia al indice en un listview
 * @param listener evento a ejecutar cuando se seleccione un item de la lista
 */
fun ListView.setOnItemClickListener(listener: (position: Int, item: Any) -> Unit) {
    this.setOnItemClickListener { parent, view, position, id ->
        val item = this.getItemAtPosition(position)
        listener(position, item)
    }
}

/**
 * Evalúa si un String es una direccion de correo electrónico
 */
val String.isEmailSyntax: Boolean
    get() {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

/**
 * Evalúa si un String tiene sintáxis JSON
 */
val String.isJSON: Boolean
    get() {
        try {
            JSONObject(this)
        } catch (ex: JSONException) {
            try {
                JSONArray(this)
            } catch (ex1: JSONException) {
                return false
            }
        }
        return true
    }


/**
 * Obtiene la extensión del archivo si el string es una ruta
 */
fun String.getExtensionPathFile(): String {
    val index = this.lastIndexOf(".")
    var extension = ""
    if (index >= 0) {
        extension = this.substring(index)
    }
    return extension
}

/**
 * Fuerza a mostrar el listado de sugerencias de un campo de texto autocompletable
 * cuando se da click o se hace foco en el campo de texto
 */
fun AutoCompleteTextView.showDropdownWhenGetFocus() {
    this.setOnClickListener {
        if (this.text.trim().isNullOrBlank())
            this.showDropDown()
    }
    this.setOnFocusChangeListener { v, hasFocus ->
        if (hasFocus && this.text.trim().isNullOrBlank()) {
            this.showDropDown()
        }
    }
}

/**
 * Transforma un número double en un string de formato divisa para ser mostrado en UI
 */
fun Double.toFormatCurrency(): String? {
    val symbols = DecimalFormatSymbols.getInstance()
    symbols.groupingSeparator = '.'
    val formatter = DecimalFormat("$###,###.##", symbols)
    return formatter.format(this)
}

/**
 * Convierte un número de densidad de pixeles a pixeles propias de la pantalla
 * @param context contexto UI desde el cuál se ejecuta
 */
fun Int.dp2Px(context: Context?): Int {
    val r = context?.resources
    val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), r?.displayMetrics)
    return px.toInt()
}

/**
 * Convierte un objeto calendar a un string con un formato de fecha
 * @param format formato de fecha deseado
 */
fun Calendar.toFormat(format: String): String {
    val df2 = SimpleDateFormat(format, Locale.getDefault())
    return df2.format(this.time)
}

/**
 * Realiza un intent de envio de correo
 * @param email dirección de correo
 * @param subject Asunto con el que se quiere enviar el correo
 * @param body contenido del mensaje
 */
fun Context.intentSendEmail(email: String?, subject: String?, body: String?) {
    val intent = Intent(Intent.ACTION_VIEW)
    val data = Uri.parse("mailto:?subject=$subject&body=$body&to=$email")
    intent.data = data
    this.startActivity(intent)
}

/**
 * Abre una url en el browser del dispositivo
 * @param url ruta url a ser abierta en el browser por default del dispositivo
 */
fun Context.intentOpenUrl(url: String?) {
    if (url != null && !url.trim { it <= ' ' }.isEmpty()) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            this.startActivity(browserIntent)
        } catch (e: android.content.ActivityNotFoundException) {
            Log.e("error", e.toString())
        }

    }
}

/**
 * Realiza una acción de compartir desde la app hacia apps sociales que tenga el dispositivo
 * @param title titulo de lo que se quiere compartir
 * @param titleChooser titulo con el que se quiere que salga la ventana de compartir
 * @param description Contenido en texto que se quiere compartit
 * @param urlLink Url a compartir
 * @param errorShare Error a mostrar en el caso de que un error al compartir ocurra
 */
fun Context.intentShareTextLink(title: String?, titleChooser: String?, description: String?, urlLink: String?, errorShare: String?) {
    try {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/html"
        i.putExtra(Intent.EXTRA_SUBJECT, title)
        var sAux = "\n" + description + "\n\n"
        sAux = sAux + urlLink
        i.putExtra(Intent.EXTRA_TEXT, sAux)
        this.startActivity(Intent.createChooser(i, titleChooser))
    } catch (e: Exception) {
        Toast.makeText(this, errorShare, Toast.LENGTH_SHORT)
    }
}

/**
 * Guarda una cadena de caracteres en las preferencias de la aplicación
 * @param name nombre de la preferencia
 * @param preference valor de la preferencia
 */
fun Context.savePreferenceString(name: String?, preference: String?) {
    val settings = PreferenceManager.getDefaultSharedPreferences(this)
    val editor = settings.edit()
    editor.putString(name, preference)
    editor.apply()
}

/**
 * Obtiene un valor guardado correspondiente a la preferencia/configuracion seleccionada
 * @param name nombre de la preferencia/configuracion que se quiere obtener
 */
fun Context.getPreferenceString(name: String?): String? {
    val settings = android.preference.PreferenceManager.getDefaultSharedPreferences(this)
    return settings.getString(name, null)
}

/**
 * Borra una preferencia/configuracion de la aplicación del dispositivo
 * @param name nombre de la preferencia/configuración a eliminar
 */
fun Context.deletePreference(name: String?) {
    val settings = PreferenceManager.getDefaultSharedPreferences(this)
    val editor = settings.edit()
    editor.remove(name)
    editor.apply()
}

/**
 * Esconde el teclado virtual de la pantalla
 */
fun AppCompatActivity.hideKeyboard() {
    val inputMethodManager = this.getSystemService(android.app.Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    if (inputMethodManager.isActive && this.currentFocus != null) {
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
    }
}


