package edu.ap.toilettime.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import edu.ap.toilettime.R
import edu.ap.toilettime.model.Toilet


class ToiletDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toilet_detail)

        val toilet: Toilet = Gson().fromJson(intent.extras?.get(Toilet.TOILET).toString(), Toilet::class.java)
        Log.d("DETAIL", "Received toilet from intent: ${toilet.district}")
    }

    override fun finish() {
        intent = Intent()
        // intent.putExtra() return needed data to add new toilet
        setResult(RESULT_OK, intent)
        super.finish()
    }
}