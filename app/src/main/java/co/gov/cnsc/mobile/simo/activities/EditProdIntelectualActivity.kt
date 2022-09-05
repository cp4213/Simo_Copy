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
import co.gov.cnsc.mobile.simo.models.IdDescription
import co.gov.cnsc.mobile.simo.models.SIMO
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
import kotlinx.android.synthetic.main.activity_edit_prod_intelectual.*
import kotlinx.android.synthetic.main.activity_edit_prod_intelectual.buttonUpload
import kotlinx.android.synthetic.main.activity_edit_prod_intelectual.editTextAttachment
import kotlinx.android.synthetic.main.activity_edit_prod_intelectual.linearForm
import kotlinx.android.synthetic.main.activity_edit_prod_intelectual.textInputAttachment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Esta clase contiene la funcionalidad para la pantalla de editar o agregar una 'Producción Intelectual'
 * a la hoja de vida
 */
class EditProdIntelectualActivity : SIMOActivity() {

    /**
     * Objeto 'Producción Intelectual' a mostrar en la pantalla
     */
    private var credential: Credential? = null

    /**
     * Posición de la 'Producción Intelectual' en el listado de la pantalla anterior
     */
    private var position: Int? = null

    /**
     * Archivo de soporte seleccionado
     */
    private var fileProdIntelectual: File? = null

    /**
     * Objeto que hace referencia al archivo que se subió al servidor
     */
    private var fileProdIntelectualUploaded: co.gov.cnsc.mobile.simo.models.File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_prod_intelectual)
        showToolbarBack()
        credential = intent?.extras?.getParcelable("credential")
        position = intent?.extras?.getInt("position", -1)
        if (credential == null) {
            setTextTitleToolbar(R.string.add_prod_intelectual)
            buttonUpload?.setText(R.string.add)
            AnalyticsReporter.screenAddIntelectualProduct(this)
        } else {
            setTextTitleToolbar(R.string.edit_prod_intelectual)
            buttonUpload?.setText(R.string.update)
            AnalyticsReporter.screenEditIntelectualProduct(this)
        }
        configureUI()
        getTypeProducts()
    }

    /**
     * Configura la interfaz gráfica de la pantalla
     */
    fun configureUI() {
        //si existe documento adjunto para descarga se activa el botón download
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
     * Obtiene un listado con todos los tipos de productos
     * en este caso producciones intelectuales
     */
    private fun getTypeProducts() {
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.getTypeProducts({ typeProducts ->
            ProgressBarDialog.stopProgressDialog()
            spinnerTypeProduct?.items = typeProducts
            paint()
        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            SIMOApplication.showFuelError(this, fuelError)
            finish()
        })
    }

    /**
     * Pinta la información de la 'Producción Intelectual' en la pantalla en caso de la opción editar
     */
    private fun paint() {
        credential?.let {
            spinnerTypeProduct?.selectedItem = credential?.typeProdIntelectual
            editNumberIdentifier?.setText(credential?.identifier)
            editBibliographic?.setText(credential?.bibliographicRecord)
            editTextAttachment?.setText(credential?.document?.name)
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
                fileProdIntelectual = File(cacheDir, contentResolver.getFileName(filedoc!!))
                val outputStream = FileOutputStream(fileProdIntelectual)
                inputStream.copyTo(outputStream)
                if (SIMOApplication.checkMaxFileSize(this, fileProdIntelectual)) {

                    uploadDocumentProdIntectual()
                }
            }
        })


    /**
     * Sube un archivo seleccionado; desde el móvil al servidor
     */
    private fun uploadDocumentProdIntectual() {
        editTextAttachment?.error = null
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.uploadFilePDF(fileProdIntelectual!!, { file ->
            ProgressBarDialog.stopProgressDialog()
            fileProdIntelectualUploaded = file
            editTextAttachment?.setText(contentResolver.getFileName(filedoc!!))
            fileProdIntelectual = null
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
     * Valida el formulario de 'Producción Intelecutal' y sube la información al servidor
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
        if (editNumberIdentifier?.text?.matches(SIMOApplication.REGEX_FOR_IDENTIFIER_NUMBER.toRegex()) == false) {
            textInputNumberIdentifier?.error = getString(R.string.number_identifier_requirements)
            validate = false
        }
        if (editBibliographic?.text.isNullOrBlank()) {
            textInputBibliographic.error = getString(R.string.not_valid_field)
            validate = false
        }
        if (credential?.document == null && fileProdIntelectualUploaded == null) {
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
     * Sube la información del formulario al servidor para actualizar 'Producción Intelectual' o para crearlo
     */
    private fun updateInformation() {
        val idUser = SIMO.instance.session?.idUser
        val username = SIMO.instance.session?.username
        val idCredential = credential?.id
        val prodIntectual = spinnerTypeProduct?.selectedItem as IdDescription?
        val idTypeProdIntectual = prodIntectual?.id
        val numberIdentifier = editNumberIdentifier?.text.toString()
        val quoteBibliographic = editBibliographic?.text.toString()

        ProgressBarDialog.startProgressDialog(this)
        RestAPI.createOrUpdateProdIntelectual(idUser = idUser, username = username, idCredential = idCredential,
                idTypeProduct = idTypeProdIntectual, numberIdentifier = numberIdentifier,
                quoteBibliographic = quoteBibliographic, documentAttachment = credential?.document,
                stageIdAttachment = fileProdIntelectualUploaded?.stageId,
                success = { credential ->
                    ProgressBarDialog.stopProgressDialog()
                    var messageId: Int = R.string.ip_created_successfully
                    if (idCredential != null) {
                        messageId = R.string.ip_updated_successfully
                    }
                    Toast.makeText(this, messageId, Toast.LENGTH_LONG).show()
                    returnToBack(credential)
                },
                error = { fuelError ->
                    ProgressBarDialog.stopProgressDialog()
                    SIMOApplication.showFuelError(this, fuelError)
                }
        )

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
                SIMOApplication.showConfirmDialog(this, R.string.delete_prod_intelectual, R.string.are_you_sure_delete_item_cv, R.string.yes,
                        { dialogInterface, i ->
                            deleteProdIntelectual()
                        }, R.string.no,
                        { dialogInterface, i ->

                        })
            }
            else -> return super.onOptionsItemSelected(item)

        }
        return true
    }

    /**
     * Llama a un servicio web para borrar dicho registro de 'Producción Intelectual' del servidor
     */
    private fun deleteProdIntelectual() {
        val idUser = SIMO.instance.session?.idUser
        val idCredential = credential?.id
        ProgressBarDialog.startProgressDialog(this)
        RestAPI.deleteProductIntelectual(idUser, idCredential, { response ->
            ProgressBarDialog.stopProgressDialog()
            Toast.makeText(this, R.string.prod_intelectual_deleted, Toast.LENGTH_LONG).show()
            returnToBack(credential)
        }, { fuelError ->
            ProgressBarDialog.stopProgressDialog()
            SIMOApplication.showFuelError(this, fuelError)
        })
    }

    /**
     * Vuelve a la pantalla anterior envía la 'Producción Intelectual' y la posición en el listado
     */
    private fun returnToBack(credential: Credential?) {
        val intent = Intent()
        intent.putExtra("credential", credential)
        intent.putExtra("position", position)
        setResult(RESULT_OK, intent)
        finish()
    }


    companion object {
        const val REQUEST_CODE_ATTACHMENT = 0
    }
}
