package co.gov.cnsc.mobile.simo.analitycs

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Esta clase administra todos los eventos que se registran como analytics a firebase
 */
class AnalyticsReporter {

    companion object {
        //Categoría que hace reference a la pantalla a la cuál el usuario ingresó
        const val CATEGORY_SCREEN = "Pantalla"


        /**
         * Registra un evento de analytics a firebase
         * @param context Contexto desde el que se hace el llamado
         * @param idEvent id del evento que se quiere registrar
         * @param nameEvent nombre del evento que se quiere registrar
         * @param categoryEvent Nombre de la categoría a la cuál el evento será agrupado
         */
        private fun registerAnalyticsEvent(context: Context?, idEvent: String, nameEvent: String,
                                           categoryEvent: String) {
            if (context != null) {
                val analytics = FirebaseAnalytics.getInstance(context)
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, idEvent)
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, nameEvent)
                bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, categoryEvent)
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, nameEvent)
                analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
            }
        }

        fun screenSplash(context: Context?) {
            registerAnalyticsEvent(context, "Splash", "splash", CATEGORY_SCREEN)
        }

        fun screenStart(context: Context?) {
            registerAnalyticsEvent(context, "Inicio", "inicial", CATEGORY_SCREEN)
        }

        fun screenRegister(context: Context?) {
            registerAnalyticsEvent(context, "Registro", "registro", CATEGORY_SCREEN)
        }

        fun screenLogin(context: Context?) {
            registerAnalyticsEvent(context, "Login", "login", CATEGORY_SCREEN)
        }

        fun screenForgotPassword(context: Context?) {
            registerAnalyticsEvent(context, "Olvido de Contraseña", "olvido_de_contraseña", CATEGORY_SCREEN)
        }

        fun screenMainSearch(context: Context?) {
            registerAnalyticsEvent(context, "Principal de Busqueda", "busqueda", CATEGORY_SCREEN)
        }

        fun screenMyJobs(context: Context?) {
            registerAnalyticsEvent(context, "Mis Empleos", "mis_empleos", CATEGORY_SCREEN)
        }

        fun screenMyAlerts(context: Context?) {
            registerAnalyticsEvent(context, "Alertas", "alertas", CATEGORY_SCREEN)
        }

        fun screenDetailAlert(context: Context?) {
            registerAnalyticsEvent(context, "Detalle de Alerta", "detalle_alerta", CATEGORY_SCREEN)
        }

        fun screenConvocatories(context: Context?) {
            registerAnalyticsEvent(context, "Convocatorias", "convocatoriaa", CATEGORY_SCREEN)
        }

        fun screenDetailConvocatory(context: Context?) {
            registerAnalyticsEvent(context, "Detalle de Convocatoria", "detalle_de_convocatoria", CATEGORY_SCREEN)
        }

        fun screenPaymentsDone(context: Context?) {
            registerAnalyticsEvent(context, "Pagos Realizados", "pagos_realizados", CATEGORY_SCREEN)
        }

        fun screenListJobs(context: Context?) {
            registerAnalyticsEvent(context, "Listado OPEC", "listado_opec", CATEGORY_SCREEN)
        }

        fun screenAdvancedSearch(context: Context?) {
            registerAnalyticsEvent(context, "Filtros de Busqueda Avanzada", "filtros_busqueda_avanzada", CATEGORY_SCREEN)
        }

        fun screenJobDetail(context: Context?) {
            registerAnalyticsEvent(context, "Detalle del Empleo", "detalle_empleo", CATEGORY_SCREEN)
        }

        fun screenTermsService(context: Context?) {
            registerAnalyticsEvent(context, "Terminos de Servicio", "terminos_de_servicio", CATEGORY_SCREEN)
        }

        fun screenContact(context: Context?) {
            registerAnalyticsEvent(context, "Contacto", "contacto", CATEGORY_SCREEN)
        }

        fun screenHelpUsImprove(context: Context?) {
            registerAnalyticsEvent(context, "Ayudanos a Mejorar", "ayudanos_a_mejorar", CATEGORY_SCREEN)
        }

        fun screenChangePassword(context: Context?) {
            registerAnalyticsEvent(context, "Cambiar Contraseña", "cambiar_contraseña", CATEGORY_SCREEN)
        }


        fun screenMyCVBasic(context: Context?) {
            registerAnalyticsEvent(context, "Mi Información Básica", "mi_informacion_basica", CATEGORY_SCREEN)
        }

        fun screenMyFormations(context: Context?) {
            registerAnalyticsEvent(context, "Mi Formacion", "mi_formacion", CATEGORY_SCREEN)
        }

        fun screenAddFormation(context: Context?) {
            registerAnalyticsEvent(context, "Agregar Formacion", "agregar_formacion", CATEGORY_SCREEN)
        }

        fun screenEditFormation(context: Context?) {
            registerAnalyticsEvent(context, "Editar Formacion", "editar_formacion", CATEGORY_SCREEN)
        }

        fun screenMyExperience(context: Context?) {
            registerAnalyticsEvent(context, "Mi Experiencia", "mi_experiencia", CATEGORY_SCREEN)
        }

        fun screenAddExperience(context: Context?) {
            registerAnalyticsEvent(context, "Agregar Experiencia", "agregar_experiencia", CATEGORY_SCREEN)
        }

        fun screenEditExperience(context: Context?) {
            registerAnalyticsEvent(context, "Editar Experiencia", "editar_experiencia", CATEGORY_SCREEN)
        }


        fun screenMyIntelectualProduct(context: Context?) {
            registerAnalyticsEvent(context, "Mi Produccion Intelectual", "mi_produccion_intelectual", CATEGORY_SCREEN)
        }

        fun screenAddIntelectualProduct(context: Context?) {
            registerAnalyticsEvent(context, "Agregar Producto Intelectual", "agregar_producto_intelectual", CATEGORY_SCREEN)
        }

        fun screenEditIntelectualProduct(context: Context?) {
            registerAnalyticsEvent(context, "Editar Producto Intelectual", "editar_producto_intelectual", CATEGORY_SCREEN)
        }

        fun screenMyOtherDocuments(context: Context?) {
            registerAnalyticsEvent(context, "Mis Otros Documentos", "mis_otros_documentos", CATEGORY_SCREEN)
        }

        fun screenAddOtherDocument(context: Context?) {
            registerAnalyticsEvent(context, "Agregar Otro Documento", "agregar_otro_documento", CATEGORY_SCREEN)
        }

        fun screenEditOtherDocument(context: Context?) {
            registerAnalyticsEvent(context, "Editar Otro Documento", "editar_otro_documento", CATEGORY_SCREEN)
        }

        fun screenResultsInscriptions(context: Context?) {
            registerAnalyticsEvent(context, "Resultados de Inscripcion", "resultados de inscripcion", CATEGORY_SCREEN)
        }

        fun screenNewPassword(context: Context?) {
            registerAnalyticsEvent(context, "Nueva Contraseña", "nueva_contraseña", CATEGORY_SCREEN)
        }

        fun screenVerifyEmail(context: Context?) {
            registerAnalyticsEvent(context, "Verificar Email", "verificar_email", CATEGORY_SCREEN)
        }

        fun screenEnrollEmployment(context: Context?) {
            registerAnalyticsEvent(context, "Inscribir Empleo", "inscribir_empleo", CATEGORY_SCREEN)
        }

        fun screenPSEPayment(context: Context?) {
            registerAnalyticsEvent(context, "Pagando por PSE", "pagando_por_pse", CATEGORY_SCREEN)
        }

        fun screenResumePayment(context: Context?) {
            registerAnalyticsEvent(context, "Resumen del Pago", "resumen_del_pago", CATEGORY_SCREEN)
        }

        fun screenTransferPayment(context: Context?) {
            registerAnalyticsEvent(context, "Transferir Pago", "transferir_pago", CATEGORY_SCREEN)
        }


    }
}