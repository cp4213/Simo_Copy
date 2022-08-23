package co.gov.cnsc.mobile.simo.activities

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import co.gov.cnsc.mobile.simo.*
import co.gov.cnsc.mobile.simo.auth.AccountUtils
import co.gov.cnsc.mobile.simo.extensions.intentOpenUrl
import co.gov.cnsc.mobile.simo.fragments.main.*
import co.gov.cnsc.mobile.simo.models.SIMO
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.storage.SIMOResources
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_popup_window_validations.view.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import com.github.kittinunf.fuel.core.Request


/**
 * Esta clase contiene la funcionalidad de las pantallas principales de SIMO
 * 'Buscar Empleos', 'Mis Empleos', 'Alertas', 'Convocatorias', 'Pagos'
 * Contiene estas opciones 'Ayúdenos a mejorar', 'Contacto', 'Cambiar Clave', 'Cerrar sesión'
 */
class MainActivity : SIMOActivity(), SIMOFragment.OnFragmentInteractionListener {


    private var searchItemMenu: MenuItem? = null
    private var searchView: SearchView? = null
    private var formateAdress: Boolean= false
    private var request: Request? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        configureUI()
        validationAdress()
    }

    /**
     * Configuración adicional del layout
     * Crea el menu principal (parte inferior), el menu C.V. (parte superior izquierda) y el menu auxiliar (parte superior derecha)
     */
    fun configureUI() {
        if (SIMO.instance.isLogged) {
            showToolbar()
            setImageToolbarLeft(R.drawable.account_user) {
                goToMyCVActivity()
            }
        } else {
            showToolbarBack( )
        }
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_search -> {
                    if (bottomNavigation.selectedItemId != R.id.menu_search) {
                        setMenuSearch()
                    }
                }
                R.id.menu_my_jobs -> {
                    if (bottomNavigation.selectedItemId != R.id.menu_my_jobs) {
                        setMenuJobs()
                    }
                }
                R.id.menu_alerts -> {
                    if (bottomNavigation.selectedItemId != R.id.menu_alerts) {
                        setMenuAlerts()
                    }
                }
                R.id.menu_convocatories -> {
                    if (bottomNavigation.selectedItemId != R.id.menu_convocatories) {
                        setMenuConvocatories()
                    }
                }
                R.id.menu_payments -> {
                    if (bottomNavigation.selectedItemId != R.id.menu_payments) {
                        setMenuPayments()
                    }
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
        setMenuSearch()

        if (SIMO.instance.isLogged) {
            bottomNavigation.visibility = View.VISIBLE
        } else {
            bottomNavigation.visibility = View.GONE
        }
    }
    /**
     * Muestra la pantalla de busqueda inicial 'Busqueda por Municipio' y 'Palabra clave'
     */

// Valida si existe la dirección en el nuevo formato, en caso negativo, envía a actualizar la dirección
    private fun validationAdress(){
        if(!formateAdress && SIMO.instance.isLogged){
            request = RestAPI.getUser( SIMO.instance.session?.username!!, { user ->
                if(user.standarAdress!!.equals("false")){
                    val builder = AlertDialog.Builder(this)
                    val view = layoutInflater.inflate(R.layout.layout_popup_window_validations,null)
                    builder.setView(view)
                    val dialog =builder.create()
                    dialog.show()
                    view.cerrar_val_btn.setText(R.string.act_adress)
                    view.cerrar_btn.setText(R.string.accept_button_dialog)
                    view.cerrar_btn.setOnClickListener{
                        goToEditAdress()
                    }
                }
            }, { fuelError ->
            })
        }
    }

    private fun setMenuSearch() {
        searchView?.setQuery("", false)
        searchView?.isIconified = true
        searchItemMenu?.isVisible = false
        if (SIMO.instance.isLogged) imageLeft?.visibility = View.VISIBLE
        setTextTitleToolbar(R.string.title_main)
        addElevationAppbarLayout()
        showFragment(SearchFragment.newInstance())
    }

    /**
     * Muestra la pantalla de 'Mis empleos'
     */
    private fun setMenuJobs() {
        searchItemMenu?.isVisible = false
        setTextTitleToolbar(R.string.my_employs)
        removeElevationAppbarLayout()
        showFragment(MyEmploysFragment.newInstance())
    }

    /**
     * Muestra la pantalla de 'Alertas'
     */
    private fun setMenuAlerts() {
        searchItemMenu?.isVisible = false
        setTextTitleToolbar(R.string.my_alerts)
        addElevationAppbarLayout()
        showFragment(MyAlertsFragment.newInstance())
    }

    /**
     * Muestra la pantalla de 'Convocatorias'
     */
    private fun setMenuConvocatories() {
        searchItemMenu?.isVisible = false
        setTextTitleToolbar(R.string.my_convocatories)
        addElevationAppbarLayout()
        showFragment(ConvocatoriesFragment.newInstance())
    }
    /**
     * Muestra la pantalla de Pagos
     */
    private fun setMenuPayments() {
        searchItemMenu?.isVisible = false
        setTextTitleToolbar(R.string.payments_done)
        addElevationAppbarLayout()
        showFragment(PaymentsFragment.newInstance())
    }
    /**
     * Muestra la pantalla de 'Mi CV' MyCVActivity
     */
    fun goToMyCVActivity() {
        val intent = Intent(this, MyCVActivity::class.java)
        startActivity(intent)
    }
    fun goToEditAdress() {
        val intent = Intent(this, EditAdressActivity::class.java)
        startActivity(intent)
    }


    /**
     * Muestra la pantalla del 'Tutorial'
     */
    private fun goToTutorial() {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse("https://www.youtube.com/watch?v=xPPdEIEg-4s")
        startActivity(openURL)
    }

    /**
     * Muestra la pantalla de 'Ayúdenos a mejorar'
     */
    private fun goToHelpUsActivity() {
        val intent = Intent(this, HelpUsToImproveActivity::class.java)
        startActivity(intent)
    }

    /**
     * Muestra la pantalla de 'Contacto'
     */
    private fun goToContactActivity() {
        val intent = Intent(this, ContactActivity::class.java)
        startActivity(intent)
    }


    /**
     * Muestra la pantalla de 'Cambiar Contraseña'
     */
    private fun goToChangePassword() {
        val intent = Intent(this, ChangePasswordActivity::class.java)
        startActivity(intent)
    }

    /**
     * Pregunta al usuario si está seguro de querer cerrar sesión
     * en caso afirmativo cierra la sesión
     */
    private fun closeSession() {
        SIMOApplication.showConfirmDialog(this, R.string.close_session,
            R.string.are_you_sure_exit, R.string.yes, { dialogInterface, which ->
                logout()
            }, R.string.no, { dialogInterface, which ->

            })
    }

    /**
     * Coloca el menú en la parte superior derecha de la pantalla
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (SIMO.instance.isLogged) {
            searchView = onCreateOptionsSearchView(R.menu.main_menu, menu)
        }else{
            searchView = onCreateOptionsSearchView(R.menu.main_menu_visitor, menu)
        }
        searchView?.setOnCloseListener {
            imageLeft?.visibility = View.VISIBLE
            return@setOnCloseListener false
        }
        searchView?.setOnSearchClickListener {
            imageLeft?.visibility = View.INVISIBLE
        }
        searchItemMenu = menu?.findItem(R.id.menu_search_view)

        // }
        return true
    }

    /**
     * Hace visibible una de las pantallas principales 'Inicio', 'Mis Empleos', 'Alertas',
     * 'Convocatorias','Pagos Realizados'
     */
    private fun showFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentManager.popBackStackImmediate()
        fragmentTransaction.replace(R.id.containerTabs, fragment).addToBackStack(null).commit()
    }


    /**
     * Finaliza la actividad cuando se da tap en el botón atrás de android
     */
    override fun onBackPressed() {
        finish()
        //goToMainActivity()
    }

    /**
     * Configura cada una de las opciones del menu de la parte superior derecha
     * cuando se da tap sobre cada una de las opciones
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
            when (item?.itemId) {
                R.id.menu_help_us_improve -> {
                    goToHelpUsActivity()
                }
                R.id.menu_citizen_tutorial -> {
                    goToTutorial()
                }
                R.id.menu_the_process -> {
                    knowTheProcess()
                }
                R.id.menu_user_guide -> {
                    userGuide()
                }
                R.id.menu_contact -> {
                    goToContactActivity()
                }
                R.id.menu_legal -> {
                    goToLegalActivity()
                }
                R.id.menu_change_key -> {
                    goToChangePassword()
                }
                R.id.menu_close_session -> {
                    closeSession()
                }
                R.id.menu_delete_history -> {
                    deleteSearchHistory()
                }
                R.id.menu_rate_simo -> {
                    rateSIMOMobile()
                }
                R.id.menu_audiences -> {
                    setAudiences()
                }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun setAudiences() {
        searchItemMenu?.isVisible = false
        setTextTitleToolbar(R.string.my_Audiencias_menu_title)
        removeElevationAppbarLayout()
        showFragment(AudiencesFragment.newInstance())
    }

    /**
     * Pregunta al usuario si está seguro de cerrar sesión
     * en caso afirmativo elimina la sesión del dispositivo y vuelve a la pantalla inicial
     */
    private fun logout() {
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.logout({ response ->
            ProgressBarDialog.stopProgressDialog()
            AccountUtils.removeAuthAccount(this, SIMO.instance.session)
            goToStartActivity()
        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            AccountUtils.removeAuthAccount(this, SIMO.instance.session)
            goToStartActivity()
        })
    }

    /**
     * Pregunta al usuario si está seguro de borrar el historíal de búsquedas
     * en caso afirmativo elimina las palabras puestas en el buscador
     */
    private fun deleteSearchHistory() {
        SIMOApplication.showConfirmDialog(this, R.string.delete_search_history,
            R.string.all_searches_will_be_deleted, R.string.delete, { dialogInterface, which ->
                SIMOResources.removeKeyWords(this)
                Toast.makeText(this, R.string.historial_deleted, Toast.LENGTH_SHORT).show()
            }, R.string.cancel, { dialogInterface, which ->

            })
    }

    /**
     * Abre la pagina de la aplicación en la tienda de aplicaciones del
     * Play store
     */
    private fun rateSIMOMobile() {
        this.intentOpenUrl(BuildConfig.URL_PLAY_STORE_APP)
    }

    /**
     * Abre la pagina web que contiene la explicación del proceso de
     * Concurso al Mérito
     */
    private fun knowTheProcess() {
        this.intentOpenUrl(BuildConfig.URL_THE_PROCESS)
    }


    /**
     * Abre la pagina web que contiene la guía de usuario para la app móvil
     * Concurso al Mérito
     */
    private fun userGuide() {
        this.intentOpenUrl("https://www.cnsc.gov.co/convocatorias/tutoriales-y-videos/guias-manuales")
        //SIMOApplication.checkPermissionsAndDownloadFile(this, "https://www.cnsc.gov.co/sites/default/files/2021-08/guia_para_el_manejo_de_la_aplicacion_movil_simo_v2.pdf#page=1&zoom=auto,-99,792", "guia_para_el_manejo_de_la_aplicacion_movil_simo_v2.pdf")
        //  Toast.makeText(this, R.string.download_text_start, Toast.LENGTH_LONG).show()
    }

    override fun onFragmentInteraction(uri: Uri) {

    }
}


