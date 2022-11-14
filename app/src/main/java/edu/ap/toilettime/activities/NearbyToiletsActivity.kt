package edu.ap.toilettime.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import edu.ap.toilettime.R

class NearbyToiletsActivity : AppCompatActivity() {
    private var toiletRepository : ToiletRepository = ToiletRepository()
    lateinit var BTNBack : Button
    lateinit var lvToilets : ListView

    companion object{
        const val EXTRA_TOILETLIST = "toilets"

<<<<<<< HEAD
        /*fun nearbyToiletIntent(context: Context, toilets: ArrayList<Toilet>?): Intent{
            val toiletsIntent = Intent(context, toilets)
        }*/
=======
        fun nearbyToiletIntent(context: Context): Intent{
            val toiletsIntent = Intent(context, NearbyToiletsActivity::class.java)
            return toiletsIntent
        }
>>>>>>> NearbyToiletsDesign
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_toilets)

<<<<<<< HEAD
        BTNBack = findViewById(R.id.btnAddToilet)
=======
        var toilets = toiletRepository.allToilets(false)

        BTNBack = findViewById(R.id.BTNBackNearbyToilets)
>>>>>>> NearbyToiletsDesign
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