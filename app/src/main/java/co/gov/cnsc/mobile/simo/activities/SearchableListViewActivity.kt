package co.gov.cnsc.mobile.simo.activities

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.adapters.SIMOAutoCompleteTextAdapter
import co.gov.cnsc.mobile.simo.extensions.setOnItemClickListener
import kotlinx.android.synthetic.main.activity_searchable_list_view.*

/**
 * Esta clase maneja la funcionalidad de los listados de recursos de SIMO
 * Paises, Departamentos, Municipios, entidades, etc
 */
class SearchableListViewActivity : AppCompatActivity() {

    /**
     * Tipo de recurso que se desea listar
     */
    var typeResource: Int? = null

    /**
     * Adaptador de recursos SIMO
     */
    var adapterResource: SIMOAutoCompleteTextAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("Act","SearchableListViewA")
        setContentView(R.layout.activity_searchable_list_view)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        typeResource = intent?.extras?.getInt("type_resource")
        val idFilter = intent?.extras?.getString("id_filter")
        //val query = intent?.extras?.getString("query","")
        adapterResource = SIMOAutoCompleteTextAdapter(this, typeResource!!, idFilter)
        listViewResource.adapter = adapterResource
        textViewTitle.text = adapterResource?.titleSelectResource
        //searchViewResource.setQuery(query,true)
        //adapterResource?.filter?.filter(query)
        adapterResource?.filter?.filter("")
        configureUI()
    }

    /**
     * Configura elementos del layout como el buscador y el listado de recursos
     */
    private fun configureUI() {
        searchViewResource.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapterResource?.filter?.filter(newText)
                return true
            }
        })
        listViewResource.setOnItemClickListener { position, item ->
            returnToBack(item)
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        window?.decorView?.clearFocus()

    }

    /**
     * Cuando se da tap sobre el botón Cerrar de la pantalla/popup dialog
     */
    fun onClose(button: View) {
        finish()
    }

    /**
     * Retorna a la pantalla anterior y le envía el item seleccionado por el usuario
     */
    private fun returnToBack(item: Any?) {
        val intent = Intent()
        intent.putExtra("item", item as Parcelable)
        setResult(RESULT_OK, intent)
        finish()
    }

    /**
     * Finaliza la pantalla y esconde el teclado
     */
    override fun finish() {
        window?.decorView?.clearFocus()
        super.finish()
    }
}
