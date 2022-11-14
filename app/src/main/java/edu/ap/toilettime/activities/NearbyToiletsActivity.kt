package edu.ap.toilettime.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import edu.ap.toilettime.Adapters.ToiletAdapter
import edu.ap.toilettime.R
import edu.ap.toilettime.database.ToiletRepository
import edu.ap.toilettime.model.Toilet

class NearbyToiletsActivity : AppCompatActivity() {
    private var toiletRepository : ToiletRepository = ToiletRepository()
    lateinit var BTNBack : Button
    lateinit var lvToilets : ListView

    companion object{
        const val EXTRA_TOILETLIST = "toilets"

        fun nearbyToiletIntent(context: Context): Intent{
            val toiletsIntent = Intent(context, NearbyToiletsActivity::class.java)
            return toiletsIntent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_toilets)

        var toilets = toiletRepository.allToilets(false)

        BTNBack = findViewById(R.id.BTNBackNearbyToilets)
        lvToilets = findViewById(R.id.LVToilets)

        BTNBack.setOnClickListener {
            this.finish()
        }

        Thread{
            do{
                toilets = toiletRepository.allToilets(false)
                if(toilets != null){
                    runOnUiThread{
                        val toiletAdapter = ToiletAdapter(this, toilets as ArrayList<Toilet>)
                        lvToilets.adapter = toiletAdapter
                    }
                }else{
                    Thread.sleep(200)
                }
            }while (toilets == null)
        }.start()
    }

    override fun finish() : Unit {
        intent = Intent()
        // intent.putExtra() return needed data to add new toilet
        setResult(RESULT_OK, intent)
        super.finish()
    }
}