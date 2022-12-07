package edu.ap.toilettime.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.ap.toilettime.Adapters.ToiletAdapter
import edu.ap.toilettime.R
import edu.ap.toilettime.database.DatabaseHelper
import edu.ap.toilettime.model.Toilet
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.GeoPoint


class NearbyToiletsActivity : AppCompatActivity() {

    lateinit var roadManager: RoadManager
    lateinit var btnBack : Button
    lateinit var btnClearFilter : Button
    lateinit var btnMaleFilter : MaterialButton
    var btnMaleFilterActive : Boolean = false
    lateinit var btnFemaleFilter : MaterialButton
    var btnFemaleFilterActive : Boolean = false
    lateinit var btnWheelchairFilter : MaterialButton
    var btnWheelchairFilterActive : Boolean = false
    lateinit var btnChangingTableFilter : MaterialButton
    var btnChangingTableFilterActive : Boolean = false

    lateinit var lvToilets : ListView
    lateinit var toiletAdapter : Adapter

    var toiletList: ArrayList<Toilet> = ArrayList()
    var toiletFilterList: ArrayList<Toilet> = ArrayList()

    var currentLat: Double = 0.0
    var currentLong: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //setup road manager
        roadManager = OSRMRoadManager(this, WebView(this).settings.userAgentString)

        setContentView(R.layout.activity_nearby_toilets)

        currentLat = intent.getDoubleExtra("lat", 0.0)
        currentLong = intent.getDoubleExtra("long", 0.0)

        btnBack = findViewById(R.id.btnBackNearbyToilets)

        btnClearFilter = findViewById(R.id.btnClearFilters)

        btnMaleFilter = findViewById(R.id.btnMaleFilter)
        btnFemaleFilter = findViewById(R.id.btnFemaleFilter)
        btnWheelchairFilter = findViewById(R.id.btnWheelchairFilter)
        btnChangingTableFilter = findViewById(R.id.btnChangingTableFilter)

        lvToilets = findViewById(R.id.lvToilets)

        lvToilets.onItemClickListener = AdapterView.OnItemClickListener {
            parent, view, position, id ->
            val selectedToilet : Toilet = parent.getItemAtPosition(position) as Toilet

            intent = Intent()
            // intent.putExtra() return needed data to add new toilet
            intent.putExtra("lat", selectedToilet.latitude)
            intent.putExtra("long", selectedToilet.longitude)

            setResult(RESULT_OK, intent)
            finish()
        }

        btnBack.setOnClickListener {
            intent = Intent()
            intent.putExtra("lat", currentLat)
            intent.putExtra("long", currentLong)
            this.finish()
        }

        btnClearFilter.setOnClickListener {
            clearFilters()
        }

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

        //loadToiletData()

        toiletList = Gson().fromJson(intent.extras?.get(Toilet.TOILET).toString(), object : TypeToken<List<Toilet>>() {}.type)

        Thread{
            runOnUiThread{
                checkFilters()
                updateFilterList()
                toiletAdapter = ToiletAdapter(this, toiletFilterList)
                lvToilets.adapter = toiletAdapter as ToiletAdapter
            }
        }.start()
    }

    fun loadToiletData(){
        Thread{
            toiletList = DatabaseHelper(this@NearbyToiletsActivity).getAllToilets()

            runOnUiThread{
                checkFilters()
                updateFilterList()

                toiletAdapter = ToiletAdapter(this, toiletFilterList)
                lvToilets.adapter = toiletAdapter as ToiletAdapter

                Thread{
                    calculateDistances()
                }.start()
            }
        }.start()
    }

    private fun checkFilters(){
        btnMaleFilterActive = intent.getBooleanExtra("MALE-FILTER", false)
        if(btnMaleFilterActive){
            btnMaleFilter.icon.setTint(getColor(R.color.white))
        }
        btnFemaleFilterActive = intent.getBooleanExtra("FEMALE-FILTER", false)
        if(btnFemaleFilterActive){
            btnFemaleFilter.icon.setTint(getColor(R.color.white))
        }
        btnWheelchairFilterActive = intent.getBooleanExtra("WHEELCHAIR-FILTER", false)
        if(btnWheelchairFilterActive){
            btnWheelchairFilter.icon.setTint(getColor(R.color.white))
        }
        btnChangingTableFilterActive = intent.getBooleanExtra("CHANGING-TABLE-FILTER", false)
        if(btnChangingTableFilterActive){
            btnChangingTableFilter.icon.setTint(getColor(R.color.white))
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
        // update adapter
        toiletAdapter = ToiletAdapter(this, toiletFilterList)
        lvToilets.adapter = toiletAdapter as ToiletAdapter
    }

    private fun calculateDistances(){

        for (toilet in toiletFilterList){
            val waypoints: ArrayList<GeoPoint> = ArrayList()
            waypoints.add(GeoPoint(currentLat, currentLong))
            waypoints.add(GeoPoint(toilet.latitude,toilet.longitude))

            val road = roadManager.getRoad(waypoints)
            toilet.distance = road.mLength
        }

        toiletList = ArrayList(toiletList.sortedWith(compareBy { it.distance }))

        runOnUiThread{
            updateFilterList()
        }
    }

    override fun finish() {
        //intent = Intent()
        // intent.putExtra() return needed data to add new toilet
        intent.putExtra("MALE-FILTER", btnMaleFilterActive)
        intent.putExtra("FEMALE-FILTER", btnFemaleFilterActive)
        intent.putExtra("WHEELCHAIR-FILTER", btnWheelchairFilterActive)
        intent.putExtra("CHANGING-TABLE-FILTER", btnChangingTableFilterActive)

        setResult(RESULT_OK, intent)
        super.finish()
    }

    companion object{
        const val EXTRA_TOILETLIST = "toilets"

        /*fun nearbyToiletIntent(context: Context, toilets: ArrayList<Toilet>?): Intent{
            val toiletsIntent = Intent(context, toilets)
        }*/
        fun nearbyToiletIntent(context: Context): Intent{
            val toiletsIntent = Intent(context, NearbyToiletsActivity::class.java)
            return toiletsIntent
        }
    }
}