package edu.ap.toilettime.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import edu.ap.toilettime.Adapters.ToiletAdapter
import edu.ap.toilettime.R
import edu.ap.toilettime.database.ToiletRepository
import edu.ap.toilettime.model.Toilet

class MainActivity : AppCompatActivity() {
    lateinit var BTNAddToilet : Button
    lateinit var BTNNearbyToilets : Button
    lateinit var BTNToiletDetail : Button //temp: will be icons on a map
    var toiletList: ArrayList<Toilet>? = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        BTNAddToilet = findViewById(R.id.BTNBackNearbyToilets)
        BTNNearbyToilets = findViewById(R.id.BTNNearbyToilets)
        BTNToiletDetail = findViewById(R.id.BTNToiletDetail)

        val addToiletResultLauncher = CreateAddToiletResultLauncher()
        val nearbyToiletsResultLauncher = CreateNearbyToiletsResultLauncher()
        val toiletDetailResultLauncher = CreateToiletDetailResultLauncher()

        BTNAddToilet.setOnClickListener {
            clickBTNAddToilet(addToiletResultLauncher)
        }

        BTNNearbyToilets.setOnClickListener {
            clickBTNNearbyToilets(nearbyToiletsResultLauncher)
        }

        BTNToiletDetail.setOnClickListener {
            clickBTNToiletDetail(toiletDetailResultLauncher)
        }

        Thread{
            val db = ToiletRepository()
            db.allToilets()?.let { toiletList?.addAll(it) }

            if (toiletList != null) {

                val lvToilets: ListView = findViewById(R.id.LVToilets)
                val toiletAdapter = ToiletAdapter(this, toiletList!!)

                lvToilets.adapter = toiletAdapter
                //start
            }
        }.start()
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
        //AddToiletIntent.putExtra() add all needed extras to add a new toilet
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
        val nearbyToiletsIntent = Intent(this, NearbyToiletsActivity::class.java)
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

    fun clickBTNToiletDetail(resultLauncher : ActivityResultLauncher<Intent>){
        val toiletDetailIntent = Intent(this, ToiletDetailActivity::class.java)
        //AddToiletIntent.putExtra() add all needed extras to add a new toilet
        resultLauncher.launch(toiletDetailIntent)
    }
}