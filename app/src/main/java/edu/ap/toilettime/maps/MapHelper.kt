package edu.ap.toilettime.maps

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import edu.ap.toilettime.R
import edu.ap.toilettime.activities.MainActivity
import edu.ap.toilettime.activities.ToiletDetailActivity
import edu.ap.toilettime.model.Toilet
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.bonuspack.utils.BonusPackHelper
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File


class MapHelper(packageName: String, cachePath: String, mapView: MapView, val activity: AppCompatActivity, private val hasLocationPermission: Boolean) {

    private var mMapView: MapView? = null
    private var mapController: IMapController? = null
    var mMyLocationOverlay: MyLocationNewOverlay? = null

    init {
        //Setup OSM basics
        val osmConfig = Configuration.getInstance()
        osmConfig.userAgentValue = packageName
        val basePath = File(cachePath, "osmdroid")
        osmConfig.osmdroidBasePath = basePath
        val tileCache = File(osmConfig.osmdroidBasePath, "tile")
        osmConfig.osmdroidTileCache = tileCache
        mMapView = mapView
        mapController = mMapView?.controller

        //Setup map
        mMapView!!.setMultiTouchControls(true)
        mMapView!!.setBuiltInZoomControls(false)

        mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(activity.applicationContext), mMapView)

        if (hasLocationPermission){
            mMyLocationOverlay!!.enableMyLocation()
        }else{
            MainActivity.currentLat = 51.23020595
            MainActivity.currentLong = 4.41655480828479
        }
    }

    fun initMap(location: GeoPoint?, toilets: ArrayList<Toilet>, zoom: Double, addingToilet: Boolean) {
        Handler(Looper.getMainLooper()).post {

            mMapView?.setTileSource(TileSourceFactory.MAPNIK)

            if (!addingToilet){
                for(toilet: Toilet in toilets){
                    addMarker(
                        toilet,
                        GeoPoint(toilet.latitude, toilet.longitude),
                        "${toilet.street} ${toilet.houseNr}, ${toilet.districtCode} ${toilet.district}",
                        R.mipmap.icon_toilet_map_larger
                    )
                }
            }else{
                addMarker(
                    null,
                    location!!,
                    "",
                    R.mipmap.icon_toilet_map_larger
                )
            }

            mapController!!.setZoom(zoom)

            if (location != null){
                setCenter(location)

                Log.d("MAP", "Setting center on ${location.latitude}, ${location.longitude}")
                mMapView!!.overlays.add(mMyLocationOverlay)
                return@post
            }

            if (hasLocationPermission){

                //Set custom icon
                val icon = BitmapFactory.decodeResource(activity.applicationContext.resources, R.mipmap.icon_location_foreground)
                mMyLocationOverlay!!.setPersonIcon(icon)
                mMyLocationOverlay!!.setDirectionIcon(icon)
                mMyLocationOverlay!!.setPersonAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                mMyLocationOverlay!!.setDirectionAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

                //Animate to own location
                mMyLocationOverlay!!.runOnFirstFix{
                    Handler(Looper.getMainLooper()).post {
                        Log.d("MAP", "Animating to own location: ${mMyLocationOverlay!!.myLocation}")
                        mapController!!.animateTo(mMyLocationOverlay!!.myLocation)

                        //set current location
                        MainActivity.currentLat = mMyLocationOverlay!!.myLocation.latitude
                        MainActivity.currentLong = mMyLocationOverlay!!.myLocation.longitude

                        mMapView!!.overlays.add(mMyLocationOverlay)
                    }
                }

            }else{
                setCenter(GeoPoint(MainActivity.currentLat, MainActivity.currentLong))
            }
        }
    }

    fun addMarker(toilet: Toilet?, geoPoint: GeoPoint, name: String, @DrawableRes icon: Int) {
        Handler(Looper.getMainLooper()).post {
            val marker = Marker(mMapView)

            marker.position = geoPoint
            marker.title = name
            marker.icon = ContextCompat.getDrawable(activity.applicationContext, icon)
            marker.setInfoWindow(null)

            if (activity is MainActivity){
                marker.setOnMarkerClickListener { _, _ ->

                    if (toilet != null){
                        val toiletDetailIntent = Intent(activity.applicationContext, ToiletDetailActivity::class.java)
                        toiletDetailIntent.putExtra(Toilet.TOILET, Gson().toJson(toilet))
                        activity.toiletDetailResultLauncher.launch(toiletDetailIntent)
                    }

                    return@setOnMarkerClickListener true
                }
            }

            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            mMapView?.overlays?.add(marker)
            mMapView?.invalidate()
        }
    }

    fun clearMarkers(){
        mMapView?.overlays?.forEach { (it as? Marker)?.remove(mMapView) }
    }

    fun setCenter(geoPoint: GeoPoint) {
        mapController?.setCenter(geoPoint)
    }

    fun onPause() {
        mMapView?.onPause()
    }

    fun onResume() {
        mMapView?.onResume()
    }

    fun getMapView(): MapView? {
        return mMapView
    }
}