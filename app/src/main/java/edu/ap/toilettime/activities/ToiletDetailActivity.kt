package edu.ap.toilettime.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.ap.toilettime.R

class ToiletDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toilet_detail)
    }

    override fun finish() : Unit {
        intent = Intent()
        // intent.putExtra() return needed data to add new toilet
        setResult(RESULT_OK, intent)
        super.finish()
    }
}