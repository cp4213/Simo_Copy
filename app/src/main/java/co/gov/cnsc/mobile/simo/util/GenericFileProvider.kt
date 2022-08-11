package co.gov.cnsc.mobile.simo.util

import androidx.core.content.FileProvider


/**
 * Archivo File Provider necesario para poder acceder a los archivos del dispositivo
 * en versiones de android 8 o superior
 */
class GenericFileProvider : FileProvider() {
    companion object {
        const val PROVIDER_NAME = ".co.simo.name.provider"
    }
}