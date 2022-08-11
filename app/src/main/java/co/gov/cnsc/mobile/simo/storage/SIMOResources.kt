package co.gov.cnsc.mobile.simo.storage

import android.content.Context
import android.util.Log
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.extensions.deletePreference
import co.gov.cnsc.mobile.simo.extensions.getPreferenceString
import co.gov.cnsc.mobile.simo.extensions.savePreferenceString
import co.gov.cnsc.mobile.simo.models.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Clase que administra la obtención y el almacenamiento de los listados de recursos que se
 * necesitan para los formularios de SIMO
 */
class SIMOResources {

    companion object {

        const val KEY_WORDS = "key_words"
        const val ENTITIES = "entities"
        const val DEPARTMENTS_OPEC = "departments_opec"
        const val DEPARTMENTS = "departments"
        const val CITIES_OPEC = "cities_opec"
        const val CITIES = "cities"
        const val CONVOCATORIES = "convocatories"
        const val SALARIAL_RANGES = "salarial_ranges"
        const val LEVELS = "levels"
        const val EDUCATIONAL_LEVELS = "educational_levels"
        const val COUNTRIES = "countries"
        val urbanVia = listOf<IdDescription>(
            IdDescription("Anillo vial","Anillo vial"),
            IdDescription("Autopista","Autopista"),IdDescription("Avenida","Avenida"),
            IdDescription("Avenida calle","Avenida calle"),IdDescription("Avenida carrera","Avenida carrera"),
            IdDescription("Calle","Calle"),IdDescription("Callejon","Callejon"),
            IdDescription("Carrera","Carrera"),IdDescription("Circular","Circular"),
            IdDescription("Circunvalar","Circunvalar"),IdDescription("Diagonal","Diagonal"),
            IdDescription("Transversal","Transversal"),IdDescription("Troncal","Troncal")
        )
        val ruralVia = listOf<IdDescription>(
            IdDescription("Camino","Camino"),
            IdDescription("Carretera","Carretera"),IdDescription("Casa","Casa"),
            IdDescription("Circular","Circular"),IdDescription("Corregimiento","Corregimiento"),
            IdDescription("Finca","Finca"),IdDescription("Inspección","Inspección"),
            IdDescription("Kilómetro","Kilómetro"),IdDescription("Lote","Lote"),
            IdDescription("Variante","Variante"),IdDescription("Vereda","Vereda"),
            IdDescription("Vía","Vía"),IdDescription("Zona","Zona")
        )
        val ruralViab = listOf<IdDescription>(
            IdDescription("empty",""),IdDescription("Camino","Camino"),
            IdDescription("Carretera","Carretera"),IdDescription("Casa","Casa"),
            IdDescription("Circular","Circular"),IdDescription("Corregimiento","Corregimiento"),
            IdDescription("Finca","Finca"),IdDescription("Inspección","Inspección"),
            IdDescription("Kilómetro","Kilómetro"),IdDescription("Lote","Lote"),
            IdDescription("Variante","Variante"),IdDescription("Vereda","Vereda"),
            IdDescription("Vía","Vía"),IdDescription("Zona","Zona")
        )
        val cuadrante = listOf<IdDescription>(IdDescription("empty",""),
            IdDescription("ESTE","ESTE"),
            IdDescription("NORTE","NORTE"),IdDescription("OESTE","OESTE"),
            IdDescription("SUR","SUR")
        )
        val letter = listOf<IdDescription>(IdDescription("empty",""),
            IdDescription("A","A"),
            IdDescription("B","B"),IdDescription("C","C"),
            IdDescription("D","D"),IdDescription("E","E"),
            IdDescription("F","F"),IdDescription("G","G"),
            IdDescription("H","H"),IdDescription("I","I"),
            IdDescription("J","J"),IdDescription("K","K"),
            IdDescription("L","L"),IdDescription("M","M"),
            IdDescription("N","N"),IdDescription("O","O"),
            IdDescription("P","P"),IdDescription("Q","Q"),
            IdDescription("R","R"),IdDescription("S","S"),
            IdDescription("T","T"),IdDescription("U","U"),
            IdDescription("V","V"),IdDescription("W","W"),
            IdDescription("X","X"),IdDescription("Y","Y"),
            IdDescription("Z","Z")
        )

        /**
         * Guarda un listado de items de cualquier tipo
         * @param context Contexto de la aplicación
         * @param name Nombre del listado a guardar
         * @param items Listado de recursos a guardar
         */
        private fun setListResources(context: Context, name: String, items: List<Any>) {
            val json = Gson().toJson(items)
            context.savePreferenceString(name, json)
        }

        /**
         * Guarda el listado de entidades en las preferencias del usuario
         * @param context Contexto de la aplicación
         * @param entities Listado de entidades
         */
        fun setEntities(context: Context, entities: List<Entity>) {
            setListResources(context, ENTITIES, entities)
        }

        /**
         * Obtiene el listado de entidades guardadas en las preferencias del usuario
         * @param context Contexto de la aplicación
         */
        private fun getEntities(context: Context): List<Entity> {
            val json = context.getPreferenceString(ENTITIES)
            val type = object : TypeToken<List<Entity>>() {}.type
            return Gson().fromJson(json, type)
        }

        /**
         * Filtra el listado de entidades a partir de una palabra clave
         * @param context Contexto de la aplicación
         * @param query Palabra clave para filtrar
         */
        fun filterEntities(context: Context?, query: String): List<Entity> {
            if (context != null && query != null) {
                val entities = getEntities(context).filter { item -> SIMOApplication.removerTildes(item.toString()).contains(SIMOApplication.removerTildes(query), true) }
                return entities
            } else {
                return listOf()
            }
        }

        /**
         * Guarda el listado de departamentos en los que hay disponibles ofertas laborales
         * @param context Contexto de la aplicación
         * @param departments Listado de departamentos
         */
        fun setDepartmentsOPEC(context: Context, departments: List<Department>) {
            setListResources(context, DEPARTMENTS_OPEC, departments)
        }

        /**
         * Guarda el listado de departamentos que hay en el pais
         * @param context Contexto de la aplicación
         * @param departments Listado de departamentos
         */
        fun setDepartments(context: Context, departments: List<Department>) {
            setListResources(context, DEPARTMENTS, departments)
        }

        /**
         * Obtiene el listado de departamentos en los que hay disponibles ofertas laborales
         * @param context Contexto de la aplicación
         */
        private fun getDepartmentsOPEC(context: Context): List<Department> {
            val json = context.getPreferenceString(DEPARTMENTS_OPEC)
            val type = object : TypeToken<List<Department>>() {}.type
            if (json != null) {
                return Gson().fromJson(json, type)
            } else {
                return listOf()
            }
        }

        /**
         * Obtiene el listado de departamentos del país
         * @param context Contexto de la aplicación
         */
        private fun getDepartments(context: Context): List<Department> {
            val json = context.getPreferenceString(DEPARTMENTS)
            val type = object : TypeToken<List<Department>>() {}.type
            if (json != null) {
                return Gson().fromJson(json, type)
            } else {
                return listOf()
            }
        }

        /**
         * Obtiene el listado de departamentos del país
         * @param context Contexto de la aplicación
         */
        private fun getSelectionProcess(context: Context): List<Convocatory> {
            val json = context.getPreferenceString(CONVOCATORIES)
            val type = object : TypeToken<List<Convocatory>>() {}.type
            if (json != null) {
                return Gson().fromJson(json, type)
            } else {
                return listOf()
            }
        }

        /**
         * Obtiene un objeto departamento a partir del nombre del mismo
         * @param context Contexto de la aplicación
         * @param query Nombre del departamento
         */
        fun findDepartment(context: Context?, query: String): Department? {
            if (context != null && !query.isNullOrBlank()) {
                return getDepartments(context).filter { item -> item.realName == query }.firstOrNull()
            } else {
                return null
            }
        }
        fun findSelectionProcess(context: Context?, query: String): Convocatory? {
            if (context != null && !query.isNullOrBlank()) {
                return getSelectionProcess(context).filter { item -> item.fullNameConvocatory == query }.firstOrNull()
            } else {
                return null
            }
        }

        /**
         * Filtra el listado de departamentos OPEC a partir de una palabra clave
         * @param context Contexto de la aplicación
         * @param query Palabra clave para filtrar
         */
        fun filterDepartmentsOPEC(context: Context?, query: String): List<Department> {
            if (context != null) {
                return getDepartmentsOPEC(context).filter { item -> SIMOApplication.removerTildes(item.realName).contains(SIMOApplication.removerTildes(query), true) }
            } else {
                return listOf()
            }
        }

        /**
         * Filtra el listado de departamentos a partir de una palabra clave
         * @param context Contexto de la aplicación
         * @param query Palabra clave para filtrar
         */
        fun filterDepartments(context: Context?, query: String): List<Department> {
            if (context != null) {
                return getDepartments(context).filter { item -> SIMOApplication.removerTildes(item.realName).contains(SIMOApplication.removerTildes(query), true) }
            } else {
                return listOf()
            }
        }

        /**
         * Guarda el listado de ciudades en los que hay disponibles ofertas laborales
         * @param context Contexto de la aplicación
         * @param cities Listado de ciudades
         */
        fun setCitiesOPEC(context: Context, cities: List<City>) {
            setListResources(context, CITIES_OPEC, cities)
        }

        /**
         * Guarda el listado de ciudades de toudo el país
         * @param context Contexto de la aplicación
         * @param cities Listado de ciudades
         */
        fun setCities(context: Context, cities: List<City>) {
            setListResources(context, CITIES, cities)
        }

        /**
         * Obtiene el listado de ciudades del deaprtamento en los que hay disponibles ofertas laborales
         * @param idDepartment id del departamento
         * @param context Contexto de la aplicación
         */
        private fun getCitiesOPEC(idDepartment: String?, context: Context): List<City> {
            val json = context.getPreferenceString(CITIES_OPEC)
            val type = object : TypeToken<List<City>>() {}.type
            var items = Gson().fromJson<List<City>>(json, type)
            if (items == null) {
                items = listOf()
            }
            if (idDepartment != null) {
                return items.filter { d -> d.department?.id == idDepartment }
            } else {
                return items
            }
        }

        /**
         * Obtiene el listado de ciudades en los que hay disponibles ofertas laborales
         * @param context Contexto de la aplicación
         */
        private fun getCitiesOPEC(context: Context): List<City.Complete> {
            val json = context.getPreferenceString(CITIES_OPEC)
            val type = object : TypeToken<List<City.Complete>>() {}.type
            var items = Gson().fromJson<List<City.Complete>>(json, type)
            if (items == null) {
                items = listOf()
            }
            return items
        }

        /**
         * Obtiene el listado de ciudades de un departamento
         * @param idDepartment id del departamento a traer las ciudades
         * @param context Contexto de la aplicación
         */
        private fun getCities(idDepartment: String?, context: Context): List<City> {
            val json = context.getPreferenceString(CITIES)
            val type = object : TypeToken<List<City>>() {}.type
            var items = Gson().fromJson<List<City>>(json, type)
            if (items == null) {
                items = listOf()
            }
            if (idDepartment != null) {
                return items.filter { d -> d.department?.id == idDepartment }
            } else {
                return items
            }
        }

        /**
         * Filtra el listado de ciudades del pais de un departamento a través de una palabra clave
         * @param idDepartment id del departamento
         * @param context Contexto de la aplicación
         * @param query Palabra clave para filtrar el listado
         * Se añadió la función removerTildes para que los resultados del filtro ignoren ausencia/presencia de tildes
         */
        fun filterCities(idDepartment: String?, context: Context?, query: String): List<City> {
            if (context != null) {
                val cities = getCities(idDepartment, context).filter { item ->
                    SIMOApplication.removerTildes(item.toString()).contains(SIMOApplication.removerTildes(query), true)
                }
                return cities
            } else {
                return listOf()
            }
        }

        /**
         * Filtra el listado de ciudades del pais de un departamento a través de una palabra clave
         * donde hay ofertaa laborales
         * @param idDepartment id del departamento
         * @param context Contexto de la aplicación
         * @param query Palabra clave para filtrar el listado
         */
        fun filterCitiesOPEC(idDepartment: String?, context: Context?, query: String): List<City> {
            if (context != null) {
                val cities = getCitiesOPEC(idDepartment, context).filter { item -> SIMOApplication.removerTildes(item.toString()).contains(SIMOApplication.removerTildes(query), true) }
                return cities
            } else {
                return listOf()
            }
        }

        /**
         * Filtra el listado de ciudades del pais donde hay ofertas laborales
         * @param context Contexto de la aplicación
         * @param query Palabra clave para filtrar el listado
         */
        fun filterCitiesCompleteOPEC(context: Context?, query: String): List<City.Complete> {
            if (context != null) {
                val cities = getCitiesOPEC(context).filter { item -> SIMOApplication.removerTildes(item.toString()).contains(SIMOApplication.removerTildes(query), true) }
                return cities
            } else {
                return listOf()
            }
        }

        /**
         * Obtiene un objeto ciudad OPEC a partir del nombre del mismo
         * @param context Contexto de la aplicación
         * @param query Nombre de la ciudad
         */
        fun findCityOPEC(context: Context?, query: String): City? {
            if (context != null && !query.isNullOrBlank()) {
                return getCitiesOPEC(context).filter { item -> item.toString() == query }.firstOrNull()
            } else {
                return null
            }
        }

        /**
         * Obtiene un objeto ciudad a partir del nombre del mismo
         * @param context Contexto de la aplicación
         * @param query Nombre de la ciudad
         */
        fun findCity(context: Context?, query: String): City? {
            if (context != null && !query.isNullOrBlank()) {
                return getCities(null, context).filter { item -> item.toString() == query }.firstOrNull()
            } else {
                return null
            }
        }

        /**
         * Guarda el listado de convocatorias disponibles
         * @param context Contexto de la aplicación
         * @param convocatories Listado de convocatorias
         */
        fun setConvocatories(context: Context, convocatories: List<Convocatory>) {
            setListResources(context, CONVOCATORIES, convocatories)
        }

        /**
         * Guarda el listado de rangos salariales disponibles
         * @param context Contexto de la aplicación
         * @param salarialSalarial listado de rangos salariales
         */
        fun setSalarialRanges(context: Context, salarialSalarial: List<SalarialRange>) {
            setListResources(context, SALARIAL_RANGES, salarialSalarial)
        }

        private fun getSalarialRanges(context: Context): List<SalarialRange> {
            val json = context.getPreferenceString(SALARIAL_RANGES)
            val type = object : TypeToken<List<SalarialRange>>() {}.type
            return Gson().fromJson(json, type)
        }

        fun filterSalarialRange(context: Context?, query: String): List<SalarialRange> {
            if (context != null) {
                val salaries = getSalarialRanges(context).filter { item -> item.toString().contains(query, true) }
                return salaries
            } else {
                return listOf()
            }
        }

        /**
         * Guarda el listado de niveles disponibles
         * @param context Contexto de la aplicación
         * @param levels Niveles
         */
        fun setLevels(context: Context, levels: List<IdName>) {
            setListResources(context, LEVELS, levels)
        }

        /**
         * Guarda el listado de niveles educativos
         * @param context Contexto de la aplicación
         * @param educationalLevels Listado de niveles educativos
         */
        fun setEducationalLevels(context: Context, educationalLevels: List<EducationalLevel>) {
            setListResources(context, EDUCATIONAL_LEVELS, educationalLevels)
        }

        /**
         * Guarda el listado de paises del mundo
         * @param context Contexto de la aplicación
         * @param countries Listado de paises
         */
        fun setCountries(context: Context, countries: List<Country>) {
            setListResources(context, COUNTRIES, countries)
        }

        /**
         * Obtiene el listado de Niveles desde el almacenamiento del dispositivo
         * @param context Contexto de la aplicación
         */
        private fun getLevels(context: Context): List<IdName> {
            val json = context.getPreferenceString(LEVELS)
            val type = object : TypeToken<List<IdName>>() {}.type
            return Gson().fromJson(json, type)
        }

        fun filterLevels(context: Context?, query: String): List<IdName> {
            if (context != null) {
                val levels = getLevels(context).filter { item -> item.toString().contains(query, true) }
                return levels
            } else {
                return listOf()
            }
        }

        private fun getEducationalLevels(context: Context): List<EducationalLevel> {
            val json = context.getPreferenceString(EDUCATIONAL_LEVELS)
            val type = object : TypeToken<List<EducationalLevel>>() {}.type
            return Gson().fromJson(json, type)
        }

        fun filterEducationalLevels(context: Context?, query: String): List<EducationalLevel> {
            if (context != null) {
                val educationalLevels = getEducationalLevels(context).filter { item -> item.toString().contains(query, true) }
                return educationalLevels
            } else {
                return listOf()
            }
        }

        private fun getCountries(context: Context): List<Country> {
            val json = context.getPreferenceString(COUNTRIES)
            val type = object : TypeToken<List<Country>>() {}.type
            return Gson().fromJson(json, type)
        }

        fun filterCountries(context: Context, query: String): List<Country> {
            if (context != null) {
                val countries = getCountries(context).filter { item -> SIMOApplication.removerTildes(item.toString()).contains(SIMOApplication.removerTildes(query), true) }
                return countries
            } else {
                return listOf()
            }
        }

        /**
         * Borra el listado de palabras clave buscadas anteriormente
         * @param context Contexto de la aplicación
         */
        fun removeKeyWords(context: Context) {
            context.deletePreference(KEY_WORDS)
        }


        fun getKeyWords(context: Context): List<String> {
            val json = context.getPreferenceString(KEY_WORDS)
            if (json != null) {
                val type = object : TypeToken<List<String>>() {}.type
                return Gson().fromJson(json, type)
            } else {
                return listOf()
            }
        }

        fun filterKeyWords(context: Context, query: String): List<String> {
            if (context != null) {
                val keyWords = getKeyWords(context).filter { item -> SIMOApplication.removerTildes(item.toString()).contains(SIMOApplication.removerTildes(query), true) }
                return keyWords
            } else {
                return listOf()
            }
        }

        /**
         * Agrega una palabra clave al listado de sugerencias de empleos buscados
         * @param context Contexto de la aplicación
         * @param keyWord Palabra clave a agregar al listado
         */
        fun tryAddKeyWord(context: Context, keyWord: String) {
            if (keyWord.length > 2) {
                val listWords = getKeyWords(context).toMutableList()
                val findWord = listWords.find { it ->
                    it.equals(keyWord, true)
                }
                if (findWord == null) {
                    if (listWords.size > 50) {
                        listWords.removeAt(0)
                    }
                    listWords.add(keyWord)
                    val json = Gson().toJson(listWords)
                    context.savePreferenceString(KEY_WORDS, json)
                }
            }
        }
    }
}