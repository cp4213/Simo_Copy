package co.gov.cnsc.mobile.simo.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.viewpager.widget.ViewPager
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.adapters.TutorialPageAdapter
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.models.AdviceTutorial
import com.github.kittinunf.fuel.core.FuelManager
import kotlinx.android.synthetic.main.activity_start.*
import java.util.*

/**
 * Contiene la funcionalidad de la pantalla inicial de SIMO, donde está las opciones
 * de Ingreso, Registro e Ingreso como invitado
 */
class StartActivity : SIMOActivity() {

    companion object {
        const val ACTION_LOGIN = "login"
        const val ACTION_REGISTER = "register"

        /**
         * Listado de consejos en el viewpager de la pantalla inicial de SIMO
         */
        val slidesTutorial: List<AdviceTutorial>?
            get() {
                val list = mutableListOf<AdviceTutorial>()

                //p1, s1, p2, p3, p4
                list.add(AdviceTutorial(descriptionRes = R.string.tutorial_advice_1, imageBackground = R.drawable.primer_slide))
                list.add(AdviceTutorial(descriptionRes = R.string.tutorial_advice_3, imageBackground = R.drawable.tercer_slide))
                list.add(AdviceTutorial(descriptionRes = R.string.tutorial_advice_2, imageBackground = R.drawable.segundo_slide))
                list.add(AdviceTutorial(descriptionRes = R.string.tutorial_advice_4, imageBackground = R.drawable.cuarto_slide))

                return list
            }
    }


    private var action: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        configureUI()
        action = intent?.action
        if (action == ACTION_LOGIN) {
            val username = intent?.extras?.getString("username")
            goToLoginActivity(username = username)
        } else if (action == ACTION_REGISTER) {
            goToRegisterActivity(RegisterActivity.ACTION_FIRST, null)
        }
    }

    /**
     * Configuración de widgets y componentes del layout
     * Pinta los consejos/tips en la pantalla
     */

    fun configureUI() {
        imageBackground.setFactory {
            val image = ImageView(applicationContext)
            image.scaleType = ImageView.ScaleType.CENTER_CROP
            image.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT)
            image
        }
        imageBackground.setInAnimation(applicationContext, android.R.anim.fade_in)
        imageBackground.setOutAnimation(applicationContext, android.R.anim.fade_out)
        imageBackground.setImageResource(R.drawable.primer_slide)
        val listSlides = slidesTutorial
        val adapter = TutorialPageAdapter(this, listSlides as ArrayList<AdviceTutorial>)
        viewPagerAdvices.adapter = adapter
        viewPagerAdvices.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                val imageBackgroundSlide = listSlides[position].imageBackground
                imageBackground.setImageResource(imageBackgroundSlide!!)
            }

        })
        circleIndicator.setViewPager(viewPagerAdvices)
    }


    /**
     * Evento cuando se da tap sobre la opción 'Entrar como invitado'
     */

    fun onEnterAsInvited(button: View) {
        val cookie = "null"
        FuelManager.instance.baseHeaders = mapOf("Cookie" to cookie)
        goToMainActivity(false)
    }

    /**
     * Evento cuando se da tap sobre la opción 'Ingresar'
     */
    fun onLogin(button: View) {
        goToLoginActivity()
    }

    /**
     * Evento cuando se da tap sobre el logo de la CNSC
     */
    fun onWSCNSC(button: View) {
        val urlPage = "https://www.cnsc.gov.co/"
        //Abre url de pagina.
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlPage)))
    }

    /**
     * Evento cuando se da tap sobre la opción 'Registrar'
     */
    fun onRegister(button: View) {
        goToRegisterActivity(RegisterActivity.ACTION_FIRST, null)
    }

    /**
     * Ejecución al comenzar una activity
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenStart(this)
        Log.i("Act","StartA")
    }


}
