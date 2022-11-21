package edu.ap.toilettime.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import edu.ap.toilettime.R
import edu.ap.toilettime.model.Toilet

class ToiletAdapter(private val context: Context,
                    private val dataSource: ArrayList<Toilet>) : BaseAdapter() {
    private  val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.list_item_toilet, parent, false)

        val TVAdress = rowView.findViewById(R.id.TVAdress) as TextView

        val TVDistance = rowView.findViewById(R.id.TVDistance) as TextView

        val toilet = getItem(position) as Toilet

        TVAdress.text = "${toilet.street} ${toilet.houseNr}, ${toilet.districtCode} ${toilet.district}"
        TVDistance.text = toilet.id //change to distance later

        return rowView
    }
}