package edu.ap.toilettime.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import edu.ap.toilettime.R
import edu.ap.toilettime.database.ToiletRepository
import edu.ap.toilettime.model.Toilet
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File


class MainActivity : AppCompatActivity() {
    private var mMapView: MapView? = null
    private var mapController: IMapController? = null
    private var mMyLocationOverlay: MyLocationNewOverlay? = null
    lateinit var btnAddToilet : Button
    lateinit var btnNearbyToilets : Button
    lateinit var btnRefresh : Button
    var toiletList: ArrayList<Toilet>? = ArrayList()
    private val urlNominatim = "https://nominatim.openstreetmap.org/search.php?q="

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val osmConfig = Configuration.getInstance()
        osmConfig.userAgentValue = packageName
        val basePath = File(cacheDir.absolutePath, "osmdroid")
        osmConfig.osmdroidBasePath = basePath
        val tileCache = File(osmConfig.osmdroidBasePath, "tile")
        osmConfig.osmdroidTileCache = tileCache
        setContentView(R.layout.activity_main)

        btnAddToilet = findViewById(R.id.btnAddToilet)
        btnNearbyToilets = findViewById(R.id.btnNearbyToilets)
        btnRefresh = findViewById(R.id.btnRefresh)
        mMapView = findViewById(R.id.mapview)
        mapController = mMapView?.controller

        val addToiletResultLauncher = CreateAddToiletResultLauncher()
        val nearbyToiletsResultLauncher = CreateNearbyToiletsResultLauncher()
        val toiletDetailResultLauncher = CreateToiletDetailResultLauncher()

        btnAddToilet.setOnClickListener {
            clickBTNAddToilet(addToiletResultLauncher)
        }

        btnNearbyToilets.setOnClickListener {
            clickBTNNearbyToilets(nearbyToiletsResultLauncher)
        }

        /*BTNToiletDetail.setOnClickListener {
            clickBTNToiletDetail(toiletDetailResultLauncher)
        }*/

        Thread{
            val db = ToiletRepository()
            db.allToilets()?.let { toiletList?.addAll(it) }

            if (toiletList != null) {
                initMap(hasPermissions())
            }
        }.start()

        if (hasPermissions()) {
            initMap(true)
        }
        else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION), 100)
        }
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (hasPermissions()) {
                initMap(true)
            } else {
                initMap(false)
            }
        }
    }

    fun initMap(hasLocationPermission: Boolean) {
        mMapView?.setTileSource(TileSourceFactory.MAPNIK)
        // create a static ItemizedOverlay showing some markers
        var toilets = ToiletRepository().allToilets()
        // add receiver to get location from tap


        // MiniMap
        //val miniMapOverlay = MinimapOverlay(this, mMapView!!.tileRequestCompleteHandler)
        //this.mMapView?.overlays?.add(miniMapOverlay)

        mMapView?.controller?.setZoom(17.0)

        if (hasLocationPermission){
            mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mMapView)
            mMyLocationOverlay!!.enableMyLocation()
            mMapView!!.overlays.add(mMyLocationOverlay)
        }else{
            setCenter(GeoPoint(51.23020595, 4.41655480828479), "Campus Ellermanstraat")
        }
    }

    private fun addMarker(geoPoint: GeoPoint, name: String) {
        val marker = Marker(mMapView)

        marker.position = geoPoint
        marker.title = name
        marker.icon = ContextCompat.getDrawable(this@MainActivity, android.R.drawable.btn_star_big_on)

        marker.setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_CENTER)
        mMapView?.overlays?.add(marker)
        mMapView?.invalidate()
    }

    fun setCenter(geoPoint: GeoPoint, name: String) {
        mapController?.setCenter(geoPoint)
    }

    override fun onPause() {
        super.onPause()
        mMapView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }

    fun CreateAddToiletResultLauncher(): ActivityResultLauncher<Intent> {
        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    val extras = data?.extras
                    if (extras != null) {
                        // code to do when coming back from intent (here you add the new toilet)
                    }
                }
            }
        return resultLauncher
    }

    fun clickBTNAddToilet(resultLauncher : ActivityResultLauncher<Intent>){
        val addToiletActivityIntent = Intent(this, AddToiletActivity::class.java)
        resultLauncher.launch(addToiletActivityIntent)
    }

    fun CreateNearbyToiletsResultLauncher(): ActivityResultLauncher<Intent> {
        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    val extras = data?.extras
                    if (extras != null) {
                        // code to do when coming back from intent (here you add the new toilet)
                    }
                }
            }
        return resultLauncher
    }

    fun clickBTNNearbyToilets(resultLauncher : ActivityResultLauncher<Intent>){
        val nearbyToiletsIntent = NearbyToiletsActivity.nearbyToiletIntent(this)
        //AddToiletIntent.putExtra() add all needed extras to add a new toilet
        resultLauncher.launch(nearbyToiletsIntent)
    }

    fun CreateToiletDetailResultLauncher(): ActivityResultLauncher<Intent> {
        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    val extras = data?.extras
                    if (extras != null) {
                        // code to do when coming back from intent (here you add the new toilet)
                    }
                }
            }
        return resultLauncher
    }

    fun clickBTNToiletDetail(resultLauncher : ActivityResultLauncher<Intent>){
        val toiletDetailIntent = Intent(this, ToiletDetailActivity::class.java)
        //AddToiletIntent.putExtra() add all needed extras to add a new toilet
        resultLauncher.launch(toiletDetailIntent)
    }
}