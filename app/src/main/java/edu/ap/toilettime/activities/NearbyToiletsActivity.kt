package edu.ap.toilettime.activities

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Adapter
import android.widget.Button
import android.widget.ListView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import edu.ap.toilettime.Adapters.ToiletAdapter
import edu.ap.toilettime.R
import edu.ap.toilettime.database.ToiletFirebaseRepository

class NearbyToiletsActivity : AppCompatActivity() {
    private var toiletRepository : ToiletRepository = ToiletRepository()
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_toilets)

        var toilets = toiletRepository.allToilets(false)

        btnBack = findViewById(R.id.btnBackNearbyToilets)

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

        Thread{
            do{
                val toilets = toiletRepository.allToilets(false)
                if(toilets != null){
                    runOnUiThread{
                        toiletAdapter = ToiletAdapter(this, toilets as ArrayList<Toilet>)
                        lvToilets.adapter = toiletAdapter as ToiletAdapter
                    }
                }else{
                    Thread.sleep(200)
                }
            }while (toilets == null)
        }.start()
    }

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
}