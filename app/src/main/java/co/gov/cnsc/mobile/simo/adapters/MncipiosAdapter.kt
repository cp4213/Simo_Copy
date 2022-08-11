package co.gov.cnsc.mobile.simo.adapters

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.models.Mncipios

class MncipiosAdapter(private var activity: Activity, private var items: ArrayList<Mncipios>): BaseAdapter()  {
    private class ViewHolder(row: View?) {
        var txtId: TextView? = null
        var txtNameMncipio: TextView? = null

        init {
            //this.txtId = row?.findViewById<TextView>(R.id.id_txt)
            this.txtNameMncipio = row?.findViewById<TextView>(R.id.textView5)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View?
        val viewHolder: ViewHolder

        if(convertView == null){
            val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view         = inflater.inflate(R.layout.spinner_item_dropdown_mncipios_list, null)
            viewHolder   = ViewHolder(view)
            view?.tag    = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        var mcipioData = items[position];
        viewHolder?.txtNameMncipio?.text = mcipioData.nombre
        return view as View;
    }

    override fun getItem(position: Int): Mncipios {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        //return position.toLong()
        return items[position].id.toLong()
    }

    override fun getCount(): Int {
        return items.size
    }
}