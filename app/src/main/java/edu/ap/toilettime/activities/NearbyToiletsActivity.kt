package edu.ap.toilettime.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import edu.ap.toilettime.Adapters.ToiletAdapter
import edu.ap.toilettime.R
import edu.ap.toilettime.database.ToiletRepository
import edu.ap.toilettime.model.Toilet

class NearbyToiletsActivity : AppCompatActivity() {
    lateinit var BTNBack : Button
    lateinit var lvToilets : ListView

    companion object{
        const val EXTRA_TOILETLIST = "toilets"

        fun nearbyToiletIntent(context: Context, toilets: ArrayList<Toilet>?): Intent{
            val toiletsIntent = Intent(context, toilets)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_toilets)

        BTNBack = findViewById(R.id.BTNBackNearbyToilets)
        lvToilets = findViewById(R.id.LVToilets)

        BTNBack.setOnClickListener {
            this.finish()
        }
    }

    override fun finish() : Unit {
        intent = Intent()
        // intent.putExtra() return needed data to add new toilet
        setResult(RESULT_OK, intent)
        super.finish()
    }
}