package co.gov.cnsc.mobile.simo

import android.content.Intent
import android.graphics.Color
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import co.gov.cnsc.mobile.simo.activities.*
import co.gov.cnsc.mobile.simo.extensions.dp2Px
import co.gov.cnsc.mobile.simo.extensions.hideKeyboard
import co.gov.cnsc.mobile.simo.models.User
import co.gov.cnsc.mobile.simo.storage.SIMOResources
import com.github.kittinunf.fuel.core.FuelError
import com.google.android.material.appbar.AppBarLayout

/**
 * Activity SIMO de la cuál heredan todas las otras activities
 * Esta clase contiene metodos y funcionalidades comunes para todas las pantallas
 */
open class SIMOActivity : AppCompatActivity() {


    /**
     * Recoge el toolbar del layout y con este llena el action bar por defecto
     */
    fun showToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        }
    }

    /**
     * Recoge el toolbar del layout y con este llena el action bar por defecto
     * y adicionalmente muestra el botón atrás en la parte superior izquierda
     */
    fun showToolbarBack() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }


    /**
     * Quita cualquier efecto de elevación del action bar
     */
    fun removeElevationAppbarLayout() {
        val appbar = findViewById<AppBarLayout>(R.id.appBar)
        ViewCompat.setElevation(appbar, 0.dp2Px(this).toFloat())
    }

    /**
     * Agrega un efecto de elevación al action bar
     */
    fun addElevationAppbarLayout() {
        val appbar = findViewById<AppBarLayout>(R.id.appBar)
        ViewCompat.setElevation(appbar, 5.dp2Px(this).toFloat())
    }

    /**
     * Muestra un titulo en el appbar de la aplicación
     * @param text string titulo de la actividad
     * @param addSpaceRightForCenter agrega un relleno en la parte derecha del appbar para
     * ayudar a centrar los titulos
     */
    fun setTextTitleToolbar(text: String?, addSpaceRightForCenter: Boolean? = false) {
        val textView = findViewById<TextView>(R.id.textTitle)
        if (textView != null) {
            textView.visibility = View.VISIBLE
            textView.text = text
            if (addSpaceRightForCenter == true) {
                textView.setPadding(0, 0, 64.dp2Px(this), 0)
            } else {
                textView.setPadding(0, 0, 0, 0)
            }
        }
    }

    /**
     * Muestra un titulo en el appbar de la aplicación
     * @param resString id del string titulo de la aplicación
     * @param addSpaceRightForCenter agrega un relleno en la parte derecha del appbar para
     * ayudar a centrar los titulos
     */
    fun setTextTitleToolbar(resString: Int, addSpaceRightForCenter: Boolean? = false) {
        val title = getString(resString)
        setTextTitleToolbar(title, addSpaceRightForCenter)
    }

    /**
     * Agrega una imagen/icono en la parte superior izquierda del app bar
     * @param imageRes imagen/icono a mostrar
     * @param action action a ejecutar al dar tap sobre el icono
     */
    fun setImageToolbarLeft(imageRes: Int?, action: () -> Unit?) {
        val imageLeft = findViewById<ImageView>(R.id.imageLeft)
        if (imageLeft != null) {
            imageLeft.visibility = View.VISIBLE
            imageLeft.setImageResource(imageRes!!)
            if (action != null) {
                imageLeft.setOnClickListener {
                    action()
                }
            }
        }
    }

    /**
     * Va a la pantalla principal de la aplicación
     */
    fun goToMainActivity(cleanStack: Boolean = true) {
        val intent = Intent(this, MainActivity::class.java)
        if (cleanStack) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    /**
     * Va a la pantalla inicial de la aplicación
     */
    fun goToStartActivity(action: String? = null, username: String? = null) {
        val intent = Intent(this, StartActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.action = action
        intent.putExtra("username", username)
        startActivity(intent)
    }

    /**
     * Va a la pantalla de login
     */
    fun goToLoginActivity(username: String? = null, password: String? = null) {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("username", username)
        intent.putExtra("password", password)
        startActivity(intent)
    }

    /**
     * Va a la pantalla de registro
     */
    fun goToRegisterActivity(action: String, user: User?) {
        val intent = Intent(this, RegisterActivity::class.java)
        intent.action = action
        intent.putExtra("user", user)
        startActivity(intent)
    }

    /**
     * Va a la pantalla de terminos legales
     */
    fun goToLegalActivity() {
        val intent = Intent(this, LegalAdviceActivity::class.java)
        startActivity(intent)
    }

    /**
     * Acciónn cuando se da tap sobre el icono atrás
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item!!)
    }

    /**
     * Muestra un toast con el error en caso de que un servicio web no se ejecute correctamente
     */
    fun showFuelError(error: FuelError) {
        SIMOApplication.showFuelError(this, error)
    }


    /**
     * Muestra la opción de búsqueda rápida sobre la parte superior derecha de cualquier pantalla
     */
    fun onCreateOptionsSearchView(menuRes: Int, menu: Menu?): SearchView {
        menuInflater.inflate(menuRes, menu)
        val menuItem = menu?.findItem(R.id.menu_search_view)
        val searchView = menuItem?.actionView as SearchView
        val searchSrcTextView = searchView.findViewById(androidx.appcompat.R.id.search_src_text) as SearchView.SearchAutoComplete
        searchView.setIconifiedByDefault(true)
        searchView.queryHint = getString(R.string.search_job___)
        searchSrcTextView.setHintTextColor(Color.WHITE)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query?.length!! > 0) {
                    SIMOResources.tryAddKeyWord(applicationContext, query)
                    SIMOApplication.goToWorkOffers(context = applicationContext, keyWord = query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })

        val keyWords = SIMOResources.getKeyWords(this)
        val adapterK = ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, keyWords)
        searchSrcTextView.setAdapter(adapterK)
        searchSrcTextView.setTextColor(Color.WHITE)
        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return true
            }

            override fun onSuggestionClick(position: Int): Boolean {
                val keyWord = keyWords[position]
                searchSrcTextView.setText(keyWord)
                return true
            }

        })

        return searchView
    }

    /**
     * Esconde el teclado y finaliza/esconde la pantalla
     */
    override fun finish() {
        hideKeyboard()
        super.finish()
    }

}