package co.gov.cnsc.mobile.simo.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.models.Tests
//import android.R



class TestsAdapter(private var activity: Activity, private var items: ArrayList<Tests> ): BaseAdapter() {

    private class ViewHolder(row: View?) {
        var txtId: TextView? = null
        var txtComment: TextView? = null
        var txtPlace: TextView? = null

        init {
            this.txtId = row?.findViewById<TextView>(R.id.id_txt)
            this.txtComment = row?.findViewById<TextView>(R.id.descripcion_txt)
            this.txtPlace = row?.findViewById<TextView>(R.id.place_txt)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View?
        val viewHolder: ViewHolder

        if(convertView == null){
            val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view         = inflater.inflate(R.layout.item_view_list_tests, null)
            viewHolder   = ViewHolder(view)
            view?.tag    = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        var testData = items[position];
        viewHolder?.txtId!!.text = testData.id
        viewHolder?.txtComment!!.text = testData.descripcion
        viewHolder?.txtPlace!!.text = "Lugar: " + testData.nombreMcipio + " | " + testData.nombreDepto

        return view as View
    }

    override fun getItem(position: Int): Tests {
        return items[position]
    }

    /*fun getTestId(position: Int) : String {
        return "ok"
    }*/

    /*fun getSubscriptionId(position: Int): String {
        return if (items != null) {
            items.get(position).id
        } else {
            "0"
        }
    }*/

    override fun getItemId(position: Int): Long {
        //return position.toLong()
        return items[position].id.toLong()
    }

    override fun getCount(): Int {
        return items.size
    }
}