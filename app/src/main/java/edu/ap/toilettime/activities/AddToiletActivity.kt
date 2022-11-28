package edu.ap.toilettime.activities

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import edu.ap.toilettime.R
import edu.ap.toilettime.maps.MapHelper
import edu.ap.toilettime.model.User

class AddToiletActivity : AppCompatActivity() {

    lateinit var cbMenAccessible: CheckBox
    lateinit var cbWomenAccessible: CheckBox
    lateinit var cbWheelchairAccessible: CheckBox
    lateinit var cbChangingTable: CheckBox
    lateinit var tvAddress: TextView
    lateinit var tvAddError: TextView
    lateinit var txtEmail: EditText
    lateinit var btnBack: Button
    lateinit var btnAdd: Button
    lateinit var mapHelper: MapHelper
    lateinit var sharedPreferences: SharedPreferences

    private var updated: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_toilet)

        //Init views
        cbMenAccessible = findViewById(R.id.cbAddToiletMenAccessible)
        cbWomenAccessible = findViewById(R.id.cbAddToiletWomenAccessible)
        cbWheelchairAccessible = findViewById(R.id.cbAddToiletWheelchairAccessible)
        cbChangingTable = findViewById(R.id.cbAddToiletChangingTable)
        tvAddress = findViewById(R.id.tvAddToiletAddress)
        tvAddError = findViewById(R.id.tvAddToiletError)
        txtEmail = findViewById(R.id.txtAddEmailAddress)
        btnBack = findViewById(R.id.btnBackAddToilet)
        btnAdd = findViewById(R.id.btnAddToilet)

        //Load shared preferences
        sharedPreferences = getSharedPreferences(User.USER, Context.MODE_PRIVATE)
        txtEmail.setText(sharedPreferences.getString(User.EMAIL, ""))

        //TODO Get location from intent, get map center location when no permissions

        //Setup OSM
        mapHelper = MapHelper(packageName, cacheDir.absolutePath, findViewById(R.id.minimapview), this@AddToiletActivity)

        //Register button clicks
        btnBack.setOnClickListener { onBackClick() }
        btnAdd.setOnClickListener { onAddClick() }

        //Init add view
        drawDetails()
    }

    private fun drawDetails(){

    }

    private fun onAddClick(){

    }

    fun updateAddress(address: String){
        tvAddress.text = address
    }

    private fun onBackClick(){
        if (updated){
            setResult(204)
        }
        super.finish()
    }
}