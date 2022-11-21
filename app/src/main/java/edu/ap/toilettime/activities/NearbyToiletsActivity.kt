package edu.ap.toilettime.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Adapter
import android.widget.Button
import android.widget.ListView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_toilets)

        btnBack = findViewById(R.id.btnAddToilet)

        btnClearFilter = findViewById(R.id.btnClearFilters)

        btnMaleFilter = findViewById(R.id.btnMaleFilter)
        btnMaleFilterActive = true
        btnFemaleFilter = findViewById(R.id.btnFemaleFilter)
        btnFemaleFilterActive = true
        btnWheelchairFilter = findViewById(R.id.btnWheelchairFilter)
        btnWheelchairFilterActive = true
        btnChangingTableFilter = findViewById(R.id.btnChangingTableFilter)
        btnChangingTableFilterActive = true

        lvToilets = findViewById(R.id.lvToilets)

        btnBack.setOnClickListener {
            this.finish()
        }

        btnClearFilter.setOnClickListener {
            ClearFilters()
        }

        btnMaleFilter.setOnClickListener {
            SwitchMaleFilter()
        }

        btnFemaleFilter.setOnClickListener {
            SwitchFemaleFilter()
        }

        btnWheelchairFilter.setOnClickListener {
            SwitchWheelchairFilter()
        }

        btnChangingTableFilter.setOnClickListener {
            SwitchChangingTableFilter()
        }

        loadToiletData()
    }

    fun loadToiletData(){
        Thread{
            toiletList = DatabaseHelper(null, this@NearbyToiletsActivity).getAllToilets()

            runOnUiThread{
                toiletAdapter = ToiletAdapter(this, toiletList)
                lvToilets.adapter = toiletAdapter as ToiletAdapter
            }
        }.start()
    }

    //TODO on list item click ->
    /*val toiletDetailIntent = Intent(this, MainActivity::class.java)
    toiletDetailIntent.putExtra("lat", toilet.latitude)
    toiletDetailIntent.putExtra("long", toilet.longitude)
    activitylauncher.launch(toiletDetailIntent)*/

    private fun ClearFilters(){
        if(!btnMaleFilterActive){
            SwitchMaleFilter()
        }
        if(!btnFemaleFilterActive){
            SwitchFemaleFilter()
        }
        if(!btnWheelchairFilterActive){
            SwitchWheelchairFilter()
        }
        if(!btnChangingTableFilterActive){
            SwitchChangingTableFilter()
        }
    }

    private fun SwitchMaleFilter(){
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

    }

    private fun SwitchFemaleFilter(){
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
    }

    private fun SwitchWheelchairFilter(){
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
    }

    private fun SwitchChangingTableFilter(){
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