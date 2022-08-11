package co.gov.cnsc.mobile.simo.util

import androidx.core.content.FileProvider

/**
 * Archivo File Provider necesario para poder acceder a los archivos del dispositivo
 * en versiones de android 8 o superior desde la librería de selección de PDF
 */
class FilePickerProvider : FileProvider()