package co.gov.cnsc.mobile.simo.util

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context

/**
 * Clase administra la funcionalidad de las pantallas de carga
 */
class ProgressBarDialog {

    companion object {

        /**
         * Número de progress dialogs mostrándose
         */
        private var counterProgressRequest = 0

        /**
         * Progress de cargando
         */
        private var progressDiag: ProgressDialog? = null


        /**
         * Muestra una pantalla de carga
         * @param context Contexto desde el cúal se ejecuta la pantalla de carga
         * @param title Titulo de la pantalla de carga
         * @param msg Mensaje de la pantalla de carga
         */
        @Synchronized
        fun startProgressDialog(context: Context, title: String, msg: String) {
            counterProgressRequest++
            if (progressDiag == null) {
                progressDiag = ProgressDialog(context)
                progressDiag?.setTitle(title)
                progressDiag?.setMessage(msg)
                progressDiag?.setCancelable(false)
                progressDiag?.isIndeterminate = true
                if (!(context as Activity).isFinishing) {
                    progressDiag?.show()
                }
            } else if (progressDiag?.isShowing == false) {
                progressDiag?.show()
            }
        }

        /**
         * Finaliza, esconde la pantalla de carga
         */
        @Synchronized
        fun stopProgressDialog() {
            counterProgressRequest--
            if (counterProgressRequest <= 1) {
                if (progressDiag != null) {
                    progressDiag?.dismiss()
                    progressDiag = null
                    counterProgressRequest = 0
                }
            }
        }

        /**
         * Muestra una pantalla de carga por defecto
         * @param context Contexto desde el cuál se muestra la pantalla de carga
         */
        fun startProgressDialog(context: Context) {
            this.startProgressDialog(context, "Cargando", "Espere...")
        }
    }


}