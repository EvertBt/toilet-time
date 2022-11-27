package edu.ap.toilettime.maps

import android.content.Context
import android.content.Intent
import android.graphics.drawable.DrawableContainer
import android.os.Handler
import android.os.Looper
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import edu.ap.toilettime.R
import edu.ap.toilettime.activities.MainActivity
import edu.ap.toilettime.activities.ToiletDetailActivity
import edu.ap.toilettime.database.ToiletFirebaseRepository
import edu.ap.toilettime.model.Toilet
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File

class MapHelper(packageName: String, cachePath: String, mapView: MapView, private val activity : MainActivity) {

    private var mMapView: MapView? = null
    private var mapController: IMapController? = null
    private var mMyLocationOverlay: MyLocationNewOverlay? = null

    init {
        //Setup OSM
        val osmConfig = Configuration.getInstance()
        osmConfig.userAgentValue = packageName
        val basePath = File(cachePath, "osmdroid")
        osmConfig.osmdroidBasePath = basePath
        val tileCache = File(osmConfig.osmdroidBasePath, "tile")
        osmConfig.osmdroidTileCache = tileCache
        mMapView = mapView
        mapController = mMapView?.controller
    }

    fun initMap(hasLocationPermission: Boolean, location: GeoPoint?, toilets: ArrayList<Toilet>) {
        Handler(Looper.getMainLooper()).post {

            mMapView?.setTileSource(TileSourceFactory.MAPNIK)

            for(toilet: Toilet in toilets){
                addMarker(
                    toilet,
                    GeoPoint(toilet.latitude, toilet.longitude), "${toilet.street} ${toilet.houseNr}, ${toilet.districtCode} ${toilet.district}",
                    R.mipmap.icon_toilet_map_foreground
                )
            }

            mapController!!.setZoom(19.0)

            if (location != null){
                setCenter(location, "")

                if (hasLocationPermission){
                    mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(activity.applicationContext), mMapView)
                    mMyLocationOverlay!!.enableMyLocation()
                    mMapView!!.overlays.add(mMyLocationOverlay)
                }
                return@post
            }

            if (hasLocationPermission){
                mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(activity.applicationContext), mMapView)
                mMyLocationOverlay!!.enableMyLocation()
                mMyLocationOverlay!!.runOnFirstFix{
                    Handler(Looper.getMainLooper()).post {
                        mapController!!.animateTo(mMyLocationOverlay!!.myLocation)
                    }
                }
                mMapView!!.overlays.add(mMyLocationOverlay)
            }else{
                setCenter(GeoPoint(51.23020595, 4.41655480828479), "Campus Ellermanstraat")
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

            marker.setOnMarkerClickListener { _, _ ->

                if (toilet != null){
                    val toiletDetailIntent = Intent(activity.applicationContext, ToiletDetailActivity::class.java)
                    toiletDetailIntent.putExtra(Toilet.TOILET, Gson().toJson(toilet))
                    activity.toiletDetailResultLauncher.launch(toiletDetailIntent)
                }

                return@setOnMarkerClickListener true
            }

            marker.setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_CENTER)
            mMapView?.overlays?.add(marker)
            mMapView?.invalidate()
        }
    }

    fun setCenter(geoPoint: GeoPoint, name: String) {
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