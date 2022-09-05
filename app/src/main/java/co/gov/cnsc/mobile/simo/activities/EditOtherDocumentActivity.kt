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
import co.gov.cnsc.mobile.simo.models.TypeData
import co.gov.cnsc.mobile.simo.network.RestAPI
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
import kotlinx.android.synthetic.main.activity_edit_formation.*
import kotlinx.android.synthetic.main.activity_edit_other_document.*
import kotlinx.android.synthetic.main.activity_edit_other_document.buttonUpload
import kotlinx.android.synthetic.main.activity_edit_other_document.editTextAttachment
import kotlinx.android.synthetic.main.activity_edit_other_document.linearForm
import kotlinx.android.synthetic.main.activity_edit_other_document.textInputAttachment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Esta clase contiene la funcionalidad para la pantalla de editar o agregar 'Otro Documento'
 * a la hoja de vida
 */
class EditOtherDocumentActivity : SIMOActivity() {

    /**
     * Objeto 'Otro documento' a mostrar en la pantalla
     */
    private var credential: Credential? = null

    /**
     * Posición del 'Otro documento' en el listado de la pantalla anterior
     */
    private var position: Int? = null

    /**
     * Archivo de soporte seleccionado
     */
    private var fileOtherDocument: File? = null

    /**
     * Objeto que hace referencia al archivo que se subió al servidor
     */
    private var fileOtherDocumentUploaded: co.gov.cnsc.mobile.simo.models.File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_other_document)
        showToolbarBack()
        credential = intent?.extras?.getParcelable("credential")
        position = intent?.extras?.getInt("position", -1)
        if (credential == null) {
            setTextTitleToolbar(R.string.add_other_document)
            buttonUpload?.setText(R.string.add)
            AnalyticsReporter.screenAddOtherDocument(this)
        } else {
            setTextTitleToolbar(R.string.edit_other_document)
            buttonUpload?.setText(R.string.update)
            AnalyticsReporter.screenEditOtherDocument(this)
        }
        configureUI()
        getTypeInformation()
    }

    /**
     * Configura la interfaz gráfica de la pantalla
     */
    private fun configureUI() {
        editTextAttachment?.setOnClickListener {
            if (credential?.document != null) {
                SIMOApplication.checkIfConnectedByData(this) {
                    SIMOApplication.checkPermissionsAndDownloadFile(this, credential?.urlDocument!!, credential?.nameDocument!!)
                }
            }
        }
    }

    /**
     * Obtiene un listado con todos los tipos de información
     * en este caso documentos
     */
    private fun getTypeInformation() {
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.getTypeInformation({ typeProducts ->
            ProgressBarDialog.stopProgressDialog()
            spinnerTypeDocument?.items = typeProducts
            paint()
        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            SIMOApplication.showFuelError(this, fuelError)
            finish()
        })
    }

    /**
     * Pinta la información del 'Otro Documento' en la pantalla en caso de la opción editar
     */
    fun paint() {
        credential?.let {
            spinnerTypeDocument?.selectedItem = credential?.typeInformation
            editTextAttachment?.setText(credential?.document?.name)
        }
        window?.decorView?.clearFocus()
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
                fileOtherDocument = File(cacheDir, contentResolver.getFileName(filedoc!!))
                val outputStream = FileOutputStream(fileOtherDocument)
                inputStream.copyTo(outputStream)
                if (SIMOApplication.checkMaxFileSize(this, fileOtherDocument)) {

                    uploadDocumentOther()
                }
            }
        })

    /**
     * Pide confirmación en caso de que el usuario esté usando datos,
     * solicita permiso de acceso a los archivos,
     * y abre un explorador de archivos pdf
     */
    fun onUploadFile(button: View) {
        SIMOApplication.checkIfConnectedByData(this) {
            Dexter.withContext(this)
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


    /**
     * Sube un archivo seleccionado al servidor
     */
    private fun uploadDocumentOther() {
        editTextAttachment?.error = null
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.uploadFilePDF(fileOtherDocument!!, { file ->
            ProgressBarDialog.stopProgressDialog()
            fileOtherDocumentUploaded = file
            editTextAttachment?.setText(contentResolver.getFileName(filedoc!!))
            fileOtherDocument = null
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
     * Valida el formulario de 'Otro Documento' y sube la información al servidor
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
        if (spinnerTypeDocument?.adapter == null) {
            Toast.makeText(this, R.string.a_type_document_is_necessary, Toast.LENGTH_SHORT).show()
            validate = false
        }
        if (credential?.document == null && fileOtherDocumentUploaded == null) {
            textInputAttachment.error = getString(R.string.upload_document_required)
            validate = false
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
     * Sube la información del formulario al servidor para actualizar 'Otro documento' o para crearlo
     */
    private fun updateInformation() {
        val idUser = SIMO.instance.session?.idUser
        val username = SIMO.instance.session?.username
        val idCredential = credential?.id
        val typeOtherDocument = spinnerTypeDocument?.selectedItem as TypeData?
        val idTypeOtherDocument = typeOtherDocument?.id

        ProgressBarDialog.startProgressDialog(this)
        RestAPI.createOrUpdateOtherDocument(idUser = idUser, username = username, idCredential = idCredential,
                idTypeDocument = idTypeOtherDocument, documentAttachment = credential?.document,
                stageIdAttachment = fileOtherDocumentUploaded?.stageId,
                success = { credential ->
                    ProgressBarDialog.stopProgressDialog()
                    var messageId: Int = R.string.document_created_successfully
                    if (idCredential != null) {
                        messageId = R.string.document_updated_successfully
                    }
                    Toast.makeText(this, messageId, Toast.LENGTH_LONG).show()
                    returnToBack(credential)
                },
                error = { fuelError ->
                    ProgressBarDialog.stopProgressDialog()
                    if (fuelError.exception.message != null) {
                        Toast.makeText(this, fuelError.exception.message, Toast.LENGTH_LONG).show()
                    } else {
                        SIMOApplication.showFuelError(this, fuelError)
                    }
                }
        )
    }

    /**
     * Vuelve a la pantalla anterior envía el 'Otro Documento' y la posición en el listado
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
                SIMOApplication.showConfirmDialog(this, R.string.delete_other_document, R.string.are_you_sure_delete_item_cv, R.string.yes,
                        { dialogInterface, i ->
                            deleteOtherDocument()
                        }, R.string.no,
                        { dialogInterface, i ->

                        })
            }
            else -> return super.onOptionsItemSelected(item)

        }
        return true
    }

    /**
     * Llama a un servicio web para borrar dicho registro de 'Otro Documento' del servidor
     */
    private fun deleteOtherDocument() {
        val idUser = SIMO.instance.session?.idUser
        val idCredential = credential?.id
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.deleteOtherDocument(idUser, idCredential, { response ->
            ProgressBarDialog.stopProgressDialog()
            Toast.makeText(this, R.string.other_document_deleted, Toast.LENGTH_LONG).show()
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
