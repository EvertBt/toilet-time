package edu.ap.toilettime.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import edu.ap.toilettime.R
import edu.ap.toilettime.model.Toilet

class ToiletDetailActivity : AppCompatActivity() {
    lateinit var toilet: Toilet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toilet_detail)

        toilet = Gson().fromJson(intent.extras?.get(Toilet.TOILET).toString(), Toilet::class.java)
        Log.d("DETAIL", "Received toilet from intent: ${toilet.district}")
    }

    override fun finish() {
        val toiletDetailIntent = Intent(this, MainActivity::class.java)
        toiletDetailIntent.putExtra("lat", toilet.latitude)
        toiletDetailIntent.putExtra("long", toilet.longitude)
        startActivity(toiletDetailIntent)
    }
}