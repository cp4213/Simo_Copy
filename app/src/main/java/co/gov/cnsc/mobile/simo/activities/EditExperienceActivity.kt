package co.gov.cnsc.mobile.simo.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOActivity
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.analitycs.AnalyticsReporter
import co.gov.cnsc.mobile.simo.models.Credential
import co.gov.cnsc.mobile.simo.models.SIMO
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.network.RestAPI.Companion.deleteExperience
import co.gov.cnsc.mobile.simo.util.ProgressBarDialog
import co.gov.cnsc.mobile.simo.views.TextInputLayout
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.model.MediaFile
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_edit_experience.*
import kotlinx.android.synthetic.main.activity_edit_experience.buttonUpload
import kotlinx.android.synthetic.main.activity_edit_experience.editTextAttachment
import kotlinx.android.synthetic.main.activity_edit_experience.linearForm
import kotlinx.android.synthetic.main.activity_edit_experience.textInputAttachment
import kotlinx.android.synthetic.main.activity_edit_formation.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*


/**
 * Esta clase contiene la funcionalidad para la pantalla de editar o agregar una experiencia
 * a la hoja de vida
 */
class EditExperienceActivity : SIMOActivity() {

    /**
     * Experiencia a mostrar en la pantalla
     */
    private var credential: Credential? = null

    /**
     * Posición de la experiencia en el listado de la pantalla anterior
     */
    private var position: Int? = null

    /**
     * Archivo de soporte seleccionado
     */
    private var fileExperience: File? = null

    /**
     * Objeto que hace referencia al archivo que se subió al servidor
     */
    private var fileExperienceUploaded: co.gov.cnsc.mobile.simo.models.File? = null


    /**
     * Función que pinta el layout de acuerdo con su naturaleza: nueva experiencia (agregar), experiencia existente (editar)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("DEV","EditExperienceA")
        setContentView(R.layout.activity_edit_experience)
        showToolbarBack()
        credential = intent?.extras?.getParcelable("credential")
        position = intent?.extras?.getInt("position", -1)
        if (credential == null) {
            setTextTitleToolbar(R.string.add_experience)
            buttonUpload?.setText(R.string.add)
            AnalyticsReporter.screenAddExperience(this)
        } else {
            setTextTitleToolbar(R.string.edit_experience)
            buttonUpload?.setText(R.string.update)
            AnalyticsReporter.screenEditExperience(this)
        }
        configureUI()
        paint()
    }

    /**
     * Configura la interfaz gráfica de la pantalla: asigna eventos a los checkbox, y campos de textos
     */
    fun configureUI() {
        checkIsTeacherCathedra?.setOnCheckedChangeListener { buttonView, isChecked ->
            val visibility = if (isChecked) {
                View.VISIBLE
            } else {
                View.GONE
            }
            textInputWorkedTotal?.visibility = visibility
        }
        checkCurrentEmploy?.setOnCheckedChangeListener { buttonView, isChecked ->
            val visibility = if (isChecked) {
                View.GONE
            } else {
                View.VISIBLE
            }
            textInputDateExit?.visibility = visibility
        }

        checkFullTime?.setOnCheckedChangeListener { buttonView, isChecked ->
            val visibility = if (isChecked) {
                View.GONE
            } else {
                View.VISIBLE
            }
            textInputHoursWork?.visibility = visibility
        }

        editDateEnter?.setOnClickListener {
            val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(this, editDateEnter, "yyyy-MM-dd")
            val maxDateEnterCal = Calendar.getInstance()
            maxDateEnterCal.add(Calendar.YEAR, -52)
            datePickerDialog.datePicker.minDate = maxDateEnterCal.time.time
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().time.time
        }
        editDateExit?.setOnClickListener {
            val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(this, editDateExit, "yyyy-MM-dd")
            val maxDateExitCal = Calendar.getInstance()
            maxDateExitCal.add(Calendar.YEAR, -52)
            datePickerDialog.datePicker.minDate = maxDateExitCal.time.time
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().time.time
        }

        editDateCertification?.setOnClickListener {
            val datePickerDialog = SIMOApplication.showCalendarPickerFromEdittext(this, editDateCertification, "yyyy-MM-dd")
            val maxDateCertCal = Calendar.getInstance()
            maxDateCertCal.add(Calendar.YEAR, -52)
            datePickerDialog.datePicker.minDate = maxDateCertCal.time.time
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().time.time
        }
        editTextAttachment?.setOnClickListener {
            if (credential?.document != null) {
                SIMOApplication.checkIfConnectedByData(this) {
                    SIMOApplication.checkPermissionsAndDownloadFile(this, credential?.urlDocument!!,
                            credential?.nameDocument!!)
                }
            }
        }
    }

    /**
     * Pinta la información de la experiencia en la pantalla en caso de la opción editar
     */
    fun paint() {
        credential?.let {
            checkIsTeacherCathedra?.isChecked = credential?.timeHours == true
            editCompany?.setText(credential?.company)
            editCharge?.setText(credential?.charge)
            checkCurrentEmploy?.isChecked = credential?.actual == true
            checkFullTime?.isChecked = credential?.fullTime == true
            editHoursWork?.setText(credential?.dailyHours)
            editDateEnter?.setText(credential?.dateEntry)
            editDateExit?.setText(credential?.dateEnd)
            editWorkedTotal?.setText(credential?.time)
            editTextAttachment?.setText(credential?.document?.name)
            editDateCertification?.setText(credential?.dateCertification)

        }
        window?.decorView?.clearFocus()
    }

    /**
     * Pide confirmación en caso de que el usuario esté usando datos,
     * solicita permiso de acceso a los archivos,
     * y abre un explorador de archivos pdf
     */
    fun onUploadFile(button: View) {
        SIMOApplication.checkIfConnectedByData(this) {
            Dexter.withActivity(this)
                    .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(object : PermissionListener, MultiplePermissionsListener {
                        override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                            Log.d(SIMOApplication.TAG, "onPermissionRationaleShouldBeShown")
                            token?.continuePermissionRequest()
                        }

                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            Log.d(SIMOApplication.TAG, "onPermissionsChecked")
                            if (report?.areAllPermissionsGranted() == true) {
                                SIMOApplication.FilePickerNew(resultLauncher)
                            } else {
                                Log.d(SIMOApplication.TAG, "onPermissionDenied")
                            }
                        }

                        override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                            Log.d(SIMOApplication.TAG, "onPermissionGranted")
                            SIMOApplication.FilePickerNew(resultLauncher)
                        }

                        override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                            Log.d(SIMOApplication.TAG, "onPermissionRationaleShouldBeShown")
                        }

                        override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                            Log.d(SIMOApplication.TAG, "onPermissionDenied")
                        }

                    })
                    .check()

        }
    }

    private var filedoc: Uri?=null
    /**
     * Maneja la respuesta al seleccionar un archivo pdf del dispositivo
     */
    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),{ result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null ) {
                filedoc =result.data!!.data
                val parcelFileDescriptor =contentResolver.openFileDescriptor(filedoc!!, "r", null)
                val inputStream = FileInputStream(parcelFileDescriptor!!.fileDescriptor)
                fileExperience = File(cacheDir, contentResolver.getFileName(filedoc!!))
                val outputStream = FileOutputStream(fileExperience)
                inputStream.copyTo(outputStream)
                if (SIMOApplication.checkMaxFileSize(this, fileExperience)) {

                    uploadDocumentExperience()
                }
            }
        })


    /**
     * Sube un archivo seleccionado al servidor
     */
    private fun uploadDocumentExperience() {
        editTextAttachment?.error = null
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.uploadFilePDF(fileExperience!!, { file ->
            ProgressBarDialog.stopProgressDialog()
            fileExperienceUploaded = file
            editTextAttachment?.setText(contentResolver.getFileName(filedoc!!))
            fileExperience = null
        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            if (fuelError.exception.message != null) {
                Toast.makeText(this, fuelError.exception.message, Toast.LENGTH_LONG).show()
            } else {
                SIMOApplication.showFuelError(this, fuelError)
            }
        })
    }

    /**
     * Valida el formulario de la experiencia sube la información al servidor
     */
    fun onUploadData(button: View) {
        cleanErrors()
        val validate = validateForm()
        if (validate) {
            updateInformation()
        }
    }

    /**
     * Valida la información en el formulario
     */
    private fun validateForm(): Boolean {

        var validate = true

        //Common fields

        //checkTeacher
        if (checkIsTeacherCathedra?.isChecked == true) {
            if (editWorkedTotal?.text?.matches(SIMOApplication.REGEX_FOR_HOURS_WORKED.toRegex()) == false) {
                textInputWorkedTotal?.error = getString(R.string.hours_worked_requirements)
                validate = false
            }
        }

        //Company
        if (editCompany?.text?.matches(SIMOApplication.REGEX_FOR_COMPANY.toRegex()) == false) {
            textInputCompany?.error = getString(R.string.company_requirements)
            validate = false
        }

        //Charge
        if (editCharge?.text?.matches(SIMOApplication.REGEX_FOR_EMPLOYMENT.toRegex()) == false) {
            textInputCharge?.error = getString(R.string.employment_requirements)
            validate = false
        }

        //checkFullTime
        if (checkFullTime?.isChecked == false) {

            if (Integer.parseInt(editHoursWork?.text.toString())<1 || Integer.parseInt(editHoursWork?.text.toString())>8){
            //if (editHoursWork?.text?.matches(SIMOApplication.REGEX_FOR_AVERAGE_HOURS.toRegex()) == false) {
                textInputHoursWork?.error = getString(R.string.average_nofullt_hours_requirements)
                validate = false
            }
        }

        //Dates

        //Date Enter
        if (editDateEnter?.text.isNullOrBlank()) {
            textInputDateEnter.error = getString(R.string.select_value_required)
            validate = false
        } /*else {

            if (editDateEnter.text.toString() >= editDateExit.text.toString()) {
                textInputDateEnter?.error = getString(R.string.lower_date_exit)
                validate = false
            }

        }*/

        //Date Certification
        if (editDateCertification?.text.isNullOrBlank()) {
            textInputDateCertification.error = getString(R.string.select_value_required)
            validate = false
        } else {

            if (editDateCertification.text.toString() <= editDateExit.text.toString()) {
                textInputDateCertification?.error = getString(R.string.exit_date_lower_than_expedition_date)
                validate = false
            }

        }

        //Attachment
        if (credential?.document == null && fileExperienceUploaded == null) {
            textInputAttachment.error = getString(R.string.upload_document_required)
            validate = false
        }

        //checked Actual
        if (checkCurrentEmploy?.isChecked == true) {

            //Date Entry
            if (editDateEnter?.text.isNullOrBlank()) {
                textInputDateEnter.error = getString(R.string.select_value_required)
                validate = false
            } else {

                if (editDateEnter.text.toString() >= editDateCertification.text.toString()) {
                    textInputDateEnter?.error = getString(R.string.lower_date_entry)
                    validate = false
                }

            }

            //Date Certification
            if (editDateCertification?.text.isNullOrBlank()) {
                textInputDateCertification.error = getString(R.string.select_value_required)
                validate = false
            } else {

                if (editDateCertification.text.toString() <= editDateEnter.text.toString()) {
                    textInputDateCertification?.error = getString(R.string.entry_date_lower_than_expedition_date)
                    validate = false
                }

            }

        } else {

            //Date Exit
            if (editDateExit?.text.isNullOrBlank()) {
                textInputDateExit.error = getString(R.string.select_value_required)
                validate = false
            } else {

                if (editDateExit.text.toString() <= editDateEnter.text.toString()) {
                    textInputDateExit?.error = getString(R.string.lower_date_xp)
                    validate = false
                }

            }
        }

        return validate
    }


    /**
     * Esconde los labels de errores en cada uno de los campos de formulario
     */
    private fun cleanErrors() {
        textInputAttachment?.error = null
        val count = linearForm?.childCount
        count?.let {
            for (position in 0..count) {
                val view = linearForm?.getChildAt(position)
                if (view is TextInputLayout) {
                    view.error = null
                }
            }
        }
    }

    /**
     * Sube la información del formulario al servidor para actualizar una experiencia o para crearla
     */
    private fun updateInformation() {
        val idUser = SIMO.instance.session?.idUser
        val username = SIMO.instance.session?.username
        val idCredential = credential?.id
        val company = editCompany?.text.toString()
        val charge = editCharge?.text.toString()
        var hoursTotal: Int? = null
        if (checkIsTeacherCathedra?.isChecked == true) {
            hoursTotal = editWorkedTotal?.text?.toString()?.toInt()
        }

        var hoursDaily: Int? = null
        if (checkFullTime?.isChecked == false) {
            hoursDaily = editHoursWork?.text?.toString()?.toInt()
        }

        val dateEntry = editDateEnter?.text?.toString()

        var dateExit: String? = null
        if (checkCurrentEmploy?.isChecked == false) {
            dateExit = editDateExit?.text?.toString()
        }

        val stageIdAttachment = fileExperienceUploaded?.stageId
        val dateExpedition = editDateCertification?.text?.toString()

        ProgressBarDialog.startProgressDialog(this)
        RestAPI.createOrUpdateExperience(idUser = idUser, username = username, idCredential = idCredential,
                company = company, charge = charge, hoursTotal = hoursTotal, hoursDailyAverage = hoursDaily,
                dateEntry = dateEntry, dateExit = dateExit, documentAttachment = credential?.document,
                stageIdAttachment = stageIdAttachment, dateExpedition = dateExpedition,
                success = { credential ->
                    ProgressBarDialog.stopProgressDialog()
                    var messageId: Int = R.string.experience_created_successfully
                    if (idCredential != null) {
                        messageId = R.string.experience_updated_successfully
                    }
                    Toast.makeText(this, messageId, Toast.LENGTH_LONG).show()
                    returnToBack(credential)
                },
                error = { fuelError ->
                    ProgressBarDialog.stopProgressDialog()
                    SIMOApplication.showFuelError(this, fuelError)
                })

    }

    /**
     * Vuelve a la pantalla anterior envía la experiencia y la posición en el listado
     */
    private fun returnToBack(credential: Credential?) {
        val intent = Intent()
        intent.putExtra("credential", credential)
        intent.putExtra("position", position)
        setResult(RESULT_OK, intent)
        finish()
    }

    /**
     * Pone el menú de eliminar en la parte superior derecha de la pantalla
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (credential != null)
            menuInflater.inflate(R.menu.delete, menu)
        return true
    }

    /**
     * Evento cuando se da tap sobre la opción eliminar de la pantalla
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.menu_delete -> {
                SIMOApplication.showConfirmDialog(this, R.string.delete_experience, R.string.are_you_sure_delete_item_cv, R.string.yes,
                        { dialogInterface, i ->
                            deleteExperience()
                        }, R.string.no,
                        { dialogInterface, i ->

                        })
            }
            else -> return super.onOptionsItemSelected(item)

        }
        return true
    }

    /**
     * Llama a un servicio web para borrar dicho registro de experiencia del servidor
     */
    private fun deleteExperience() {
        val idUser = SIMO.instance.session?.idUser
        val idCredential = credential?.id
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.deleteExperience(idUser, idCredential, { response ->
            ProgressBarDialog.stopProgressDialog()
            Toast.makeText(this, R.string.experience_deleted, Toast.LENGTH_LONG).show()
            returnToBack(credential)
        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            SIMOApplication.showFuelError(this, fuelError)
        })

    }

    companion object {
        const val REQUEST_CODE_ATTACHMENT = 0
    }
}
