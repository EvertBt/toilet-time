package edu.ap.toilettime.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import edu.ap.toilettime.R
import edu.ap.toilettime.api.APIHelper
import edu.ap.toilettime.database.DatabaseHelper
import edu.ap.toilettime.maps.MapHelper
import edu.ap.toilettime.model.Toilet
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent.setEventListener
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.GeoPoint


class MainActivity : AppCompatActivity() {
    lateinit var roadManager: RoadManager
    lateinit var btnAddToilet : Button
    lateinit var btnNearbyToilets : Button
    lateinit var btnRefresh : Button
    lateinit var svSearchAddress : SearchView
    lateinit var mapHelper: MapHelper
    lateinit var toiletDetailResultLauncher: ActivityResultLauncher<Intent>
    private var toiletList: ArrayList<Toilet> = ArrayList()
    var toiletFilterList: ArrayList<Toilet> = ArrayList()
    private var lastLocation: GeoPoint? = null

    lateinit var btnMaleFilter : MaterialButton
    var btnMaleFilterActive : Boolean = false
    lateinit var btnFemaleFilter : MaterialButton
    var btnFemaleFilterActive : Boolean = false
    lateinit var btnWheelchairFilter : MaterialButton
    var btnWheelchairFilterActive : Boolean = false
    lateinit var btnChangingTableFilter : MaterialButton
    var btnChangingTableFilterActive : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //setup road manager
        roadManager = OSRMRoadManager(this, WebView(this).settings.userAgentString)

        //OSM Setup
        StrictMode.setThreadPolicy(ThreadPolicy.Builder().permitAll().build())

        //set locationPermission in companion object
        locationPermission = hasPermissions()

        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.primary_background_dark)
        }

        //Get views
        setContentView(R.layout.activity_main)
        btnAddToilet = findViewById(R.id.btnBackToiletDetail)
        btnNearbyToilets = findViewById(R.id.btnNearbyToilets)
        btnRefresh = findViewById(R.id.btnRefresh)
        svSearchAddress = findViewById(R.id.svMainSearch)

        btnMaleFilter = findViewById(R.id.btnMaleFilter)
        btnMaleFilterActive = false
        btnFemaleFilter = findViewById(R.id.btnFemaleFilter)
        btnFemaleFilterActive = false
        btnWheelchairFilter = findViewById(R.id.btnWheelchairFilter)
        btnWheelchairFilterActive = false
        btnChangingTableFilter = findViewById(R.id.btnChangingTableFilter)
        btnChangingTableFilterActive = false

        //Setup OSM
        mapHelper = MapHelper(packageName, cacheDir.absolutePath, findViewById(R.id.mapview), this@MainActivity)

        //Setup listeners
        setupListeners()

        //Load toilets from database
        loadToiletData()

        //Load last location from intent
        val lat: Double? = intent.extras?.getDouble("lat")
        val long: Double? = intent.extras?.getDouble("long")

        if (lat != null && long != null){
            Log.d("MAIN", "Found last location: $lat $long")
            lastLocation = GeoPoint(lat, long)
        }

        if (hasPermissions()) {
            mapHelper.initMap(true, lastLocation, toiletList, 19.0, false)
        }else{
            requestPermission()
        }
    }

    private fun requestPermission(){
        when {
            hasPermissions()-> {
                // Permission is granted
            }
            (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            and (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
            and (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))-> {

                // Aditional rationale should be displayed
                requestPermissions()
            }
            else -> {
                // Permission has not been asked yet
                requestPermissions()
            }
        }
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions(){
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ), 100)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (hasPermissions()) {
                Log.d("MAP", "Initializing map from onRequestPermissionsResult, has permissions")
                mapHelper.initMap(true, lastLocation, toiletList, 19.0, false)
            } else {
                Log.d("MAP", "Initializing map from onRequestPermissionsResult, no permissions")
                mapHelper.initMap(false, lastLocation, toiletList, 19.0, false)
            }
        }
    }

    fun loadToiletData(){
        Thread{
            toiletList = DatabaseHelper(this@MainActivity).getAllToilets()

            if (toiletList.isNotEmpty()) {
                mapHelper.initMap(hasPermissions(), lastLocation, toiletList, 19.0, false)
            }

            calculateDistances()
        }.start()
    }

    private fun calculateDistances(){

        for (toilet in toiletList){
            val waypoints: ArrayList<GeoPoint> = ArrayList()
            waypoints.add(GeoPoint(currentLat, currentLong))
            waypoints.add(GeoPoint(toilet.latitude,toilet.longitude))

            val road = roadManager.getRoad(waypoints)
            toilet.distance = road.mLength
        }

        toiletList = ArrayList(toiletList.sortedWith(compareBy { it.distance }))
    }

    private fun searchLocation(address: String){

        //Force close keyboard
        svSearchAddress.clearFocus()
        val view: View? = findViewById(android.R.id.content)
        if (view != null) {
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        if (address != ""){
            //Search for location
            APIHelper().searchLocation(address, this, mapHelper)
        }
    }

    private fun setupListeners(){

        val addToiletResultLauncher = createAddToiletResultLauncher()
        val nearbyToiletsResultLauncher = createNearbyToiletsResultLauncher()
        toiletDetailResultLauncher = createToiletDetailResultLauncher()

        btnAddToilet.setOnClickListener {
            clickBTNAddToilet(addToiletResultLauncher)
        }

        btnNearbyToilets.setOnClickListener {
            clickBTNNearbyToilets(nearbyToiletsResultLauncher)
        }

        btnRefresh.setOnClickListener {
            lastLocation = null
            loadToiletData()
            clearFilters()
        }

        svSearchAddress.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchLocation(query)
                return true
            }
            override fun onQueryTextChange(p0: String?): Boolean {return false}
        })

        setEventListener(this, KeyboardVisibilityEventListener {
            if (!it) svSearchAddress.clearFocus()
        })

        btnMaleFilter.setOnClickListener {
            switchMaleFilter()
        }

        btnFemaleFilter.setOnClickListener {
            switchFemaleFilter()
        }

        btnWheelchairFilter.setOnClickListener {
            switchWheelchairFilter()
        }

        btnChangingTableFilter.setOnClickListener {
            switchChangingTableFilter()
        }
    }

    private fun clearFilters(){
        if(btnMaleFilterActive){
            switchMaleFilter()
        }
        if(btnFemaleFilterActive){
            switchFemaleFilter()
        }
        if(btnWheelchairFilterActive){
            switchWheelchairFilter()
        }
        if(btnChangingTableFilterActive){
            switchChangingTableFilter()
        }
    }

    private fun switchMaleFilter(){
        if(btnMaleFilterActive){
            //Disable filter
            btnMaleFilter.icon.setTint(getColor(R.color.grayed_out_tint))
            btnMaleFilterActive = false
        }
        else {
            //Enable filter
            btnMaleFilter.icon.setTint(getColor(R.color.white))
            btnMaleFilterActive = true
        }
        updateFilterList()
    }

    private fun switchFemaleFilter(){
        if(btnFemaleFilterActive){
            //Disable filter
            btnFemaleFilter.icon.setTint(getColor(R.color.grayed_out_tint))
            btnFemaleFilterActive = false
        }
        else {
            //Enable filter
            btnFemaleFilter.icon.setTint(getColor(R.color.white))
            btnFemaleFilterActive = true
        }
        updateFilterList()
    }

    private fun switchWheelchairFilter(){
        if(btnWheelchairFilterActive){
            //Disable filter
            btnWheelchairFilter.icon.setTint(getColor(R.color.grayed_out_tint))
            btnWheelchairFilterActive = false
        }
        else {
            //Enable filter
            btnWheelchairFilter.icon.setTint(getColor(R.color.white))
            btnWheelchairFilterActive = true
        }
        updateFilterList()
    }

    private fun switchChangingTableFilter(){
        if(btnChangingTableFilterActive){
            //Disable filter
            btnChangingTableFilter.icon.setTint(getColor(R.color.grayed_out_tint))
            btnChangingTableFilterActive = false
        }
        else {
            //Enable filter
            btnChangingTableFilter.icon.setTint(getColor(R.color.white))
            btnChangingTableFilterActive = true
        }
        updateFilterList()
    }

    private fun updateFilterList(){
        toiletFilterList.clear()
        toiletFilterList.addAll(toiletList);
        // all filters grayed out in beginning
        // filters combine result
        // eg. only show female AND wheelchair when those filters are active
        for (toilet in toiletList){
            if (btnMaleFilterActive and !toilet.menAccessible){
                toiletFilterList.remove(toilet)
            }
            if (btnFemaleFilterActive and !toilet.womenAccessible){
                toiletFilterList.remove(toilet)
            }
            if (btnWheelchairFilterActive and !toilet.wheelchairAccessible){
                toiletFilterList.remove(toilet)
            }
            if (btnChangingTableFilterActive and !toilet.changingTable){
                toiletFilterList.remove(toilet)
            }
        }
        // redraw toilet icons on map
        mapHelper.clearMarkers()
        for (toilet in toiletFilterList){
            Log.d("addmarkers when updating filterlist",toilet.addedBy)
            mapHelper.addMarker(toilet, GeoPoint(toilet.latitude, toilet.longitude), "", R.mipmap.icon_toilet_map_larger)
        }
    }

    private fun createAddToiletResultLauncher(): ActivityResultLauncher<Intent> {
        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == 201) {
                    val data = result.data

                    if (data != null && data.extras != null){

                        lastLocation = Gson().fromJson(data.extras?.getString(Toilet.LOCATION), GeoPoint::class.java)
                        Log.d("APP", "Last loc: ${lastLocation}")
                        val toilet: Toilet = Gson().fromJson(data.extras?.getString(Toilet.TOILET), Toilet::class.java)

                        //Update database
                        Thread{
                            DatabaseHelper(this@MainActivity).addToilet(toilet)
                        }.start()
                    }else{
                        Log.e("ADD", "Data extras was null")
                    }
                }
            }
        return resultLauncher
    }

    private fun clickBTNAddToilet(resultLauncher : ActivityResultLauncher<Intent>){

        if (!isNetworkAvailable()){
            Toast.makeText(this, "Deze functie is alleen beschikbaar met een internetverbinding", Toast.LENGTH_LONG).show()
            return
        }

        val addToiletActivityIntent = Intent(this, AddToiletActivity::class.java)
        addToiletActivityIntent.putExtra(Toilet.LOCATION, Gson().toJson(GeoPoint(mapHelper.getMapView()!!.mapCenter.latitude, mapHelper.getMapView()!!.mapCenter.longitude)))

        resultLauncher.launch(addToiletActivityIntent)
    }

    private fun createNearbyToiletsResultLauncher(): ActivityResultLauncher<Intent> {
        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    val extras = data?.extras
                    if (extras != null) {
                        // code to do when coming back from intent
                        val lat: Double = extras.getDouble("lat")
                        val long: Double = extras.getDouble("long")

                        val location = GeoPoint(lat, long)

                        btnMaleFilterActive = extras.getBoolean("MALE-FILTER", false)
                        btnFemaleFilterActive = extras.getBoolean("FEMALE-FILTER", false)
                        btnWheelchairFilterActive = extras.getBoolean("WHEELCHAIR-FILTER", false)
                        btnChangingTableFilterActive = extras.getBoolean("CHANGING-TABLE-FILTER", false)

                        checkFilters(btnMaleFilterActive, btnFemaleFilterActive, btnWheelchairFilterActive, btnChangingTableFilterActive)

                        mapHelper.initMap(hasPermissions(), location, toiletFilterList, 19.0, false)
                    }
                }
            }
        return resultLauncher
    }

    private fun clickBTNNearbyToilets(resultLauncher : ActivityResultLauncher<Intent>){
        val nearbyToiletsIntent = NearbyToiletsActivity.nearbyToiletIntent(this)
        //AddToiletIntent.putExtra() add all needed extras to add a new toilet

        if(hasPermissions()){
            nearbyToiletsIntent.putExtra("lat", mapHelper.mMyLocationOverlay!!.myLocation.latitude)
            nearbyToiletsIntent.putExtra("long", mapHelper.mMyLocationOverlay!!.myLocation.longitude)
        }else{
            nearbyToiletsIntent.putExtra("lat", currentLat)
            nearbyToiletsIntent.putExtra("long", currentLong)
        }

        nearbyToiletsIntent.putExtra("MALE-FILTER", btnMaleFilterActive)
        nearbyToiletsIntent.putExtra("FEMALE-FILTER", btnFemaleFilterActive)
        nearbyToiletsIntent.putExtra("WHEELCHAIR-FILTER", btnWheelchairFilterActive)
        nearbyToiletsIntent.putExtra("CHANGING-TABLE-FILTER", btnChangingTableFilterActive)
        nearbyToiletsIntent.putExtra(Toilet.TOILET, Gson().toJson(toiletList))

        resultLauncher.launch(nearbyToiletsIntent)
    }

    private fun checkFilters(maleFilter: Boolean, femaleFilter: Boolean, wheelchairFilter: Boolean, changingTableFilter: Boolean){
        if(maleFilter){
            btnMaleFilter.icon.setTint(getColor(R.color.white))
        }
        else{
            btnMaleFilter.icon.setTint(getColor(R.color.grayed_out_tint))
        }
        if(femaleFilter){
            btnFemaleFilter.icon.setTint(getColor(R.color.white))
        }
        else{
            btnFemaleFilter.icon.setTint(getColor(R.color.grayed_out_tint))
        }
        if(wheelchairFilter){
            btnWheelchairFilter.icon.setTint(getColor(R.color.white))
        }
        else{
            btnWheelchairFilter.icon.setTint(getColor(R.color.grayed_out_tint))
        }
        if(changingTableFilter){
            btnChangingTableFilter.icon.setTint(getColor(R.color.white))
        }
        else{
            btnChangingTableFilter.icon.setTint(getColor(R.color.grayed_out_tint))
        }

        updateFilterList()
    }

    private fun createToiletDetailResultLauncher(): ActivityResultLauncher<Intent> {
        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == 201) {
                    loadToiletData() //Reload toilet data after user reported a toilet
                }
            }
        return resultLauncher
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    override fun onPause() {
        super.onPause()
        mapHelper.getMapView()?.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapHelper.getMapView()?.onResume()
    }
    companion object{
        var locationPermission: Boolean = false
        var currentLat: Double = 0.0
        var currentLong: Double = 0.0
    }
}

