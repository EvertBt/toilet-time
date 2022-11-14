package edu.ap.toilettime.maps

import android.content.Context
import android.content.Intent
import android.view.MotionEvent
import com.google.gson.Gson
import edu.ap.toilettime.activities.ToiletDetailActivity
import edu.ap.toilettime.model.Toilet
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


class ToiletMarker(mapView: MapView?, var toilet: Toilet, var ctx: Context) : Marker(mapView) {

    override fun onSingleTapUp(e: MotionEvent?, mapView: MapView?): Boolean {

        //open detail view
        val toiletDetailIntent = Intent(ctx, ToiletDetailActivity::class.java)
        toiletDetailIntent.putExtra(Toilet.TOILET, Gson().toJson(toilet))
        ctx.startActivity(toiletDetailIntent)

        return super.onSingleTapUp(e, mapView)
    }
}