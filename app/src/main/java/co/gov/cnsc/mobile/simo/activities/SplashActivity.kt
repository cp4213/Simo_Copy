package co.gov.cnsc.mobile.simo.activities

import android.accounts.Account
import android.os.Bundle
import android.util.Log
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.auth.AccountUtils
import co.gov.cnsc.mobile.simo.auth.AuthenticableAccount
import co.gov.cnsc.mobile.simo.models.SIMO
import co.gov.cnsc.mobile.simo.models.User
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.storage.SIMOResources
import com.github.kittinunf.fuel.core.FuelManager

/**
 * Maneja la funcionalidad del splash screen de la aplicación,
 * carga la información necesaria para el funcionamiento de la aplicación
 */
class SplashActivity : SIMOActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        tryLogin()
    }

    /**
     * Intenta realizar el login a través de una cuenta almacenada anteriormente
     */
    private fun tryLogin() {
        AccountUtils.getAuthAccount(this, object : AuthenticableAccount {
            override fun onResultAccount(account: Account?, session: User.Session?, token: String?, password: String?) {
                login(session, password)
            }

            override fun onErrorAccount() {
                getEntities()
            }

        })
        //getEntities()
    }

    /**
     * Intenta realizar un login llamando a un webservice con un objeto session y una contraseña
     * y almacena la cookie en caso de que la respuesta sea satisfactoria
     * @param objeto session obtenida del almacenamiento del dispositivo
     * @param password de la cuenta del usuario
     */
    fun login(session: User.Session?, password: String?) {
        val username = session?.username
        val password = password
        RestAPI.login(username!!, password!!, { user ->
            val cookie = FuelManager.instance.baseHeaders?.get("Cookie")
            val session = User.Session(user.id!!, user.username, user.name, cookie!!, user.urlPhoto)
            SIMO.instance.session = session
            AccountUtils.setAuthAccount(this, session, password)
            getUser()
        }, { fuelError ->
            if (fuelError.response.statusCode == 401) {
                AccountUtils.removeAuthAccount(this, session)
                getUser()
            } else {
                SIMOApplication.showErrorConectionConfirmToTry(this, {
                    login(session, password)
                }, {
                    AccountUtils.removeAuthAccount(this, session)
                    finish()
                })
            }

        })
    }

    /**
     * Obtiene la información del perfil del usuario para ser mostrada en la pantalla de inicio:
     * Nombre y foto
     */
    private fun getUser() {
        val session = SIMO.instance.session
        RestAPI.getUser(session?.username!!, { user ->
            session.imageUrl = user.urlPhoto
            getEntities()
        }) { fuelError ->
            SIMOApplication.showErrorConectionConfirmToTry(this, {
                getUser()
            }, {
                finish()
            })
        }
    }

    /**
     * Despues de cargar la información se envia al usuario a la pantalla de start o a la pantalla
     * de inicio logged dependiendo si está logeado o no
     */
    private fun goToNextActivity() {
        finish()
        if (SIMO.instance.isLogged) {
            goToMainActivity(true)
        } else {
            goToStartActivity()
        }
    }


    /**
     * Obtiene la lista de entidades a través de un web service
     *  y las guarda
     */
    fun getEntities() {
        //Toast.makeText(getApplicationContext(), "Toast_Login", Toast.LENGTH_LONG).show()
        RestAPI.getEntities({ entities ->
            SIMOResources.setEntities(this, entities)
            Log.d(SIMOApplication.TAG, "Entities downloaded")
            getDepartmentsOPEC()
        }, { fuelError ->
            SIMOApplication.showErrorConectionConfirmToTry(this, {
                getEntities()
            }, {
                finish()
            })

        })
    }

    /**
     * Obtiene la lista de departamentos en los que hay ofertas laborales
     * disponibles y las guarda
     */
    private fun getDepartmentsOPEC() {
        RestAPI.getDepartmentsOPEC({ departments ->
            SIMOResources.setDepartmentsOPEC(this, departments)
            getCitiesOPEC()
        }, { fuelError ->
            SIMOApplication.showErrorConectionConfirmToTry(this, {
                getDepartmentsOPEC()
            }, {
                finish()
            })
        })
    }

    /**
     * Obtiene la lista de ciudades en las que hay ofertas laborales
     * disponibles y las guarda
     */
    private fun getCitiesOPEC() {
        RestAPI.getCitiesOPEC({ cities ->
            SIMOResources.setCitiesOPEC(this, cities)
            getConvocatories()
        }, { fuelError ->
            SIMOApplication.showErrorConectionConfirmToTry(this, {
                getCitiesOPEC()
            }, {
                finish()
            })
        })
    }

    /**
     * Obtiene la lista de convocatorias disponibles y las guarda
     */
    private fun getConvocatories() {
        RestAPI.getConvocatories({ convocatories ->
            SIMOResources.setConvocatories(this, convocatories)
            Log.d(SIMOApplication.TAG, "Convocatories downloaded")
            getSalarialRanges()
        }, { fuelError ->
            //showFuelError(fuelError)
            SIMOApplication.showErrorConectionConfirmToTry(this, {
                getConvocatories()
            }, {
                finish()
            })
        })
    }

    /**
     * Obtiene la lista de rangos salariales disponibles y los guarda
     */
    private fun getSalarialRanges() {
        RestAPI.getRanges({ ranges ->
            SIMOResources.setSalarialRanges(this, ranges)
            Log.d(SIMOApplication.TAG, "Salarial Ranges downloaded")
            getLevels()
        }, { fuelError ->
            SIMOApplication.showErrorConectionConfirmToTry(this, {
                getSalarialRanges()
            }, {
                finish()
            })
        })
    }

    /**
     * Obtiene el listado de niveles y los guarda
     */
    private fun getLevels() {
        RestAPI.getLevels({ levels ->
            SIMOResources.setLevels(this, levels)
            Log.d(SIMOApplication.TAG, "Levels downloaded")
            getEducationalLevels()
        }, { fuelError ->
            SIMOApplication.showErrorConectionConfirmToTry(this, {
                getLevels()
            }, {
                finish()
            })
        })
    }

    /**
     * Obtiene el listado de niveles de educación y los guarda
     */
    private fun getEducationalLevels() {
        RestAPI.getEducationLevels({ educationalLevels ->
            SIMOResources.setEducationalLevels(this, educationalLevels)
            Log.d(SIMOApplication.TAG, "Educational levels")
            getDepartments()
        }, { fuelError ->
            SIMOApplication.showErrorConectionConfirmToTry(this, {
                getEducationalLevels()
            }, {
                finish()
            })
        })
    }

    /**
     * Obtiene el listado de todos los departamentos y los guarda
     */
    private fun getDepartments() {
        RestAPI.getDepartments({ departments ->
            SIMOResources.setDepartments(this, departments)
            getCities()
        }, { fuelError ->
            SIMOApplication.showErrorConectionConfirmToTry(this, {
                getDepartments()
            }, {
                finish()
            })
        })
    }

    /**
     * Obtiene el listado de todas las ciudades y las guarda
     */
    private fun getCities() {
        RestAPI.getCities({ cities ->
            SIMOResources.setCities(this, cities)
            getCountries()
        }, { fuelError ->
            SIMOApplication.showErrorConectionConfirmToTry(this, {
                getCities()
            }, {
                finish()
            })
        })
    }

    /**
     * Obtiene el listado de todos los paises y los guarda
     */
    private fun getCountries() {
        RestAPI.getCountries({ countries ->
            SIMOResources.setCountries(this, countries)
            goToNextActivity()
        }, { fuelError ->
            SIMOApplication.showErrorConectionConfirmToTry(this, {
                getCountries()
            }, {
                finish()
            })
        })
    }

    /**
     * Ejecución al comenzar una activity
     */
    override fun onStart() {
        super.onStart()
        AnalyticsReporter.screenSplash(this)
        Log.d("DEV","SplashA")
    }

}
