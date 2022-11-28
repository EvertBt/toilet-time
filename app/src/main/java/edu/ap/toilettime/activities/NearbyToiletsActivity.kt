package edu.ap.toilettime.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import edu.ap.toilettime.Adapters.ToiletAdapter
import edu.ap.toilettime.R
import edu.ap.toilettime.database.DatabaseHelper
import edu.ap.toilettime.database.ToiletFirebaseRepository
import edu.ap.toilettime.model.Toilet


class NearbyToiletsActivity : AppCompatActivity() {
    private var toiletRepository : ToiletFirebaseRepository = ToiletFirebaseRepository()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_toilets)

        btnBack = findViewById(R.id.btnBackNearbyToilets)

        btnClearFilter = findViewById(R.id.btnClearFilters)

        btnMaleFilter = findViewById(R.id.btnMaleFilter)
        btnMaleFilterActive = false
        btnFemaleFilter = findViewById(R.id.btnFemaleFilter)
        btnFemaleFilterActive = false
        btnWheelchairFilter = findViewById(R.id.btnWheelchairFilter)
        btnWheelchairFilterActive = false
        btnChangingTableFilter = findViewById(R.id.btnChangingTableFilter)
        btnChangingTableFilterActive = false


        lvToilets = findViewById(R.id.lvToilets)

        lvToilets.onItemClickListener = AdapterView.OnItemClickListener {
            parent, view, position, id ->  
            val selectedToilet : Toilet = parent.getItemAtPosition(position) as Toilet

            Log.d("lat nearby",selectedToilet.latitude.toString())
            Log.d("long nearby",selectedToilet.longitude.toString())

            intent = Intent()
            // intent.putExtra() return needed data to add new toilet
            intent.putExtra("lat", selectedToilet.latitude)
            intent.putExtra("long", selectedToilet.longitude)
            setResult(RESULT_OK, intent)
            super.finish()
        }

        btnBack.setOnClickListener {
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

        loadToiletData()
    }

    fun loadToiletData(){
        Thread{
            toiletList = DatabaseHelper(this@NearbyToiletsActivity).getAllToilets()

            runOnUiThread{
                updateFilterList()
                toiletAdapter = ToiletAdapter(this, toiletFilterList)
                lvToilets.adapter = toiletAdapter as ToiletAdapter
            }
        }.start()
    }

    //TODO on list item click -
    /*val toiletDetailIntent = Intent(this, MainActivity::class.java)
    toiletDetailIntent.putExtra("lat", toilet.latitude)
    toiletDetailIntent.putExtra("long", toilet.longitude)
    activitylauncher.launch(toiletDetailIntent)*/

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

    override fun finish() : Unit {
        intent = Intent()
        // intent.putExtra() return needed data to add new toilet
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

        fun onDatabaseUpdate(activity: NearbyToiletsActivity){
            activity.loadToiletData()
        }
    }
}