package com.spaqin.finecompanion.Communications

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.spaqin.finecompanion.R
import kotlinx.android.synthetic.main.item_fineset.view.*
import org.w3c.dom.Text
import java.text.SimpleDateFormat

/**
 * Created by Spaqin on 2018-02-05.
 */
class FineSetAdapter(context: Context,  val list: ArrayList<FineSet>)
    : ArrayAdapter<FineSet>(context, 0, list) {

    val vi: LayoutInflater by lazy { context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        var vh: ViewHolder?
        val fineset: FineSet = getItem(position)
        if (convertView == null) {
            convertView = vi.inflate(R.layout.item_fineset, null)
            val sensor: TextView = convertView!!.findViewById(R.id.sens) as TextView
            val value: TextView = convertView.findViewById(R.id.value) as TextView
            val longlat: TextView = convertView.findViewById(R.id.loc) as TextView
            val datetv: TextView = convertView.findViewById(R.id.time) as TextView
            vh = ViewHolder(sensor, value, longlat, datetv)
            convertView.tag = vh
        }
        else
            vh = convertView.tag as ViewHolder

        vh.sensor.text = fineset.sensorType.toString()
        vh.value.text = fineset.sensorValue.toString() + fineset.sensorType.unit
        vh.longlat.text = "Long: " + fineset.lon + " lat: " + fineset.lat
        val sdf = SimpleDateFormat("HH:mm:ss")
        vh.datetv.text = sdf.format(fineset.datetime?.time)
        return convertView
    }

    class ViewHolder(val sensor: TextView, val value: TextView, val longlat: TextView, val datetv: TextView)
}