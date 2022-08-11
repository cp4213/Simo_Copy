package co.gov.cnsc.mobile.simo.activities

import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOFragment
import co.gov.cnsc.mobile.simo.adapters.MyProfilePagerAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_my_cv.*
import kotlinx.android.synthetic.main.layout_app_bar_layout_tabs.*

/**
 * Contiene la funcionalidad de la pantalla de 'Mi Hoja de Vida' o 'Mi CV'
 * donde está la información básica del usuario, la formación, experiencia laboral,
 * productos intelectuales y documentos adicionales del usuario
 */
class MyCVActivity : SIMOActivity(), SIMOFragment.OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_cv)
        showToolbarBack()
        setTextTitleToolbar("Mis Datos Básicos")
        configureUI()

    }

    /**
     * Configuración adicional para la UI y los widgets,
     * Coloca el estilo de navegación superior y las opciones de los widgets principales de ésta pantalla
     */
    fun configureUI() {
        val profileAdapter = MyProfilePagerAdapter(this, supportFragmentManager)
        viewPagerProfile.adapter = profileAdapter
        tabLayout.setupWithViewPager(viewPagerProfile)
        tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_account_circle_24dp)
        tabLayout.getTabAt(1)?.setIcon(R.drawable.icons8_graduation_cap)
        tabLayout.getTabAt(2)?.setIcon(R.drawable.icons8_handshake)
        tabLayout.getTabAt(3)?.setIcon(R.drawable.ic_lightbulb_outline_24dp)
        tabLayout.getTabAt(4)?.setIcon(R.drawable.ic_baseline_library_books_24px)
        for (i in 0..tabLayout.tabCount) {
            tabLayout.getTabAt(i)?.icon?.
            setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
            //colorFilter = Color.WHITE as ColorFilter
        }
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        setTextTitleToolbar(getString(R.string.CV))
                    }
                    1 -> {
                        setTextTitleToolbar(getString(R.string.my_formation))
                    }
                    2 -> {
                        setTextTitleToolbar(getString(R.string.my_experience))
                    }
                    3 -> {
                        setTextTitleToolbar(getString(R.string.my_intelectual_products))
                    }
                    4 -> {
                        setTextTitleToolbar(getString(R.string.my_other_documents))
                    }
                }
            }

        })
    }

    override fun onFragmentInteraction(uri: Uri) {
    }

    /**
     * Muestra opción de busqueda rápida de empleos en la parte superior derecha de la pantalla de
     * Hoja de Vida
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        onCreateOptionsSearchView(R.menu.searchview, menu)
        return true
    }

}
