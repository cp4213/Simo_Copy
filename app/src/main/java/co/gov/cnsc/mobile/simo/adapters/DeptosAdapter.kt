package co.gov.cnsc.mobile.simo.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import co.gov.cnsc.mobile.simo.R
import co.gov.cnsc.mobile.simo.models.Deptos

class DeptosAdapter(private var activity: Activity, private var items: ArrayList<Deptos> ): BaseAdapter() {

    private class ViewHolder(row: View?) {
        var txtId: TextView? = null
        var txtName: TextView? = null

        init {
            //this.txtId = row?.findViewById<TextView>(R.id.id_txt)
            this.txtName = row?.findViewById<TextView>(R.id.depto_txt)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View?
        val viewHolder: ViewHolder

        if(convertView == null){
            val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view         = inflater.inflate(R.layout.spinner_item_dropdown_dptos, null)
            viewHolder   = ViewHolder(view)
            view?.tag    = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        var deptoData = items[position];
        //viewHolder?.txtId!!.text = deptoData.id
        viewHolder?.txtName!!.text = deptoData.nombre

        return view as View;
    }

    override fun getItem(position: Int): Deptos {
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