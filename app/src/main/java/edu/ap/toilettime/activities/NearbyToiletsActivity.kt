package edu.ap.toilettime.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.add
import androidx.fragment.app.commit
import edu.ap.toilettime.R
import edu.ap.toilettime.fragments.ToiletFilterFragment

class NearbyToiletsActivity : AppCompatActivity() {
    lateinit var BTNBack : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_toilets)

        BTNBack = findViewById(R.id.BTNBackNearbyToilets)

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