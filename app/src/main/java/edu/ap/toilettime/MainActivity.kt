package edu.ap.toilettime

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import edu.ap.toilettime.database.*
import edu.ap.toilettime.model.Toilet

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = ToiletRepository()

        //Run on new thread
        Thread{
            //Add toilet
            /*db.addToilet(Toilet(
                id = "",
                street = "Ellermanstraat 33",
                district = "Antwerpen",
                menAccessible = true,
                womenAccessible = true,
                wheelchairAccessible = true,
                changingTable = false,
                reporterEmails = ArrayList()
            ))*/

            //Retrieve all toilets
            val toilets = db.allToilets()
            if (toilets != null){
                for (t in toilets){
                    Log.i("Toilets", "Found toilet with id ${t.id}")
                }
            }


            //Get by id
            val toilet = db.getToilet("ADiLpfc5DXKlKHAIMT4a")
            if (toilet != null) Log.i("Toilets", "Found toilet with id ${toilet.id}! location: ${toilet.street}")

            //Delete toilet
            db.deleteToilet("ADiLpfc5DXKlKHAIMT4a")
        }.start()

        Log.i("APP", "Application started!")
    }
}