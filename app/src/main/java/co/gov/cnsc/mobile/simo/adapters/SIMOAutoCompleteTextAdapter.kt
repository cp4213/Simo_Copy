package co.gov.cnsc.mobile.simo.adapters


import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.app.AppCompatActivity
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.SIMOApplication
import co.gov.cnsc.mobile.simo.activities.AdvanceSearchActivity
import co.gov.cnsc.mobile.simo.network.RestAPI
import co.gov.cnsc.mobile.simo.storage.SIMOResources

/**
 * Esta clase contiene la funcionalidad para administrar los recursos SIMO que se autocompletan en un campo
 * @param activity Activity sobre la cual se ejecuta el autocompletado
 * @param typeResources Tipo de recursos que se quieren mostrar (Departamentos, ciudades, paises)
 * @param idFilter si el filtro requiere de un id de departamento o un id padre para ejecutarse
 */
class SIMOAutoCompleteTextAdapter(val activity: AppCompatActivity, val typeResources: Int, var idFilter: String? = null) :
        ArrayAdapter<Any>(activity, android.R.layout.simple_list_item_1), Filterable {


    /**
     * Items con los que se puede autocompletar un campo
     */
    var items: List<Any>? = listOf()

    /**
     * Número de items en el listado
     */
    override fun getCount(): Int {
        return if (items == null) {
            0
        } else {
            items!!.size
        }
    }

    /**
     * Obtiene un objeto del listado
     */
    override fun getItem(position: Int): Any {
        return items!![position]
    }

    /**
     * Ejecución del filtro a partir de una serie de caracteres o letras
     */
    override fun getFilter(): Filter {
        val myFilter = object : Filter() {

            override fun performFiltering(constraint: CharSequence?): Filter.FilterResults? {
                val filterResults = Filter.FilterResults()
                var queryResults: List<Any>?
                try {
                    val query = constraint?.toString() ?: ""
                    queryResults = getResources(query, typeResources, idFilter)
                } catch (e: Exception) {
                    queryResults = listOf()
                    Log.e("myException", e.message!!)
                }
                filterResults.values = queryResults
                filterResults.count = queryResults!!.size
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults?) {
                items = results?.values as List<Any>?
                if (results != null && results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
        return myFilter
    }

    /**
     * Obtiene el listado de recursos que se quieren mostrar
     */
    fun getResources(query: String, type: Int, idFilter: String? = null): List<Any>? {
        var list: List<Any>? = listOf()
        when (type) {
            TYPE_EDUCATIONAL_INTITUTIONS -> {
                list = RestAPI.getInstitutionsSync(query)
            }
            TYPE_COUNTRIES -> {
                list = SIMOResources.filterCountries(context, query)
            }
            TYPE_DEPARTMENTS_OPEC -> {
                list = SIMOResources.filterDepartmentsOPEC(context, query)
            }
            TYPE_CITIES_OPEC -> {
                if (idFilter != null)
                    list = SIMOResources.filterCitiesOPEC(idFilter, context, query)
            }
            TYPE_EDUCATIONAL_LEVELS -> {
                list = SIMOResources.filterEducationalLevels(context, query)
            }
            TYPE_INSTITUTION_PROGRAMM -> {
                if (idFilter != null)
                    list = RestAPI.getProgrammsSync(idFilter, query)
            }
            TYPE_DEPARTMENTS -> {
                list = SIMOResources.filterDepartments(context, query)
            }
            TYPE_CITIES -> {
                if (idFilter != null)
                    list = SIMOResources.filterCities(idFilter, context, query)
            }
            TYPE_ENTITIES -> {
                list = SIMOResources.filterEntities(context, query)
            }
            TYPE_SELECTION_PROCESS -> {
                list = RestAPI.getConvocatoriesSync(query)
            }
            TYPE_SALARIAL_RANGE -> {
                list = SIMOResources.filterSalarialRange(context, query)
            }
            TYPE_LEVELS -> {
                list = SIMOResources.filterLevels(context, query)
            }
            TYPE_KEY_WORDS -> {
                list = SIMOResources.filterKeyWords(context, query)
            }
            TYPE_CITIES_OPEC_COMPLETE -> {
                list = SIMOResources.filterCitiesCompleteOPEC(context, query)
            }
            TYPE_INSTITUTION_CONVALIDATE_PROGRAMM -> {
                if (idFilter != null){
                    list = RestAPI.getProgrammsConvSync(idFilter)
                    list =list.filter { item -> SIMOApplication.removerTildes(item.toString()).contains(SIMOApplication.removerTildes(query), true)}
                }
            }
        }
        return list
    }

    /**
     * Obtiene un titulo o nombre de los recursos que se quieren mostrar
     */
    val titleSelectResource: String?
        get() {
            var title: String? = null
            when (typeResources) {
                TYPE_EDUCATIONAL_INTITUTIONS -> {
                    title = context.getString(R.string.select_educational_institucional)
                }
                TYPE_COUNTRIES -> {
                    title = context.getString(R.string.select_country)
                }
                TYPE_DEPARTMENTS_OPEC -> {
                    title = context.getString(R.string.select_department)
                }
                TYPE_CITIES_OPEC -> {
                    title = context.getString(R.string.select_city)
                }
                TYPE_EDUCATIONAL_LEVELS -> {
                    title = context.getString(R.string.select_educational_level)
                }
                TYPE_INSTITUTION_PROGRAMM -> {
                    title = context.getString(R.string.select_programm_institutional)
                }
                TYPE_DEPARTMENTS -> {
                    title = context.getString(R.string.select_department)
                }
                TYPE_CITIES -> {
                    title = context.getString(R.string.select_city)
                }
                TYPE_ENTITIES -> {
                    title = context.getString(R.string.select_entities)
                }
                TYPE_SELECTION_PROCESS -> {
                    title = context.getString(R.string.select_convocatory)
                }
                TYPE_SALARIAL_RANGE -> {
                    title = context.getString(R.string.select_range_salarial)
                }
                TYPE_LEVELS -> {
                    title = context.getString(R.string.select_level)
                }
                TYPE_CITIES_OPEC_COMPLETE -> {
                    title = context.getString(R.string.select_city)
                }
                TYPE_INSTITUTION_CONVALIDATE_PROGRAMM-> {
                    title = context.getString(R.string.select_programm_institutional)
                }
            }
            return title
        }


    companion object {
        const val TYPE_EDUCATIONAL_INTITUTIONS = 1
        const val TYPE_COUNTRIES = 2
        const val TYPE_DEPARTMENTS_OPEC = 3
        const val TYPE_CITIES_OPEC = 6
        const val TYPE_EDUCATIONAL_LEVELS = 4
        const val TYPE_INSTITUTION_PROGRAMM = 5
        const val TYPE_DEPARTMENTS = 7
        const val TYPE_CITIES = 8
        const val TYPE_ENTITIES = 9
        const val TYPE_SELECTION_PROCESS = 10
        const val TYPE_SALARIAL_RANGE = 11
        const val TYPE_LEVELS = 12
        const val TYPE_KEY_WORDS = 13
        const val TYPE_CITIES_OPEC_COMPLETE = 14
        const val TYPE_INSTITUTION_CONVALIDATE_PROGRAMM = 15
    }

}