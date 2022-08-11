package co.gov.cnsc.mobile.simo

import android.Manifest
import android.app.*
import android.app.Notification
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.AbsListView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.multidex.MultiDexApplication
import co.gov.cnsc.mobile.simo.activities.DetailConvocatoryActivity
import co.gov.cnsc.mobile.simo.activities.DetailWorkOfferActivity
import co.gov.cnsc.mobile.simo.activities.SearchableListViewActivity
import co.gov.cnsc.mobile.simo.activities.WorkOffersActivity
import co.gov.cnsc.mobile.simo.extensions.getExtensionPathFile
import co.gov.cnsc.mobile.simo.extensions.isJSON
import co.gov.cnsc.mobile.simo.extensions.toFormat
import co.gov.cnsc.mobile.simo.fragments.main.SearchFragment

import co.gov.cnsc.mobile.simo.models.*
import co.gov.cnsc.mobile.simo.util.GenericFileProvider
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.backends.okhttp.OkHttpImagePipelineConfigFactory
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.squareup.okhttp.Interceptor
import com.squareup.okhttp.OkHttpClient
import id.zelory.compressor.Compressor
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Clase Application personalizada para la app SIMO, contiene funcionalidad general para toda la app
 * Como muestra de dialogs, alerts inicialización de librarías
 */
class SIMOApplication : MultiDexApplication(), Application.ActivityLifecycleCallbacks {

    /**
     * Inicialización general de la app
     * Inicialización de librearías y ciclo de vida de la aplicación
     */
    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(baseContext)
        registerActivityLifecycleCallbacks(this)
        initializeFresco(baseContext)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }


    /**
     * Métodos estáticos se se pueden ejecutar desde cualquier parte de la aplicación
     */
    companion object {
        const val TAG = "SIMO"
        const val REGEX_EMPTY_FIELD = "^(?=\\s*\\S).*$"
        const val REGEX_FOR_PASSWORD = "^[a-zA-Z0-9@%#&()/+*$,._\\-]{8,20}$"
        const val REGEX_FOR_USERNAME = "^[a-zA-Z0-9._]{6,30}$"
        const val REGEX_FOR_NUMBER_ID = "^[0-9]{6,10}$"
        const val REGEX_FOR_OPEC_NUMBER = "^[0-9]{2,10}$"
        const val REGEX_FOR_REACHED_LEVEL = "^[0-9]{1,2}$"
        const val REGEX_FOR_PHONE_NUMBER = "^[0-9]{7,15}$"
        const val REGEX_FOR_POSTAL_CODE = "^[a-zA-Z0-9- ]{6,25}$"
        const val REGEX_FOR_RESOLUTIONNUMBER = "^[ña-zA-ZÁÉÍÓÚáéíóúü0-9-.,# ]{1,60}$"
        const val REGEX_FOR_RESIDENTIAL_ADDRESS = "^[ña-zA-ZÁÉÍÓÚáéíóúü0-9-.,# ]{10,60}$"
        const val REGEX_FOR_PROGRAM_INSTITUTION = "^[ña-zA-ZÁÉÍÓÚáéíóúü0-9-()., ]{6,150}$"
        //const val REGEX_FOR_EMPTY_FIELD = "^[^]+$"
        const val REGEX_FOR_NAMES = "^[ña-zÑA-ZÁÉÍÓÚáéíóú' ]{2,60}$"
        const val REGEX_FOR_COMPANY = "^[ña-zÑA-ZÁÉÍÓÚáéíóúü0-9-., ]{3,50}$"
        const val REGEX_FOR_EMPLOYMENT = "^[ña-zÑA-ZÁÉÍÓÚáéíóúü ]{5,50}$"
        const val REGEX_FOR_AVERAGE_HOURS = "^(2[0-4]|1[0-9]|[1-9])$"
        const val REGEX_FOR_LASTNAMES = "^[ña-zÑA-ZÁÉÍÓÚáéíóú' ]{3,60}$"
        const val REGEX_FOR_HOURS_WORKED = "^[0-9]{1,6}$"
        const val REGEX_FOR_IDENTIFIER_NUMBER = "^[a-zA-Z0-9@%#&()/+*$,._\\- ]{3,60}$"
        const val REGEX_FOR_BIBLIOGRAPHICAL_APPOINTMENT = "^[a-zA-ZÁÉÍÓÚáéíóúü0-9-., ]{15,300}$"
        const val REGEX_FOR_HELP_US_TO_IMPROVE = "^[a-zA-ZÁÉÍÓÚáéíóúü0-9-., ]{10,500}$"
        const val REQUEST_CODE_DETAIL_ALERT = 1

        /**
         * Muestra un popup de confirmación
         * @param context contexto desde el cuál se muestra el popup
         * @param titleRes id del string titulo
         * @param messageRes id del string mensaje
         * @param resStringPositive id del string botón de la acción positiva
         * @param actionPositive action a ejecutar cuando se da al botón de acción positiva
         * @param resStringNegative id del string botón de la acción negativa
         * @param actionNegative action a ejecutar cuando se da al botón de acción negativa
         */
        fun showConfirmDialog(context: Context, titleRes: Int,
                              messageRes: Int, resStringPositive: Int,
                              actionPositive: (DialogInterface, Int) -> Unit, resStringNegative: Int,
                              actionNegative: (DialogInterface, Int) -> Unit) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(titleRes)
            builder.setMessage(messageRes)
            builder.setPositiveButton(resStringPositive, actionPositive)
            builder.setNegativeButton(resStringNegative, actionNegative)
            builder.setCancelable(false)
            val dialog = builder.create()
            dialog.show()
        }

        /**
         * Inicializa la librería de Fresco pra carga de imagenes
         * @param context contexto de la aplicación
         */
        fun initializeFresco(context: Context) {
            Fresco.shutDown()
            if (SIMO.instance.session != null) {
                val client = OkHttpClient()
                client.interceptors().add(Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    val cookie = SIMO.instance.session?.cookie
                    builder.addHeader("Cookie", cookie)
                    chain.proceed(builder.build())
                })
                val config = OkHttpImagePipelineConfigFactory
                        .newBuilder(context, client)
                        .build()
                Fresco.initialize(context, config)
            } else {
                Fresco.initialize(context)
            }
        }

        /**
         * Muestra un mensaje de alerta
         * @param context contexto de la aplicación
         * @param title titulo de la alerta
         * @param message mensaje de la alerta
         * @param stringPositive texto del botón de la alerta
         * @param actionPositive acción cuando se da click en el botón de la alerta
         */
        private fun showAlertDialog(context: AppCompatActivity?, title: String?,
                                    message: String?, stringPositive: String?,
                                    actionPositive: (DialogInterface, Int) -> Unit) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton(stringPositive, actionPositive)
            val dialog = builder.create()
            dialog.show()
        }

        /**
         * Muestra un mensaje de alerta
         * @param context contexto de la aplicación
         * @param titleRes id del string del titulo de la alerta
         * @param messageRes id del string del mensaje de la alerta
         * @param resStringPositive id del string del texto del botón de la alerta
         * @param actionPositive acción cuando se da click en el botón de la alerta
         */
        fun showAlertDialog(context: AppCompatActivity?, titleRes: Int,
                            messageRes: Int, resStringPositive: Int,
                            actionPositive: (DialogInterface, Int) -> Unit) {
            val title = context?.getString(titleRes)
            val message = context?.getString(messageRes)
            val okButton = context?.getString(resStringPositive)
            showAlertDialog(context = context, title = title, message = message, stringPositive = okButton, actionPositive = actionPositive)
        }

        /**
         * Muestra un toast con el mensaje de error de un servicio web
         * @param context contexto de la aplicación
         * @param error error retornado por la ejecuión del web service
         */
        fun showFuelError(context: Context?, error: FuelError?) {
            val body = getFuelError2String(context, error)
            if (context != null) {
                Toast.makeText(context, body, Toast.LENGTH_LONG).show()
            }
        }

        /**
         * Obtiene un string/mensaje a partir de un objeto error de web sercice
         */
        fun getFuelError2String(context: Context?, error: FuelError?): String? {
            var errorString = context?.getString(R.string.general_error)
            try {
                errorString = String(error?.errorData!!)
                val isJson = errorString.isJSON
                if (isJson) {
                    val gson = Gson()
                    val jsonElement = gson.fromJson(errorString, JsonElement::class.java).asJsonObject
                    if (jsonElement.has("errorinesperado")) {
                        errorString = jsonElement.get("errorinesperado").asString
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (errorString != null && errorString.isEmpty()) {
                errorString = context?.getString(R.string.general_error)
            }
            return errorString
        }

        /**
         * Muestra un calendar picker
         * @param context contexto de la aplicación
         * @param default fecha por defecto
         * @param evento cuando se da ok al calendar picker
         */
        private fun showCalendarPicker(context: Context, default: Calendar?, dateListener: DatePickerDialog.OnDateSetListener): DatePickerDialog {
            var cal = Calendar.getInstance()
            if (default != null) {
                cal = default
            }
            val datePickerDialog = DatePickerDialog(context, dateListener, cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
            return datePickerDialog
        }

        /**
         * Muestra un calendar picker cuando se da tap sobre un edittext
         * @param context contexto de la aplicación
         * @param editText edittext sobre el cual se muestra la fecha
         * @param format formato de la fecha
         */
        fun showCalendarPickerFromEdittext(context: Context, editText: EditText, format: String): DatePickerDialog {
            val dateText = editText.text.toString()
            val dateCalendar = string2Calendar(dateText, format)
            return showCalendarPicker(context, dateCalendar, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(Calendar.YEAR, year)
                newCalendar.set(Calendar.MONTH, monthOfYear)
                newCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val newDateString = newCalendar.toFormat(format)
                editText.setText(newDateString)
            })
        }

        /**
         * Transforma un string fecha a un objeto calendar
         * @param dateString fecha en string
         * @param format formato de la fecha ingresada
         */
        private fun string2Calendar(dateString: String?, format: String?): Calendar {
            val cal = Calendar.getInstance()
            if (format != null && !format.isEmpty() && dateString != null && !dateString.isEmpty()) {
                val sdf = SimpleDateFormat(format, Locale.ENGLISH)
                val date = sdf.parse(dateString)
                cal.time = date
            }
            return cal
        }


        /**
         * Revisa si se está conectado a través de datos
         * @param context contexto de la aplicación
         * @param runAfterIfCheck acción a ejecutar despues de la confirmación del usuario
         */
        fun checkIfConnectedByData(context: Context, runAfterIfCheck: () -> Unit) {
            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val activeNetwork = connMgr?.activeNetworkInfo
                if (activeNetwork?.type == ConnectivityManager.TYPE_MOBILE) {
                    showConfirmDialogDataWarning(context, runAfterIfCheck)
                } else {
                    runAfterIfCheck()
                }
            } else {
                val mobile = connMgr?.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                if (mobile != null && mobile.isConnectedOrConnecting) {
                    showConfirmDialogDataWarning(context, runAfterIfCheck)
                } else {
                    runAfterIfCheck()
                }
            }
        }

        /**
         * Muestra un popup de confirmación acerca del consumo de datos
         * @param context contexto de la aplicación
         * @param listenerPositive evento a ejecutar si el usuario responde afirmativamente al confirm
         */
        fun showConfirmDialogDataWarning(context: Context, listenerPositive: () -> Unit) {
            showConfirmDialog(context,
                    R.string.data_consume, R.string.data_consume_warning_message,
                    R.string.continuee, { dialogInterface, i ->
                listenerPositive()
            }, R.string.no, { dialogInterface, i ->

            })
        }

        /**
         * Acción a ejecutar cuando se llega al final de un scroll
         * @param action acción a ejecutar
         */
        fun endScrollListener(action: () -> Unit): AbsListView.OnScrollListener {
            return object : AbsListView.OnScrollListener {
                override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                    if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                        if (action != null) {
                            action()
                        }
                    }
                }

                override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
                }

            }
        }

        /**
         * Va a la pantalla del listado de ofertas laborales
         * @param context contexto de la aplicación
         * @param keyWord palabra clave por la que filtrar
         * @param city Municipio por el cuál filtrar
         * @param convocatory Convocatoria por la cuál filtrar
         */
        fun goToWorkOffers(context: Context, keyWord: String = "", city: City.Complete? = null,
                           convocatory: Convocatory? = null) {
            val filter = Filter(keyWord = keyWord, entity = null, department = city?.department, city = city, convocatory = convocatory)
            val intent = Intent(context, WorkOffersActivity::class.java)
            intent.putExtra("filter", filter)
            context.startActivity(intent)
        }

        /**
         * Prepara un intent para ir al detalle de la oferta laboral
         * @param context contexto de la aplicación
         * @param idJob id de la oferta laboral
         * @param workOffer oferta laboral del listado
         * @param isFavorite si el empleo se puede seleccionar como favorito o no
         * @param idInscription si el empleo está asociado con una inscripción
         * @param statusInscription estado de la inscripción I,F,PI
         */
        fun getIntentForWorkDetail(context: Context?, idJob: String, workOffer: WorkOffer?,
                                   isFavorite: Boolean?, idInscription: String?,
                                   statusInscription: String?): Intent? {
            if (context != null) {

                val intent = Intent(context, DetailWorkOfferActivity::class.java)
                intent.putExtra("id", idJob)
                if (workOffer == null) {
                    intent.putExtra("work_offer", WorkOffer(id = idJob, favorite = false))
                } else {
                    intent.putExtra("work_offer", workOffer)
                }
                intent.putExtra("is_favorite", isFavorite)
                intent.putExtra("id_inscription", idInscription)
                intent.putExtra("status_inscription", statusInscription)
                //intent.addFlags()
                return intent
            } else {
                return null
            }
        }

        /**
         * Va a la pantalla de detalle de una convocatoria
         * @param context contexto de la aplicación
         * @param idConvocatory id de la convocatoria a mostrar
         */
        fun goToDetailConvocatorie(context: Context?, idConvocatory: String) {
            if (context != null) {
                val intent = Intent(context, DetailConvocatoryActivity::class.java)
                intent.putExtra("id", idConvocatory)
                context.startActivity(intent)
            }
        }

        /**
         * Muestra un popup para reintentar conectar en caso de haber un error de conexión
         * @param context contexto de la aplicación
         * @param retryAction acción a ejecutarse en
         */
        fun showErrorConectionConfirmToTry(context: Context?, retryAction: () -> Unit?,
                                           negationAction: () -> Unit?) {
            if (context != null) {
                showConfirmDialog(context, R.string.error_conection,
                        R.string.error_conection_message, R.string.retry,
                        { dialogInterface, i ->
                            retryAction()
                        }, R.string.no, { dialogInterface, i ->
                    negationAction()
                })
            }
        }

        /**
         * Muestra una alerta de error de conexión
         * @param context Activty desde el cuál se muestra el alert
         * @param retryAction acción a ejecutarse al dar aceptar en el popup
         */
        fun showErrorConectionAlertToTry(context: AppCompatActivity?, retryAction: () -> Unit?) {
            if (context != null) {
                showAlertDialog(context, R.string.error_conection,
                        R.string.error_conection_message, R.string.retry
                ) { _, _ ->
                    retryAction()
                }
            }
        }

        /**
         * Comprime una imagen para hacerla mas liviana
         * @param context contexto de la aplicación
         * @param fileToReduce archivo de imagen a comprimir
         */
        @Throws(IOException::class)
        fun compressImage(context: Context, fileToReduce: File): File {
            return Compressor(context)
                    .setMaxWidth(640)
                    .setMaxHeight(480)
                    .setQuality(75)
                    .setCompressFormat(Bitmap.CompressFormat.PNG)
                    .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES).absolutePath).compressToFile(fileToReduce)
        }

        /**
         * Abre una ventana con los archivos pdf disponibles en el dispositivo
         * @param activity activity desde la cuál se abre la pantalla
         * @param fragment fragment desde el cuál se abre la pantalla
         * @param typeFiles tipo de archivo a filtrarse
         * @param code request code para abrir la ventana de archivos
         */
        private fun openChooserDocuments(activity: AppCompatActivity? = null,
                                         fragment: androidx.fragment.app.Fragment? = null, typeFiles: String, code: Int) {
            val intent = if (activity != null) {
                Intent(activity, FilePickerActivity::class.java)
            } else {
                Intent(fragment?.context, FilePickerActivity::class.java)
            }

            intent.putExtra(FilePickerActivity.CONFIGS, Configurations.Builder()
                    .setCheckPermission(true)
                    .setShowImages(false)
                    .setShowAudios(false)
                    .setShowVideos(false)
                    .setShowFiles(true)
                    .setSuffixes("pdf")
                    .setSingleClickSelection(true)
                    .enableImageCapture(false)
                    .setMaxSelection(1)
                    .enableVideoCapture(false)
                    .setSkipZeroSizeFiles(true)
                    .build())
            if (activity != null) {
                activity.startActivityForResult(intent, code)
            } else {
                fragment?.startActivityForResult(intent, code)
            }

        }

        /**
         * Abre una ventana con los archivos pdf disponibles en el dispositivo
         * @param fragment fragment desde el cuál se abre la pantalla
         * @param typeFiles tipo de archivo a filtrarse
         * @param code request code para abrir la ventana de archivos
         */
        fun openChooserDocuments(fragment: Fragment?, typeFiles: String, code: Int) {
            openChooserDocuments(null, fragment, typeFiles, code)
        }

        /**
         * Abre una ventana con los archivos pdf disponibles en el dispositivo
         * @param activity activity desde la cuál se abre la pantalla
         * @param typeFiles tipo de archivo a filtrarse
         * @param code request code para abrir la ventana de archivos
         */
        fun openChooserDocuments(activity: AppCompatActivity?, typeFiles: String, code: Int) {
            openChooserDocuments(activity, null, typeFiles, code)
        }

        /**
         * Descarga un archivo desde una url mostrando el avance en una notificación
         * del sistema
         * @param context contexto de la aplicación
         * @param urlFile url del archivo a descargar
         * @param nameFile nombre del archivo cuando se decargue
         */
        private fun downloadFile(context: Context, urlFile: String, nameFile: String) {

            val channelId = context.getString(R.string.download_notification_channel_id)
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationBuilder = NotificationCompat.Builder(context, channelId)
            notificationBuilder.priority = Notification.PRIORITY_MAX
            notificationBuilder.setProgress(100, 0, false)
            notificationBuilder.setContentTitle(context.getString(R.string.downloading_file))
            notificationBuilder.setSmallIcon(R.drawable.ic_stat_ic_notification)
            notificationBuilder.setAutoCancel(true)
            notificationBuilder.setSound(defaultSoundUri)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(10, notificationBuilder.build())

            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nameFile)
            Fuel.download(urlFile).destination { response, url ->
                file
            }.progress { readBytes, totalBytes ->

                val progress = Math.abs(readBytes * 100 / totalBytes)
                notificationBuilder.setContentText("$progress%")
                notificationBuilder.setProgress(100, progress.toInt(), false)

                Log.d(TAG, "Downloading: $readBytes / $totalBytes")

                /*notificationBuilder.setProgress(100, 0,true)
                notificationBuilder.setContentText("")*/
                notificationBuilder.setSound(null)
                notificationManager.notify(10, notificationBuilder.build())
            }.response { req, res, result ->
                Log.d(TAG, "Download complete")
                Handler().postDelayed({
                    val myMime = MimeTypeMap.getSingleton()
                    val intent = Intent(Intent.ACTION_VIEW)
                    val mimeType = myMime.getMimeTypeFromExtension(file.absolutePath?.getExtensionPathFile()?.substring(1))
                    intent.setDataAndType(FileProvider.getUriForFile(context, context.applicationContext.packageName + GenericFileProvider.PROVIDER_NAME, file), mimeType)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    val pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                            PendingIntent.FLAG_ONE_SHOT)
                    notificationBuilder.setContentIntent(pendingIntent)
                    notificationBuilder.setProgress(100, 100, false)
                    notificationBuilder.setContentTitle("")
                    notificationBuilder.setContentText(context.getString(R.string.download_completed))
                    notificationBuilder.setSound(defaultSoundUri)
                    notificationManager.notify(10, notificationBuilder.build())
                }, 500)
            }
        }

        /**
         * Revisa los permisos de descarga del archivo
         * @param activity activity desde la cuál se decarga el archivo
         * @param urlFile url del archivo a decargar
         * @param nameFile nombre del archivo cuando se descargue
         */
        fun checkPermissionsAndDownloadFile(activity: Activity, urlFile: String, nameFile: String) {
            Dexter.withActivity(activity)
                    .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(object : PermissionListener, MultiplePermissionsListener {
                        override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                            Log.d(TAG, "onPermissionRationaleShouldBeShown")
                        }

                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            Log.d(TAG, "onPermissionsChecked")
                            downloadFile(activity, urlFile, nameFile)
                        }

                        override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                            Log.d(TAG, "onPermissionGranted")
                            downloadFile(activity, urlFile, nameFile)
                        }

                        override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                            Log.d(TAG, "onPermissionRationaleShouldBeShown")
                        }

                        override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                            Log.d(TAG, "onPermissionDenied")
                        }

                    })
                    .check()
        }

        /**
         * Revisa si el archivo seleccionado supera el tamaño máximo permitido
         * @param context contexto de la aplicación
         * @param file Archivo seleccionado por el usuario
         */
        fun checkMaxFileSize(context: AppCompatActivity?, file: File?): Boolean {
            var allowSize = true
            if (file != null) {
                if (file.exists()) {
                    //val maxSize = BuildConfig.MAX_SIZE_FILES_TO_UPLOAD_KB
                        val maxSize =2024
                    val fileSizeKB = file.length() / 1024
                    if (fileSizeKB > maxSize) {
                        showAlertDialog(context, context?.getString(R.string.file_size),
                                context?.getString(R.string.file_size_message, maxSize), context?.getString(R.string.ok))
                        { dialogInterface, i ->

                        }
                        allowSize = false
                    }
                } else {
                    showAlertDialog(context, R.string.file, R.string.file_selected_was_deleted, R.string.ok) { dialogInterface, i ->
                    }
                    allowSize = false
                }
            } else if (context != null) {
                showAlertDialog(context, R.string.file, R.string.no_data_file_message, R.string.ok)
                { dialogInterface, i ->

                }
                allowSize = false
            }
            return allowSize
        }

        /**
         * Abre la pantalla con algún listado de recursos SIMO
         * Paises, Departamentos, Municipios, Niveles, entidades
         * @param fragment Fragment desde el cuál se abre el listado
         * @param activity Activity desde el cuál se abre el listado
         * @param typeResource Tipo de listado de recursos a mostrar
         * @param idFilter si los recursos necesitan un id para filtrarse
         * @param query palabra clave para filtrar
         * @param requestCode request code
         */
        fun goToSpinnerListView(
            fragment: Fragment? = null, activity: AppCompatActivity? = null,
            typeResource: Int, idFilter: String? = null, query: String?, requestCode: Int) {
            //val query_flat =removerTildes(query)
            val intent = if (fragment != null) {
                Intent(fragment.context, SearchableListViewActivity::class.java)
            } else {
                Intent(activity, SearchableListViewActivity::class.java)
            }
            intent.putExtra("type_resource", typeResource)
            intent.putExtra("query", query)
            intent.putExtra("id_filter", idFilter)
            if (fragment != null) fragment.startActivityForResult(intent, requestCode)
            else activity?.startActivityForResult(intent, requestCode)
        }

        fun removerTildes(cadena: String) : String {
            var out =cadena
            out=out.replace("Á", "A")
            out=out.replace("É", "E")
            out=out.replace("Í", "I")
            out=out.replace("Ó", "O")
            out=out.replace("Ú", "U")
            out=out.replace("á", "a")
            out=out.replace("é", "e")
            out=out.replace("í", "i")
            out=out.replace("ó", "o")
            out=out.replace("ú", "u")
            return out
        }



    }

    /**
     * Se ejecuta cuando alguna activity es creada
     */

    override fun onActivityCreated(p0: Activity, savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {

            val simo = savedInstanceState.get("simo") as SIMO
            if (simo != null) {
                SIMO.instance = simo
            }
        }
    }

    override fun onActivityStarted(p0: Activity) {
        Log.d("DEV","Activity "+p0.toString())
    }

    override fun onActivityResumed(p0: Activity) {
        Log.d("DEV","ActivityResumed "+p0.toString())
    }

    override fun onActivityPaused(p0: Activity) {
        Log.d("DEV","ActivityPaused "+p0.toString())
    }


    /**
     * Se ejecuta cuando alguna activity es detenida
     */
    override fun onActivityStopped(p0: Activity) {
        Log.d("DEV","ActivityStoped "+p0.toString())
    }

    /**
     * Se ejecuta cuando la aplicación se va a background
     */

    override fun onActivitySaveInstanceState(p0: Activity, outState: Bundle) {
        try {
            val simo = SIMO.instance
            outState?.putParcelable("simo", simo)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityDestroyed(p0: Activity) {
        Log.d("DEV","ActivityDestroyed "+p0.toString())
    }






}