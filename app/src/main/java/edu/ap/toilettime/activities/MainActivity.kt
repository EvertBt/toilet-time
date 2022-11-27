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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import edu.ap.toilettime.R
import edu.ap.toilettime.api.APIHelper
import edu.ap.toilettime.database.DatabaseHelper
import edu.ap.toilettime.maps.MapHelper
import edu.ap.toilettime.model.Toilet
import org.osmdroid.util.GeoPoint

class MainActivity : AppCompatActivity() {
    lateinit var btnAddToilet : Button
    lateinit var btnNearbyToilets : Button
    lateinit var btnRefresh : Button
    lateinit var btnSearch : Button
    lateinit var txtAddress : EditText
    lateinit var mapHelper: MapHelper
    lateinit var toiletDetailResultLauncher: ActivityResultLauncher<Intent>
    private var toiletList: ArrayList<Toilet> = ArrayList()
    private var lastLocation: GeoPoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Get views
        setContentView(R.layout.activity_main)
        btnAddToilet = findViewById(R.id.btnAddToilet)
        btnNearbyToilets = findViewById(R.id.btnNearbyToilets)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnSearch = findViewById(R.id.btnSearch)
        txtAddress = findViewById(R.id.txtAdress)

        //Setup OSM
        mapHelper = MapHelper(packageName, cacheDir.absolutePath, findViewById(R.id.mapview), this@MainActivity)

        //Setup result launchers
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
            loadToiletData()
        }

        btnSearch.setOnClickListener {
            searchLocation(txtAddress.text.toString())
        }

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
            mapHelper.initMap(true, lastLocation, toiletList)
        }else{
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 100)
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
                mapHelper.initMap(true, lastLocation, toiletList)
            } else {
                mapHelper.initMap(false, lastLocation, toiletList)
            }
        }
    }

    private fun loadToiletData(){
        Thread{
            toiletList = DatabaseHelper(this@MainActivity, null).getAllToilets()

            Log.d("MAIN", "returning ${toiletList.size} toilets from local database")

            if (toiletList.isNotEmpty()) {
                mapHelper.initMap(hasPermissions(), lastLocation, toiletList)
            }
        }.start()
    }

    private fun searchLocation(address: String){

        //Force close keyboard
        txtAddress.clearFocus()
        val view: View? = findViewById(android.R.id.content)
        if (view != null) {
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        if (txtAddress.text.toString() != ""){
            //Search for location
            APIHelper().searchLocation(address, this, mapHelper)
        }
    }

    private fun createAddToiletResultLauncher(): ActivityResultLauncher<Intent> {
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

    private fun clickBTNAddToilet(resultLauncher : ActivityResultLauncher<Intent>){
        val addToiletActivityIntent = Intent(this, AddToiletActivity::class.java)
        resultLauncher.launch(addToiletActivityIntent)
    }

    private fun createNearbyToiletsResultLauncher(): ActivityResultLauncher<Intent> {
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

    private fun clickBTNNearbyToilets(resultLauncher : ActivityResultLauncher<Intent>){
        val nearbyToiletsIntent = NearbyToiletsActivity.nearbyToiletIntent(this)
        //AddToiletIntent.putExtra() add all needed extras to add a new toilet
        resultLauncher.launch(nearbyToiletsIntent)
    }

    private fun createToiletDetailResultLauncher(): ActivityResultLauncher<Intent> {
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

    override fun onPause() {
        super.onPause()
        mapHelper.getMapView()?.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapHelper.getMapView()?.onResume()
    }

    companion object{
        fun onDatabaseUpdate(mainActivity: MainActivity){
            Log.d("DATABASEHELPER", "updating database with new data")
            mainActivity.loadToiletData()
        }
    }
}

