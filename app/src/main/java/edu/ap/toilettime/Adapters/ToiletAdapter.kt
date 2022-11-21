package edu.ap.toilettime.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
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

        val tvAdress = rowView.findViewById(R.id.tvAdress) as TextView

        val tvDistance = rowView.findViewById(R.id.tvDistance) as TextView

        val icMenAccesible = rowView.findViewById(R.id.icMenAccesible) as ImageView
        val icWomenAccesible = rowView.findViewById(R.id.icWomenAccesible) as ImageView
        val icWheelchairAccesible = rowView.findViewById(R.id.icWheelchairAccesible) as ImageView
        val icChangingTable = rowView.findViewById(R.id.icChangingTable) as ImageView

        val toilet = getItem(position) as Toilet

        tvAdress.text = "${toilet.street} ${toilet.houseNr}, ${toilet.districtCode} ${toilet.district}"
        tvDistance.text = toilet.id //change to distance later

        if(!toilet.menAccessible){
            icMenAccesible.setColorFilter(context.resources.getColor(com.google.android.material.R.color.material_grey_300))
        }
        if(!toilet.womenAccessible){
            icWomenAccesible.setColorFilter(context.resources.getColor(com.google.android.material.R.color.material_grey_300))
        }
        if(!toilet.wheelchairAccessible){
            icWheelchairAccesible.setColorFilter(context.resources.getColor(com.google.android.material.R.color.material_grey_300))
        }
        if(!toilet.changingTable){
            icChangingTable.setColorFilter(context.resources.getColor(com.google.android.material.R.color.material_grey_300))
        }

        return rowView
    }
}