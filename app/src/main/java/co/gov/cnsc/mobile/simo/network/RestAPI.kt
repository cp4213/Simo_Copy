package co.gov.cnsc.mobile.simo.network

import co.gov.cnsc.mobile.simo.BuildConfig
import co.gov.cnsc.mobile.simo.models.*
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.json.FuelJson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.File
import java.nio.charset.Charset

/**
 * Administra y hace los llamados a todos los webservices que la aplicación necesita para funcionar
 */
class RestAPI {

    companion object {
        // dirección ip o dominio al cuál se hacen los llamados de los web services
        const val HOST = BuildConfig.HOST

        //Parametro header para los servicios que necesitan enviarse como un string json
        private const val CONTENT_TYPE_JSON = "application/json"

        //Número de resultados por página en la paginación de listados
        const val RESULTS_PER_PAGE = 20

        //Número máximo de paginas que se cargarán para evitar un overflow error por uso excesivo de RAM
        const val MAX_PAGES = 20

        /**
         * Llamado al servicio web de login para obtener una sesión
         * @param username nombre de usuario
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun login(username: String, password: String, success: (User) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}users/authenticate"
            val params = listOf("username" to username, "password" to password)

            Fuel.post(url, params).responseObject(User.Deserializer()) { request, response, result ->
                if (response.statusCode == 200) {
                    val cookies = response.headers["Set-Cookie"] as List<String>
                    if (cookies?.size > 0) {
                        val cookie = cookies[0]
                        FuelManager.instance.baseHeaders = mapOf("Cookie" to cookie)
                    }
                }
                result.fold(success, error)
            }
        }

        /**
         * LLama al servicio de obtener las alertas
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getAlerts(success: (List<Alert>) -> Unit, error: (FuelError) -> Unit): Request {
            val url = "${HOST}notificacionesciudadano/notificaciones/?page=0&size=100&sort=notificacion.fechaAgenda,DESC&sortDojo=-notificacion.fechaAgenda"
            return Fuel.get(url).responseObject(Alert.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Establece una alerta como leída por el usuario
         * @param idAlert id de la alerta seleccionada por el usuario
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun readAlert(idAlert: String?, success: (FuelJson) -> Unit, error: (FuelError) -> Unit): Request {
            val url = "${HOST}notificacionesciudadano/actualiza/estado/"
            val bodyString = idAlert
            val request = Fuel.post(url).body(bodyString!!, Charset.defaultCharset())
            request.headers["Content-Type"] = CONTENT_TYPE_JSON
            return request.responseJson { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Facilita el cambio de contraseña para ingreso a SIMO
         * @param username nombre de usuario
         * @param oldPassword contraseña anterior
         * @param newPassword nueva contraseña escrita por el usuario
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun changePassword(username: String, oldPassword: String, newPassword: String, success: (FuelJson) -> Unit,
                           error: (FuelError) -> Unit) {
            val url = "${HOST}usuarios/nuevacontrasena"
            val jsonBody = JsonObject()
            jsonBody.addProperty("login", username)
            jsonBody.addProperty("password", newPassword)
            jsonBody.addProperty("password_new", newPassword)
            jsonBody.addProperty("password_verify", newPassword)
            jsonBody.addProperty("password_old", oldPassword)
            val request = Fuel.post(url).body(jsonBody.toString(), Charset.defaultCharset())
            request.headers["Content-Type"] = CONTENT_TYPE_JSON
            request.responseJson { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Registra la información del usuario junto con el codigo de verificación
         * @param user objeto usuario con la información a ser registrada en la plataforma
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun registerSendVerifyCode(user: User, success: (FuelJson) -> Unit,
                                   error: (FuelError) -> Unit) {
            val url = "${HOST}ciudadanos/enviarCodigoVerificacion/"
            val jsonBody = JsonObject()
            jsonBody.addProperty("login", user.username)
            jsonBody.addProperty("correoconfirmar", user.email)
            jsonBody.addProperty("email", user.email)
            jsonBody.addProperty("identificacion", user.identifier)
            jsonBody.addProperty("envioCorreo", user.sendEmail)
            jsonBody.addProperty("fechaExpedicion", user.dateExpedition)
            jsonBody.add("nombre", null)
            jsonBody.add("ver", null)
            jsonBody.add("apellido", null)
            jsonBody.add("password", null)
            jsonBody.add("password_new", null)
            jsonBody.addProperty("terminos", true)
            val jsonDocumentType = JsonObject()
            jsonDocumentType.addProperty("id", user.documentType?.id)
            jsonBody.add("tipoDocumento", jsonDocumentType)
            val jsonUser = JsonObject()
            jsonUser.add("codigoVerificacion", null)
            jsonBody.add("usuario", jsonUser)
            val bodyString = jsonBody.toString()
            val request = Fuel.post(url).body(bodyString, Charset.defaultCharset())
            request.headers["Content-Type"] = CONTENT_TYPE_JSON
            request.responseJson { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Verifica que un codigo de verificación sea valido
         * @param verificationCode codigo a verificar
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun validateVerificationCode(verificationCode: String?, success: (String) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}ciudadanos/validacodigoverificacionmovil/$verificationCode"
            Fuel.get(url).responseString { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Verifica la existencia del 'número de identificación' y la 'fecha de expedición'
         * del documento de identidad del ciudadano contra FCD
         * @param numCedRNECokP número de cédula a validar contra FCD
         * @param fechaExpRNECvalidaP fecha de expedición de la cédula a validar contra FCD
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun validateCitizenBasicDataAgainstFCD(cedulaForm: String?, fechaExpedicionForm: String?, success: (String) -> Unit, error: (FuelError) -> Unit) {

            //Este endpoint solo funcionará cuando apuntemos a piapoco1 externo, y cuando se migre el servicio a Tucano14 o a SIMO Web en General (al parecer éste será el endpoint definitivo)
            val url = "${HOST}/ciudadanos/obtenerDatosCiudadanoRNEC/$cedulaForm/$fechaExpedicionForm/"

            //Este endpoint solo funcionará cuando se ejecute la app desde el emulador
            //val url = "http://cuna-m-fcd.cnsc.net:8081/api/personapornumerodocumentoidentificacionyfechaexpedicion/SIMO/$numCedRNECokP/$fechaExpRNECvalidaP/1"

            Fuel.get(url).responseString { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Valida que no esté pronto el cierre del pago de derechos de inscripción (10 minutos antes)
         * @param idEmpleoActual id del empleo actual (número OPEC actual)
         * @param idEmpleoNuevo id del empleo nuevo (nuevo número OPEC)
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         * @param GET method
         */
        fun validatePaymentTransfer (idEmpleoOld: String?, idEmpleoNew: String?, success: (String) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}/inscripcion/empleoBycambio/${idEmpleoOld}/${idEmpleoNew}/"
            Fuel.get(url).responseString { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Reestablece la contraseña del usuario en caso de ser olvidada
         * @param verificationCode codigo de verificacion
         * @param newPassword Nueva contraseña que el usuario escribe
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun resetPassword(verificationCode: String?, newPassword: String?, success: (FuelJson) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}usuarios/cambiarcontrasena"
            val params = listOf("codigoVerificacion" to verificationCode, "password" to newPassword)
            Fuel.post(url, params).responseJson { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Envía un correo al usuario cuando este olvida la contraseña de ingreso
         * @param username nombre de usuario con el que se registró en el sistema
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun forgotPassword(username: String?, success: (FuelJson) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}usuarios/US/$username/MOVIL/olvidarcontrasena"
            Fuel.post(url).responseJson { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Registra la información del usuario como paso final del flujo de registro
         * @param user objeto usuario con toda la información para ser registrada
         * @param password contraseña con la que el usuario se registra la primera vez
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun registerSendFinal(user: User, password: String, success: (FuelJson) -> Unit,
                              error: (FuelError) -> Unit) {
            val url = "${HOST}ciudadanos/"
            val jsonBody = JsonObject()
            jsonBody.addProperty("apellido", user.lastName)
            jsonBody.addProperty("correoconfirmar", user.email)
            jsonBody.addProperty("email", user.email)
            jsonBody.addProperty("envioCorreo", user.sendEmail)
            jsonBody.addProperty("fechaExpedicion", user.dateExpedition)
            jsonBody.addProperty("identificacion", user.identifier)
            jsonBody.addProperty("login", user.username)
            jsonBody.addProperty("nombre", user.name)
            jsonBody.addProperty("password", password)
            jsonBody.addProperty("password_new", password)
            jsonBody.addProperty("terminos", true)
            val jsonDocumentType = JsonObject()
            jsonDocumentType.addProperty("id", user.documentType?.id)
            jsonBody.add("tipoDocumento", jsonDocumentType)
            val jsonUser = JsonObject()
            jsonUser.addProperty("codigoVerificacion", user.verificationCode)
            jsonUser.addProperty("passwd", password)
            jsonBody.add("usuario", jsonUser)
            jsonBody.add("ver", null)
            val bodyString = jsonBody.toString()
            val request = Fuel.post(url).body(bodyString, Charset.defaultCharset())
            request.headers["Content-Type"] = CONTENT_TYPE_JSON
            request.responseJson { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Cierra la sesión del usuario en backend
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun logout(success: (String) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}users/logout"
            Fuel.post(url).responseString { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene toda la información de Datos Básicos
         * @param username Nombre de usuario del cuál se está consultando la información
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getUser(username: String, success: (User) -> Unit, error: (FuelError) -> Unit): Request {
            val url = "${HOST}usuarios/byLogin?login=${username}"
            return Fuel.get(url).responseObject(User.Deserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }
        fun updateUserAdress(idUser: String?,
                             idDocumentUser: String?,
                             address: String?,
                             names: String?,
                             lastNames: String?,
                             idTypeDocument: String?,
                             success: (User) -> Unit,
                             error: (FuelError) -> Unit): Request {
            val url = "${HOST}ciudadanos/${idUser}"

            val jsonMain = JsonObject()
            val jsonDocumentType = JsonObject()
            jsonDocumentType.addProperty("id", idTypeDocument)
            jsonMain.add("tipoDocumento", jsonDocumentType)
            jsonMain.addProperty("identificacion", idDocumentUser)
            jsonMain.addProperty("nombre", names)
            jsonMain.addProperty("apellido", lastNames)
            jsonMain.addProperty("direccion", address)

            val jsonBody = jsonMain.toString()
            val request = Fuel.put(url).body(jsonBody, Charset.defaultCharset())
            request.headers["Content-Type"] = CONTENT_TYPE_JSON
            request.responseObject(User.Deserializer()) { request, response, result ->
                result.fold(success, error)
            }
            return request
        }


        /**
         * Actualiza toda la información de Datos Básicos
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun updateUser(idUser: String?,
                       username: String?,
                       idTypeDocument: String?,
                       idDocumentUser: String?,
                       documentDni: Document?,
                       stageIdDni: String?,
                       idPerson: String?,
                       expeditionDate: String?,
                       names: String?,
                       lastNames: String?,
                       dateBirth: String?,
                       idCity: String?,
                       idDepartment: String?,
                       postalCodeBirth: String?,
                       idCountryBirth: String?,
                       gender: String?,
                       address: String?,
                       postalCodeRes: String?,
                       idCountryRes: String?,
                       email: String?,
                       telephone: String?,
                       idLevelEducation: String?,
                       documentPhoto: Document?,
                       stageIdPhoto: String?,
                       dateCreation: String?,
                       sendEmail: Boolean = true,
                       disabilities: List<Disability>? = listOf(),
                       success: (User) -> Unit,
                       error: (FuelError) -> Unit): Request {
            val url = "${HOST}ciudadanos/${idUser}"

            val jsonMain = JsonObject()
            val jsonDocumentType = JsonObject()
            jsonDocumentType.addProperty("id", idTypeDocument)
            jsonMain.add("tipoDocumento", jsonDocumentType)
            jsonMain.addProperty("identificacion", idDocumentUser)
            jsonMain.addProperty("fechaExpedicion", expeditionDate)
            jsonMain.addProperty("personaIdUser", idPerson)
            var jsonDocumentId: JsonObject? = null
            if (documentDni != null || stageIdDni != null) {
                jsonDocumentId = JsonObject()
                jsonDocumentId.addProperty("id", documentDni?.id)
                jsonDocumentId.addProperty("nombre", documentDni?.name)
                jsonDocumentId.addProperty("rutaArchivo", documentDni?.pathFile)
                jsonDocumentId.addProperty("contentType", documentDni?.contentType)
                jsonDocumentId.addProperty("owner", username)
                jsonDocumentId.addProperty("stageId", stageIdDni)
                jsonDocumentId.addProperty("documentoOrigenId", documentDni?.documentOriginId)
            }
            jsonMain.add("docIdentificacion", jsonDocumentId)
            jsonMain.addProperty("nombre", names)
            jsonMain.addProperty("apellido", lastNames)
            jsonMain.addProperty("fechaNacimiento", dateBirth)
            var jsonCity: JsonObject? = null
            var jsonCountryBirth: JsonObject? = null
            if (idCity != null && idDepartment != null) {
                jsonCity = JsonObject()
                jsonCity.addProperty("id", idCity)
                val jsonDepartment = JsonObject()
                jsonDepartment.addProperty("id", idDepartment)
                jsonCity.add("departamento", jsonDepartment)
            } else if (idCountryBirth != null) {
                jsonCountryBirth = JsonObject()
                jsonCountryBirth.addProperty("id", idCountryBirth)
            }
            jsonMain.add("municipioNacimiento", jsonCity)
            jsonMain.addProperty("zipcodenacimiento", postalCodeBirth)
            jsonMain.add("paisNacimiento", jsonCountryBirth)
            jsonMain.addProperty("genero", gender)
            jsonMain.addProperty("direccion", address)
            //var jsonCityRes: JsonObject? = null
            //var jsonCountryRes: JsonObject? = null
            /*if (idCityRes != null && idDepartmentRes != null) {
                jsonCityRes = JsonObject()
                jsonCityRes.addProperty("id", idCityRes)
                val jsonDepartmentRes = JsonObject()
                jsonDepartmentRes.addProperty("id", idDepartmentRes)
                jsonCityRes.add("departamento", jsonDepartmentRes)
            } else if (idCountryRes != null) {
                jsonCountryRes = JsonObject()
                jsonCountryRes.addProperty("id", idCountryRes)
            }
            jsonMain.addProperty("zipcoderesidencia", postalCodeRes)
            jsonMain.add("municipioResidencia", jsonCityRes)
            jsonMain.add("paisResidencia", jsonCountryRes)*/
            jsonMain.addProperty("email", email)
            jsonMain.addProperty("telefono", telephone)
            val jsonLevelEducation = JsonObject()
            jsonLevelEducation.addProperty("id", idLevelEducation)
            jsonMain.add("nivelEducacionFormal", jsonLevelEducation)
            jsonMain.addProperty("rolesUsuario", "CIUDADANO")
            val jsonRoles = JsonArray()
            val jsonRol = JsonObject()
            jsonRol.addProperty("id", "4")
            jsonRol.addProperty("nombre", "CIUDADANO")
            jsonRoles.add(jsonRol)
            jsonMain.add("roles", jsonRoles)
            jsonMain.addProperty("envioCorreo", sendEmail)
            jsonMain.addProperty("fechaCreacion", dateCreation)
            jsonMain.addProperty("habilitado", true)
            jsonMain.addProperty("id", idUser)
            jsonMain.addProperty("login", username)
            jsonMain.addProperty("nacidoExtranjero", idCountryBirth != null)
            jsonMain.addProperty("residenteExtranjero", idCountryRes != null)
            var jsonPhoto: JsonObject? = null
            if (documentPhoto != null || stageIdPhoto != null) {
                jsonPhoto = JsonObject()
                jsonPhoto.addProperty("contentType", documentPhoto?.contentType)
                jsonPhoto.add("documentoOrigenId", null)
                jsonPhoto.addProperty("id", documentPhoto?.id)
                jsonPhoto.addProperty("nombre", documentPhoto?.name)
                jsonPhoto.addProperty("owner", username)
                jsonPhoto.addProperty("stageId", stageIdPhoto)
                jsonPhoto.addProperty("rutaArchivo", documentPhoto?.pathFile)
            }
            jsonMain.add("docFoto", jsonPhoto)

            val jsonDisabilities = JsonArray()
            disabilities?.forEach {
                val jsonDisability = JsonObject()
                jsonDisability.addProperty("id", it.id)
                jsonDisability.addProperty("descripcion", it.name)
                jsonDisabilities.add(jsonDisability)
            }
            jsonMain.add("discapacidades", jsonDisabilities)

            val jsonBody = jsonMain.toString()
            val request = Fuel.put(url).body(jsonBody, Charset.defaultCharset())
            request.headers["Content-Type"] = CONTENT_TYPE_JSON
            request.responseObject(User.Deserializer()) { request, response, result ->
                result.fold(success, error)
            }
            return request
        }

        /**
         * Crea o actualiza un registro de Formación en la hoja de vida
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun createOrUpdateFormation(idUser: String?, username: String?, idCredential: String?,
                                    idLevelEducation: String?,
                                    idTypeEducation: String?,
                                    idClassEducation: String?,
                                    idInstitution: String?, idProgramm: String?, idCountry: String?,
                                    institutionExt: String?, programmExt: String?, isAbroad: Boolean? = false, isOther: String?,
                                    idPeriodicity: String?,
                                    levelReached: String?,
                                    graduado: String?,
                                    dateGrade: String?, dateIni: String?, dateFin: String?, hourInten:String?,
                                    convalidate: String?, convalidateResolutionNumber: String?, convalidateResolutionDate: String?,
                                    academicDiciplineid: String?,
                                    documentAttachment: Document?,
                                    stageIdAttachment: String?,
                                    success: (Credential) -> Unit,
                                    error: (FuelError) -> Unit): Request {

            val jsonMain = JsonObject()
            jsonMain.addProperty("id", idCredential)

            val jsonLevelEducational = JsonObject()
            jsonLevelEducational.addProperty("id", idLevelEducation)//idLevelEducation

            val jsontypeFormalEducation = JsonObject()
            jsontypeFormalEducation.addProperty("id",idTypeEducation)//TODO
            jsonLevelEducational.add("tipoEducacionFormal", jsontypeFormalEducation)
            val jsontypeEducationClass = JsonObject()
            jsontypeEducationClass.addProperty("id",idClassEducation) //TODO
            jsonLevelEducational.add("claseEducacion", jsontypeEducationClass)

            jsonMain.add("nivelEducacionFormal", jsonLevelEducational)

            var jsonCountry: JsonObject? = null
              if (idCountry != null) {
                    jsonCountry = JsonObject()
                    jsonCountry.addProperty("id", idCountry)
            }
            jsonMain.add("pais", jsonCountry)
            jsonMain.addProperty("entidadEducativaExt", institutionExt)
            jsonMain.addProperty("programaExt", programmExt)
            jsonMain.addProperty("tituloExtranjero", isAbroad)
            jsonMain.addProperty("otroPrograma", isOther)
            jsonMain.addProperty("graduado", graduado)
            jsonMain.addProperty("nivelAlcanzado", levelReached)
            jsonMain.addProperty("fechaGrado", dateGrade)
            jsonMain.addProperty("fechaInicio", dateIni)
            jsonMain.addProperty("fechaTerminacion", dateFin)
            jsonMain.addProperty("fechaOrdenamiento", dateFin)
            jsonMain.addProperty("intensidadHoras", hourInten)
            jsonMain.addProperty("convalidado",convalidate)
            jsonMain.addProperty("convalidadoNumeroResolucion", convalidateResolutionNumber)
            jsonMain.addProperty("convalidadoFechaResolucion", convalidateResolutionDate)
            var programmJson: JsonObject? = null
            if (idProgramm != null) {
                programmJson = JsonObject()
                programmJson.addProperty("id", idProgramm)
            }
            jsonMain.add("programa", programmJson)

            var convprogrammJson: JsonObject? = null
            if (academicDiciplineid != null) {
                convprogrammJson = JsonObject()
                convprogrammJson.addProperty("id", academicDiciplineid)
            }
            jsonMain.add("disciplinaAcademica", convprogrammJson)

            var periodicityJson: JsonObject? = null
            if (idPeriodicity != null) {
                periodicityJson = JsonObject()
                periodicityJson.addProperty("id", idPeriodicity)
            }
            jsonMain.add("periodicidad", periodicityJson)

            val documentJson = JsonObject()
            documentJson.addProperty("id", documentAttachment?.id)
            documentJson.addProperty("nombre", documentAttachment?.name)
            documentJson.addProperty("rutaArchivo", documentAttachment?.pathFile)
            documentJson.addProperty("contentType", documentAttachment?.contentType)
            documentJson.addProperty("owner", username)
            documentJson.addProperty("stageId", stageIdAttachment)
            documentJson.addProperty("documentoOrigenId", documentAttachment?.documentOriginId)
            jsonMain.add("documento", documentJson)

            val jsonBody = jsonMain.toString()

            var url = "${HOST}ciudadanos/formaciones/"
            if (idCredential != null) {
                url += "$idCredential"
            }

            var request: Request?
            if (idCredential == null) {
                request = Fuel.post(url)
            } else {
                request = Fuel.put(url)
            }
            request.body(jsonBody, Charset.defaultCharset())
            request.headers["Content-Type"] = CONTENT_TYPE_JSON
            request.responseObject(Credential.Deserializer()) { request, response, result ->
                result.fold(success, error)
            }
            return request
        }

        /**
         * Crea o actualiza un registro de Experiencia en la hoja de vida
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun createOrUpdateExperience(idUser: String?, username: String?, idCredential: String?,
                                     company: String?, charge: String?,
                                     hoursTotal: Int?, hoursDailyAverage: Int?,
                                     dateEntry: String?, dateExit: String?,
                                     documentAttachment: Document?, stageIdAttachment: String?,
                                     dateExpedition: String?,
                                     success: (Credential) -> Unit,
                                     error: (FuelError) -> Unit): Request {
            val jsonMain = JsonObject()

            jsonMain.addProperty("id", idCredential)
            jsonMain.addProperty("tiempoHoras", hoursTotal != null)
            jsonMain.addProperty("empresa", company)
            jsonMain.addProperty("cargo", charge)
            jsonMain.addProperty("actual", dateExit == null)
            jsonMain.addProperty("jornadaCompleta", hoursDailyAverage == null)
            jsonMain.addProperty("horasDiarias", hoursDailyAverage)
            jsonMain.addProperty("fechaIngreso", dateEntry)
            jsonMain.addProperty("fechaTerminacion", dateExit)
            jsonMain.addProperty("tiempo", hoursTotal)
            val documentJson = JsonObject()
            documentJson.addProperty("id", documentAttachment?.id)
            documentJson.addProperty("nombre", documentAttachment?.name)
            documentJson.addProperty("rutaArchivo", documentAttachment?.pathFile)
            documentJson.addProperty("contentType", documentAttachment?.contentType)
            documentJson.addProperty("owner", username)
            documentJson.addProperty("stageId", stageIdAttachment)
            documentJson.addProperty("documentoOrigenId", documentAttachment?.documentOriginId)
            jsonMain.add("documento", documentJson)
            jsonMain.addProperty("fechaCertificacion", dateExpedition)


            val jsonBody = jsonMain.toString()
            var url = "${HOST}ciudadanos/experiencias/"
            if (idCredential != null) {
                url += "$idCredential"
            }

            var request: Request?
            if (idCredential == null) {
                request = Fuel.post(url)
            } else {
                request = Fuel.put(url)
            }
            request.body(jsonBody, Charset.defaultCharset())
            request.headers["Content-Type"] = CONTENT_TYPE_JSON
            request.responseObject(Credential.Deserializer()) { request, response, result ->
                result.fold(success, error)
            }
            return request
        }

        /**
         * Crea o actualiza un registro de Propiedad Intelectual en la hoja de vida
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun createOrUpdateProdIntelectual(idUser: String?, username: String?, idCredential: String?,
                                          idTypeProduct: String?, numberIdentifier: String?,
                                          quoteBibliographic: String?, documentAttachment: Document?,
                                          stageIdAttachment: String?,
                                          success: (Credential) -> Unit,
                                          error: (FuelError) -> Unit): Request {
            val jsonMain = JsonObject()
            jsonMain.addProperty("id", idCredential)
            val jsonTypeProd = JsonObject()
            jsonTypeProd.addProperty("id", idTypeProduct)
            jsonMain.add("tipoProduccionIntelectual", jsonTypeProd)
            jsonMain.addProperty("identificador", numberIdentifier)
            jsonMain.addProperty("fichaBibliografica", quoteBibliographic)
            val documentJson = JsonObject()
            documentJson.addProperty("id", documentAttachment?.id)
            documentJson.addProperty("nombre", documentAttachment?.name)
            documentJson.addProperty("rutaArchivo", documentAttachment?.pathFile)
            documentJson.addProperty("contentType", documentAttachment?.contentType)
            documentJson.addProperty("owner", username)
            documentJson.addProperty("stageId", stageIdAttachment)
            documentJson.addProperty("documentoOrigenId", documentAttachment?.documentOriginId)
            jsonMain.add("documento", documentJson)

            val jsonBody = jsonMain.toString()
            var url = "${HOST}ciudadanos/producciones/"
            if (idCredential != null) {
                url += "$idCredential"
            }

            var request: Request?
            request = if (idCredential == null) {
                Fuel.post(url)
            } else {
                Fuel.put(url)
            }
            request.body(jsonBody, Charset.defaultCharset())
            request.headers["Content-Type"] = CONTENT_TYPE_JSON
            request.responseObject(Credential.Deserializer()) { request, response, result ->
                result.fold(success, error)
            }
            return request
        }

        /**
         * Crea o actualiza un registyro de Otros Documentos en la hoja de vida
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun createOrUpdateOtherDocument(idUser: String?, username: String?, idCredential: String?,
                                        idTypeDocument: String?,
                                        documentAttachment: Document?,
                                        stageIdAttachment: String?,
                                        success: (Credential) -> Unit,
                                        error: (FuelError) -> Unit): Request {
            val jsonMain = JsonObject()
            jsonMain.addProperty("id", idCredential)
            val jsonTypeDocument = JsonObject()
            jsonTypeDocument.addProperty("id", idTypeDocument)
            jsonMain.add("tipoInformacion", jsonTypeDocument)
            val documentJson = JsonObject()
            documentJson.addProperty("id", documentAttachment?.id)
            documentJson.addProperty("nombre", documentAttachment?.name)
            documentJson.addProperty("rutaArchivo", documentAttachment?.pathFile)
            documentJson.addProperty("contentType", documentAttachment?.contentType)
            documentJson.addProperty("owner", username)
            documentJson.addProperty("stageId", stageIdAttachment)
            documentJson.addProperty("documentoOrigenId", documentAttachment?.documentOriginId)
            jsonMain.add("documento", documentJson)

            val jsonBody = jsonMain.toString()
            var url = "${HOST}ciudadanos/otrosdocumentos/"
            if (idCredential != null) {
                url += "$idCredential"
            }

            var request: Request?
            if (idCredential == null) {
                request = Fuel.post(url)
            } else {
                request = Fuel.put(url)
            }
            request.body(jsonBody, Charset.defaultCharset())
            request.headers["Content-Type"] = CONTENT_TYPE_JSON
            request.responseObject(Credential.Deserializer()) { request, response, result ->
                result.fold(success, error)
            }
            return request
        }

        /**
         * Sube un archivo .pdf al servidor
         * @param file Archivo a subir
         * @param restriction tipo de archivo a subir
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        private fun uploadFile(file: File, restriction: String?,
                               success: (co.gov.cnsc.mobile.simo.models.File) -> Unit, error: (FuelError) -> Unit): Request {
            val url = "${HOST}documents/upload"
            val formData = listOf("restriction" to restriction)
            return Fuel.upload(url, Method.POST, formData)
                /*.source { request, url ->
                file
            }.name { "file" }.responseObject(co.gov.cnsc.mobile.simo.models.File.Deserializer()) { request, response, result ->
                result.fold(success, error)
            }*/
        }

        /**
         * Sube un archivo de imagen al servidor
         * @param file Archivo de imagen a subir
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun uploadImage(file: File, success: (co.gov.cnsc.mobile.simo.models.File) -> Unit, error: (FuelError) -> Unit): Request {
            return uploadFile(file, "imagen", success, error)
        }

        /**
         * Sube un archivo PDF correspondiente al documento de identidad de un usuario
         * @param file Archivo PDF de la cedula del usuario
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun uploadDniUser(file: File, success: (co.gov.cnsc.mobile.simo.models.File) -> Unit, error: (FuelError) -> Unit): Request {
            return uploadFile(file, "cedula", success, error)
        }

        /**
         * Sube un archivo PDF al servidor
         * @param file Archivo PDF del dispositivo
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun uploadFilePDF(file: File, success: (co.gov.cnsc.mobile.simo.models.File) -> Unit, error: (FuelError) -> Unit): Request {
            return uploadFile(file, null, success, error)
        }

        /**
         * Obtiene todos los registros de Formación del usuario
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getFormations(success: (List<Credential>) -> Unit, error: (FuelError) -> Unit): Request {
            val url = "${HOST}ciudadanos/formaciones/?page=0&size=100"
            return Fuel.get(url).responseObject(Credential.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Elimina un registro de Formación del servidor
         * @param idUser id del usuario
         * @param idFormation id del item a eliminar
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun deleteFormation(idUser: String?, idFormation: String?, success: (String) -> Unit, error: (FuelError) -> Unit): Request {
            val url = "${HOST}ciudadanos/formaciones/${idFormation}"
            return Fuel.delete(url).responseString { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene todos los registros de Experiencia del usuario
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getExperiences(success: (List<Credential>) -> Unit, error: (FuelError) -> Unit): Request {
            val url = "${HOST}ciudadanos/experiencias/?page=0&size=100"
            return Fuel.get(url).responseObject(Credential.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Elimina un registro de Experiencia del servidor
         * @param idUser id del usuario
         * @param idExperience id del item a eliminar
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun deleteExperience(idUser: String?, idExperience: String?, success: (String) -> Unit, error: (FuelError) -> Unit): Request {
            val url = "${HOST}ciudadanos/experiencias/${idExperience}"
            return Fuel.delete(url).responseString { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene todos los registros de Producción Intelectual del usuario
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getProductsIntelectual(success: (List<Credential>) -> Unit, error: (FuelError) -> Unit): Request {
            val url = "${HOST}ciudadanos/producciones/?page=0&size=100"
            return Fuel.get(url).responseObject(Credential.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Elimina un registro de Producción Intelectual del servidor
         * @param idUser id del usuario
         * @param idProdIntelectual id del item a eliminar
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun deleteProductIntelectual(idUser: String?, idProdIntelectual: String?, success: (String) -> Unit, error: (FuelError) -> Unit): Request {
            val url = "${HOST}ciudadanos/producciones/${idProdIntelectual}"
            return Fuel.delete(url).responseString { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene todos los registros de Otros Documentos del usuario
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getOtherDocuments(success: (List<Credential>) -> Unit, error: (FuelError) -> Unit): Request {
            val url = "${HOST}ciudadanos/otrosdocumentos/?page=0&size=100"
            return Fuel.get(url).responseObject(Credential.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Elimina un registro de Otros Documentos del servidor
         * @param idUser id del usuario
         * @param idOtherDocument id del item a eliminar
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun deleteOtherDocument(idUser: String?, idOtherDocument: String?, success: (String) -> Unit, error: (FuelError) -> Unit): Request {
            val url = "${HOST}ciudadanos/otrosdocumentos/${idOtherDocument}"
            return Fuel.delete(url).responseString { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene el resumen del número de empleos por nivel profesional (presentes en el HOME), y la sumatoria total de empleos
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getCategoryOffers(success: (List<CategoryJob>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}empleos/mobileOpecvisible"
            Fuel.get(url).responseObject(CategoryJob.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene todos los Pagos realiados por el usuario (Mis Pagos)
         * @param idUser id del usuario
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getPayments(idUser: String?, success: (List<Payment>) -> Unit, error: (FuelError) -> Unit): Request {
            val url = "${HOST}recaudos/ciudadano/${idUser}?page=0&size=100&sort=fechaPago,DESC&sortDojo=-fechaPago"
            return Fuel.get(url).responseObject(Payment.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene todas las pruebas de un empleo que ha sido Confirmado
         * @param idInscripcion id de la inscripción
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getTests(idInscripcion: String?, success: (String) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}serviciosMobile/procesoInscripcion/${idInscripcion}/obtenerPruebas"
            Fuel.get(url).responseString { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene todos los departamentos disponibles para presentar las pruebas
         * @param idPrueba id de la prueba
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */

        fun getPresentationDepartments(idPrueba: String?, success: (String) -> Unit, error: (FuelError) -> Unit){
            val url = "${HOST}/serviciosMobile/procesoInscripcion/${idPrueba}/lugares/departamentos";
            Fuel.get(url).responseString{ request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene todos los municipios disponibles para presentar las pruebas
         * @param idPrueba id de la prueba
         * @param idDepto id del departamento
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */

        fun getPresentationProvincies(idPrueba: String?, idDepto: String? ,success: (String) -> Unit, error: (FuelError) -> Unit){
            val url = "${HOST}/serviciosMobile/procesoInscripcion/${idPrueba}/lugares/departamentos/${idDepto}/municipios"
            Fuel.get(url).responseString{ request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Establece una oferta laboral como Confirmada (preinscrita sin pago)
         * @param idEmpleo id del Empleo que se quiere confirmar
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun confirmEmployment (idEmpleo: String?, success: (FuelJson) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}/serviciosMobile/procesoInscripcion/confirmarEmpleo/${idEmpleo}"
            Fuel.post(url).responseJson { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Registra el lugar de presentación de la(s) prueba(s)
         * @param idInscripcion id de la Inscripción
         * @param idPrueba id de la Prueba
         * @param idMunicipio id del Municipio
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun testPlaceRegistration (idInscripcion: String?, idPrueba: String?, idMunicipio: String?, success: (String) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}/serviciosMobile/procesoInscripcion/${idInscripcion}/registrarLugar/${idPrueba}/${idMunicipio}"
            Fuel.post(url).responseString { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Valida que todas las pruebas tengan lugar de presentación definida
         * @param idInscripcion id de la Inscripción
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun validateDefinedTestPlace (idInscripcion: String?, success: (String) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}serviciosMobile/procesoInscripcion/${idInscripcion}/validarLugarPresentacion"
            Fuel.post(url).responseString { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Valida que no esté pronto el cierre del pago de derechos de inscripción (10 minutos antes)
         * @param idInscripcion id de la Inscripción
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         * @param GET method
         */
        fun validatePaymentDueDate (idInscripcion: String?, success: (String) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}/serviciosMobile/procesoInscripcion/${idInscripcion}/validarFechaPago"
            Fuel.get(url).responseString { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Valida si existe un pago a otro empleo en la misma Convocatoria
         * @param idEmpleo id del empleo
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         * @param GET method
         */
        fun validateExistAnotherPayment (idEmpleo: String?, success: (String) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}/serviciosMobile/procesoInscripcion/${idEmpleo}/validarPagoOtroEmpleo"
            Fuel.get(url).responseString { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Registra el recaudo para una inscripción
         * @param inscripcion id de la Inscripción
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun registerPayment (inscripcion: String?, success: (String) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}/serviciosMobile/procesoInscripcion/${inscripcion}/registrarRecaudo"
            Fuel.post(url).responseString() { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Registra la inscripción a un empleo por parte del aspirante
         * @param empleo id del Empleo
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun enrollmentToEmployment (empleo: String?, success: (String) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}/serviciosMobile/procesoInscripcion/${empleo}/inscripcion"
            Fuel.post(url).responseString() { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Trae la información del resumen de los soportes
         * @param inscripcion id de la inscripcion
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responda con un mensaje de error
         */
        fun getSupportResumeEnroll (inscripcion: String?, success: (String) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}/inscripcion/id/${inscripcion}?page=0&size=100"
            Fuel.get(url).responseString() { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Permite la descarga de la Constancia de Inscripción (.pdf)
         * @param inscripcion id de la inscripcion
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        /*fun getDownloadPDFEnroll (inscripcion: String?, success: (String) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}/reporte/inscripcionPDF/${inscripcion}"
            Fuel.download(url)
                .destination { response, url -> File("temp").apply { println(absolutePath) } }
                .responseString { request, response, result ->
                    result.fold(success, error)
            }
        }*/

        /**
         * Permite Actualizar Documentos (después de cerrada la etapa de Inscripciónes del empleo)
         * @param empleo id del Empleo
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun updateDocuments (empleo: String?, success: (FuelJson) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}/serviciosMobile/procesoInscripcion/${empleo}/actualizarDocumentos"
            Fuel.post(url).responseJson { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene la lista de ofertas de trabajo
         * @param page numero de pagina que se quiere obtener
         * @param filter objeto filtro que tiene los parámetros de filtro de las ofertas
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responda con un mensaje de error
         */
        fun getWorkOffers(page: Int, filter: Filter?, success: (List<WorkOffer>) -> Unit, error: (FuelError) -> Unit, totalResults: (Int?) -> Unit) {
            //val url = "${HOST}empleos/ofertaPublica/?search_palabraClave=&amp;search_nivel=&amp;search_convocatoria=127808834&amp;search_departamento=&amp;search_municipio=&amp;search_salario=&amp;search_entidad=&amp;search_numeroOPEC=&amp;search_limiteInferior=4000000&amp;search_limiteSuperior=7000000&amp;page=0&amp;size=10"
            val url = "${HOST}empleos/ofertaPublica"
            val params = listOf(
                    "search_palabraClave" to filter?.keyWord,
                    "search_nivel" to filter?.level?.id,
                    "search_convocatoria" to filter?.convocatory?.id,
                    "search_departamento" to filter?.department?.id,
                    "search_municipio" to filter?.city?.id,
                    "search_salario" to filter?.salarialRange?.id,
                    "search_entidad" to filter?.entity?.id,
                    "search_numeroOPEC" to filter?.numberOPEC,
                    "search_limiteInferior" to filter?.lowerLimitSR,
                    //"search_limiteInferior" to 4500001,
                    "search_limiteSuperior" to filter?.upperLimitSR,
                    //"search_limiteSuperior" to 5000000,
                    "page" to page,
                    "size" to RESULTS_PER_PAGE
            )
            Fuel.get(url, params).responseObject(WorkOffer.ListDeserializer()) { request, response, result ->
                val contentRanges= response.headers["Content-Range"] as List<String>
                val contentRange = contentRanges[0]
                val splitRange = contentRange?.split("/")
                val totalRegister = splitRange?.get(1)?.toInt()
                totalResults(totalRegister)
                result.fold(success, error)
            }
        }

        /**
         * Obtiene el detalle de una oferta laboral
         * @param id id del empleo seleccionado
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getJob(id: String, success: (Job) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}empleos/${id}"
            Fuel.get(url).responseObject(Job.Deserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        fun getJob_json (id: String, success: (String) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}empleos/${id}"
            Fuel.get(url).responseString { request, response, result ->
                result.fold(success, error)
            }
        }


        /**
         * Obtiene las fechas de apertura y cierre para la etapa de Inscripciones
         * @param id id del empleo seleccionado
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getJobDueDate(id: String, success: (String) -> Unit, error: (FuelError) -> Unit ){
            val url = "${HOST}/empleos/${id}/etapas/inscripcion"
            Fuel.get(url).responseString{ request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene el detalle de una convocatoria
         * @param id id de la convocatoria seleccionada
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getConvocatory(id: String, success: (Convocatory) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}convocatorias/${id}"
            Fuel.get(url).responseObject(Convocatory.Deserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene las etapas de una convocatoria
         * @param idConvocatory id de la convocatoria seleccionada
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getStages(idConvocatory: String, success: (List<Convocatory.Stage>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}convocatorias/${idConvocatory}/etapas/activas?page=0&size=10"
            Fuel.get(url).responseObject(Convocatory.Stage.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene la normatividad relacionada con una convocatoria
         * @param idConvocatory id de la convocatoria seleccionada
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getNormativity(idConvocatory: String, success: (List<Normativity>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}convocatorias/${idConvocatory}/acuerdosPublicados?page=0&size=200"
            Fuel.get(url).responseObject(Normativity.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene el listado de entidades OPEC
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getEntities(success: (List<Entity>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}entidades/opecVisible/list/"
            Fuel.get(url).responseObject(Entity.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene el listado de convocatorias visibles (sin login)
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getConvocatories(success: (List<Convocatory>) -> Unit, error: (FuelError) -> Unit): Request {
            val url = "${HOST}convocatorias/visibles/list/?nombre=*"
            return Fuel.get(url).responseObject(Convocatory.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene el listado de convocatorias visibles (con login)
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getConvocatoriesLogged(success: (List<Convocatory>) -> Unit, error: (FuelError) -> Unit): Request {
            val url = "${HOST}convocatorias/?search_opecVisible=true&size=100"
            return Fuel.get(url).responseObject(Convocatory.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene el listado de departamentos en los que estan disponible ofertas laborales
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getDepartmentsOPEC(success: (List<Department>) -> Unit, error: (FuelError) -> Unit) {
            //val url = "${HOST}empleos/opecvisible/generales/departamento"
            val url = "${HOST}departamento"
            Fuel.get(url).responseObject(Department.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene el listado de todos los departamentos
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getDepartments(success: (List<Department>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}departamento/"
            Fuel.get(url).responseObject(Department.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene el listado de municipios en los que estan disponible ofertas laborales
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getCitiesOPEC(success: (List<City>) -> Unit, error: (FuelError) -> Unit) {
            //val url = "${HOST}municipio/vacantesbyMunicipio/"
            val url = "${HOST}municipio/"
            Fuel.get(url).responseObject(City.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene el listado de todos los municipios del pais
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getCities(success: (List<City>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}municipio/"
            Fuel.get(url).responseObject(City.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene la ciudad en la que se presentará la prueba
         * @param idPrueba id de la Prueba
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        /*fun getCityOfTests(idPrueba: String?, success: (List<City>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}convocatorias/-1/pruebas/$idPrueba/lugares/municipios/"
            Fuel.get(url).responseObject(City.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }*/

        /**
         * Obtiene el departamento en el que se presentará la prueba
         * @param idPrueba id de la Prueba
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        /*fun getDepartmentOfTests(idPrueba: String?, success: (List<Department>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}convocatorias/-1/pruebas/$idPrueba/lugares/departamentos/"
            Fuel.get(url).responseObject(Department.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }*/

        /**
         * Obtiene el listado de rangos de salario disponibles
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getRanges(success: (List<SalarialRange>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}empleos/opecvisible/generales/rangos"
            Fuel.get(url).responseObject(SalarialRange.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene el listado de niveles profesionales de empleo disponibles
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getLevels(success: (List<IdName>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}niveles/list"
            Fuel.get(url).responseObject(IdName.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene el listado de tipo de productos intelectuales
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getTypeProducts(success: (List<IdDescription>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}tipoprod"
            Fuel.get(url).responseObject(IdDescription.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene el listado de tipos de documento para 'Otros documentos'
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getTypeInformation(success: (List<TypeData>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}tipoinformacion"
            Fuel.get(url).responseObject(TypeData.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene el listado de empleos de un usuario dependiendo de su estado (F,I,PI)
         * @param status estado de los empleos que se quieren obtener
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getMyJobs(status: String, success: (List<Inscription>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}inscripcion/empleosByEstado/${status}/?page=0&size=100"
            Fuel.get(url).responseObject(Inscription.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        fun getMyJobsString(status: String, success: (String) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}inscripcion/empleosByEstado/${status}/?page=0&size=100"
            Fuel.get(url).responseString { request, response, result ->
                result.fold(success, error)
            }
        }


        /**
         * Obtiene el listado de paises del mundo
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getCountries(success: (List<Country>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}pais/list/?nombre=*"
            Fuel.get(url).responseObject(Country.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene el listado de tipos de periodicidad
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getPeriodicity(success: (List<IdName>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}periodicidad/?nombre=*"
            Fuel.get(url).responseObject(IdName.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene el listado de niveles educativos
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getEducationLevels(success: (List<EducationalLevel>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}nivelEducativo"
            Fuel.get(url).responseObject(EducationalLevel.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene los resultados de inscripción de una oferta
         * @param idJob id del trabajo del que se quieren obtener los resultados
         * @param idInscription id de la inscripción
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getTotalResultInscription(idJob: String?, idInscription: String?, success: (TotalResult) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}evaluacion/inscripcion/resultadosconsolidados/$idJob/$idInscription"
            Fuel.get(url).responseObject(TotalResult.Deserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene el listado con puntajes y número de inscripción de aspirantes inscritos en el empleo que continúan en concurso
         * @param idJob id del trabajo del que se quieren obtener los resultados
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getListOfEnrollmentScores(idJob: String?, success: (List<PuntajesQueContinuan>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}evaluacion/inscripcion/resultadosconsolidados/$idJob"
            Fuel.get(url).responseObject(PuntajesQueContinuan.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene el listado de resultados de las pruebas de la inscripción (RESULTADOS Y RECLAMACIONES A PRUEBAS)
         * @param idInscription id de la inscripción
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getTestResultsInscription(idInscription: String?, success: (List<InscriptionResult>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}evaluacion/inscripcion/$idInscription?page=0&size=10"
            Fuel.get(url).responseObject(InscriptionResult.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene la sumatoria de puntajes obtenidos en una inscripcion (SUMATORIA DE PUNTAJES)
         * @param idInscription id de la inscripción
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getSummationScoresInscription(idInscription: String?, success: (List<InscriptionResult>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}evaluacion/inscripcion/resultadospruebas/$idInscription?page=0&size=10"
            Fuel.get(url).responseObject(InscriptionResult.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Obtiene el listado de quejas y reclamos que tiene una inscripción (VER RECLAMACIONES)
         * @param idInscription id de la inscripción
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getComplaintsInscription(idInscription: String?, idTest: String?, success: (List<ComplaintTutelage>) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}reclamaciones?idPrueba=$idTest&idInscripcion=$idInscription&page=0&size=10"
            Fuel.get(url).responseObject(ComplaintTutelage.ListDeserializer()) { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Establece una oferta laboral como favorita o no favorita
         * @param idJob id del empleo
         * @param idInscription id de la inscripción
         * @param isFavorite si va a ser favorito o no
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responda con un mensaje de error
         */
        fun setOrRemoveAsFavorite(idJob: String?, idInscription: String?, isFavorite: Boolean?,
                                  success: (WorkOffer) -> Unit, error: (FuelError) -> Unit) {
            val url = "$HOST/empleos/ofertaPublica/$idJob"
            val mainJson = JsonObject()
            val jobJson = JsonObject()
            jobJson.addProperty("id", idJob)
            mainJson.add("empleo", jobJson)
            if (idInscription != null && isFavorite == false) {
                mainJson.addProperty("inscripcionId", idInscription)
            }
            val jsonBody = mainJson.toString()
            val req = Fuel.put(url)
            req.body(jsonBody, Charset.defaultCharset())
            req.headers["Content-Type"] = CONTENT_TYPE_JSON
            req.responseObject(WorkOffer.Deserializer()) { _, _, result ->
                result.fold(success, error)
            }
        }

        /**
         * Muestra los diferentes estados de inscripcion que tiene un empleo, orientando el comportamiento de los botones a mostrar
         * @param idEmpleo id del Empleo
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responsa con un mensaje de error
         */
        fun getInscriptionStatus(idEmpleo: String?, success: (String) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}inscripcion/$idEmpleo"
            Fuel.get(url).responseString { request, response, result ->
                result.fold(success, error)
            }
        }

        /**
         * Servicio sincronizados para filtros de información
         *
         */

        /**
         * Obtiene el listado de Instituciones
         * @param query Palabra por la que se quiere filtrar
         */
        fun getInstitutionsSync(query: String): List<Institution> {
            val finalQ = if (query == null || query.isBlank()) {
                "*"
            } else {
                query
            }
            val url = "${HOST}/institucionedu/list/?nombre=$finalQ"
            val (request, response, result) = Fuel.get(url).responseObject(Institution.ListDeserializer())
            return result.get()
        }

        /**
         * Obtiene el listado de Programas Institucionales
         * @param idInstitution id de la institucion seleccionada
         * @param query Palabra por la que se quiere filtrar
         */
        fun getProgrammsSync(idInstitution: String, query: String): List<Program> {
            val finalQ = if (query == null || query.isBlank()) {
                "*"
            } else {
                query
            }
            val url = "${HOST}/programaedu/list/?institucionId=$idInstitution&nombre=$finalQ"
            val (request, response, result) = Fuel.get(url).responseObject(Program.ListDeserializer())
            return result.get()
        }

        fun getProgrammsConvSync(idNivelEdu: String): List<Program> {
            val url = "${HOST}/formacion/disciplinasAcademicas/nivelEducacionFormal/$idNivelEdu/listar/?size=2000"
            val (request, response, result) = Fuel.get(url).responseObject(Program.ListDeserializer())
            return result.get()
        }




        /**
         * Obtiene el listado de Convocatorias
         * @param query Palabra por la que se quiere filtrar
         */
        fun getConvocatoriesSync(query: String): List<Convocatory> {
            val finalQ = if (query == null || query.isBlank()) {
                "*"
            } else {
                query
            }
            val url = "${HOST}convocatorias/visibles/list/?nombre=${finalQ}&page=0&size=20"
            val (request, response, result) = Fuel.get(url).responseObject(Convocatory.ListDeserializer())
            return result.get()
        }

        /**
         * Obtiene el total de inscritos en un empleo
         * @param id id del trabajo del que se quieren obtener los resultados
         * @param success evento en el caso de que el llamado sea exitoso
         * @param error evento en el caso de que el llamado responda con un mensaje de error
         */
        fun getTotalCitizensEnrolled(id: String?, success: (String) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}inscripcion/inscritosporempleo/$id/estado/I/count/"
            Fuel.get(url).responseString { request, response, result ->
                result.fold(success, error)
            }
        }

        /*fun confirmPaymentTransfer(idEmpleoActual: String?, idEmpleoNuevo: String?, idDeInscripcion: String?, success: (Json) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}inscripcion/cambiarempleo/"
            val params = listOf("id_empleo_old" to idEmpleoActual, "id_empleo_new" to idEmpleoNuevo, "id_inscripcion" to idDeInscripcion)
            Fuel.post(url, params).responseJson { request, response, result ->
                result.fold(success, error)
            }
        }*/

        /**
         * Facilita el cambio de contraseña para ingreso a SIMO
         * @param idEmpleoActual id del Empleo Actual
         * @param idEmpleoNuevo id del Empleo Nuevo
         * @param idDeInscripcion id de Inscripciòn
         */
        fun confirmPaymentTransfer(idEmpleoActual: String?, idEmpleoNuevo: String?, idDeInscripcion: String?, success: (FuelJson) -> Unit, error: (FuelError) -> Unit) {
            val url = "${HOST}inscripcion/cambiarempleo/"

            val jsonBody = JsonObject()

            jsonBody.addProperty("id_empleo_old", idEmpleoActual)
            jsonBody.addProperty("id_empleo_new", idEmpleoNuevo)
            jsonBody.addProperty("id_inscripcion", idDeInscripcion)

            val request = Fuel.post(url).body(jsonBody.toString(), Charset.defaultCharset())
            request.headers["Content-Type"] = CONTENT_TYPE_JSON
            request.responseJson { request, response, result ->
                result.fold(success, error)
            }
        }

    }

}