package edu.ap.toilettime.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.google.gson.Gson
import edu.ap.toilettime.R
import edu.ap.toilettime.database.ToiletFirebaseRepository
import edu.ap.toilettime.database.room.ToiletDatabase
import edu.ap.toilettime.model.Toilet
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File
import java.io.IOException
import java.net.URL


class MainActivity : AppCompatActivity() {
    private var mMapView: MapView? = null
    private var mapController: IMapController? = null
    private var mMyLocationOverlay: MyLocationNewOverlay? = null
    lateinit var btnAddToilet : Button
    lateinit var btnNearbyToilets : Button
    lateinit var btnRefresh : Button
    lateinit var btnSearch : Button
    lateinit var txtAdress : EditText
    var toiletList: ArrayList<Toilet>? = ArrayList()
    private val urlNominatim = "https://nominatim.openstreetmap.org/search.php?q="
    private var lastLocation: GeoPoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setup OSM
        val osmConfig = Configuration.getInstance()
        osmConfig.userAgentValue = packageName
        val basePath = File(cacheDir.absolutePath, "osmdroid")
        osmConfig.osmdroidBasePath = basePath
        val tileCache = File(osmConfig.osmdroidBasePath, "tile")
        osmConfig.osmdroidTileCache = tileCache
        setContentView(R.layout.activity_main)

        //Get views
        btnAddToilet = findViewById(R.id.btnBackNearbyToilets)
        btnNearbyToilets = findViewById(R.id.btnNearbyToilets)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnSearch = findViewById(R.id.btnSearch)
        txtAdress = findViewById(R.id.txtAdress)
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

        btnRefresh.setOnClickListener {
            loadToiletsAsync()
        }

        btnSearch.setOnClickListener {
            txtAdress.clearFocus()
            searchLocation(txtAdress.text.toString())
        }

        loadToiletsAsync()

        //Load last location from intent
        val lat: Double? = intent.extras?.getDouble("lat")
        val long: Double? = intent.extras?.getDouble("long")

        if (lat != null && long != null){
            Log.d("MAIN", "Found last location: ${lat} ${long}")
            lastLocation = GeoPoint(lat, long)
        }

        if (hasPermissions()) {
            initMap(true, lastLocation)
        }
        else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION), 100)
        }
    }

    private fun loadToiletsAsync(){
        if (toiletList!!.isEmpty()){
            Thread{
                //Initialize local database
                val localDb = Room.databaseBuilder(
                    applicationContext,
                    ToiletDatabase::class.java, "toilet-database"
                ).build()

                val toiletDao = localDb.toiletDao()
                toiletList?.addAll(toiletDao.getAll())

                //Initialize firebase database
                val db = ToiletFirebaseRepository()
                db.allToilets(true)?.let {
                    if (toiletList!!.isEmpty()){
                        toiletDao.insertAll(it)
                    }
                    toiletList?.clear()
                    toiletList?.addAll(it)
                }

                if (toiletList != null) {
                    runOnUiThread {
                        initMap(hasPermissions(), lastLocation)
                    }
                }
            }.start()
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
                initMap(true, lastLocation)
            } else {
                initMap(false, lastLocation)
            }
        }
    }

    private fun initMap(hasLocationPermission: Boolean, location: GeoPoint?) {
        mMapView?.setTileSource(TileSourceFactory.MAPNIK)

        // create a static ItemizedOverlay showing some markers
        val toilets = ToiletFirebaseRepository().allToilets(false)

        if (toilets != null){
            for(toilet: Toilet in toilets){
                addMarker(
                    toilet,
                    GeoPoint(toilet.latitude, toilet.longitude), "${toilet.street} ${toilet.houseNr}, ${toilet.districtCode} ${toilet.district}",
                    android.R.drawable.btn_star_big_on
                )
            }
        }

        mapController!!.setZoom(19.0)

        if (location != null){
            setCenter(location, "")
            return
        }else{
            Log.d("MAIN", "Location is null at map init")
        }

        if (hasLocationPermission){
            mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mMapView)
            mMyLocationOverlay!!.enableMyLocation()
            mMyLocationOverlay!!.runOnFirstFix{runOnUiThread {
                mapController!!.animateTo(mMyLocationOverlay!!.myLocation)
            }}
            mMapView!!.overlays.add(mMyLocationOverlay)
        }else{
            setCenter(GeoPoint(51.23020595, 4.41655480828479), "Campus Ellermanstraat")
        }
    }

    private fun addMarker(toilet: Toilet?, geoPoint: GeoPoint, name: String, @DrawableRes icon: Int) {
        var marker = Marker(mMapView)
        //if (toilet != null) marker = ToiletMarker(mMapView, toilet, this)

        marker.position = geoPoint
        marker.title = name
        marker.icon = ContextCompat.getDrawable(this@MainActivity, icon)
        marker.setInfoWindow(null)

        marker.setOnMarkerClickListener { _, _ ->

            Log.d("MARKER", "Tapped on marker with street ${toilet?.street}")
            val toiletDetailIntent = Intent(this, ToiletDetailActivity::class.java)
            toiletDetailIntent.putExtra(Toilet.TOILET, Gson().toJson(toilet))
            startActivity(toiletDetailIntent)

            return@setOnMarkerClickListener true
        }

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

    private fun searchLocation(address: String){

        //Force close keyboard
        val view: View? = findViewById(android.R.id.content)
        if (view != null) {
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        //Get address location async from API
        Thread{
            val client = OkHttpClient()
            val request = Request.Builder().url(URL("$urlNominatim$address&format=jsonv2")).build()
            client.newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread{
                        Toast.makeText(applicationContext, "There was a problem trying to get address data, please try again.", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {

                        try{
                            val jObject : JSONObject = JSONArray(response.body!!.string()).getJSONObject(0)
                            val lat : Double = jObject.getString("lat").toDouble()
                            val lon : Double = jObject.getString("lon").toDouble()
                            val name : String = jObject.getString("display_name")

                            val geoPoint = GeoPoint(lat, lon)

                            runOnUiThread{
                                addMarker(null, geoPoint, name, android.R.drawable.btn_plus)
                                setCenter(geoPoint, name)
                            }
                        }catch (_: Exception){
                            runOnUiThread{
                                Toast.makeText(applicationContext, "Address does not exist", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            })
        }.start()
    }

    fun clickBTNToiletDetail(resultLauncher : ActivityResultLauncher<Intent>){
        val toiletDetailIntent = Intent(this, ToiletDetailActivity::class.java)
        //AddToiletIntent.putExtra() add all needed extras to add a new toilet
        resultLauncher.launch(toiletDetailIntent)
    }
}

