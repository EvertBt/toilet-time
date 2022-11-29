package edu.ap.toilettime.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import edu.ap.toilettime.R
import edu.ap.toilettime.api.APIHelper
import edu.ap.toilettime.database.DatabaseHelper
import edu.ap.toilettime.maps.MapHelper
import edu.ap.toilettime.model.Address
import edu.ap.toilettime.model.Toilet
import edu.ap.toilettime.model.User
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay


class AddToiletActivity : AppCompatActivity(){

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
    lateinit var location: GeoPoint

    private var address: Address? = null
    private var updated: Boolean = false
    private var toilet: Toilet? = null

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

        //Get location from intent
        location = Gson().fromJson(intent.extras?.get(Toilet.LOCATION).toString(), GeoPoint::class.java)

        //Setup OSM
        setupMap()

        //Call API for address
        tvAddress.text = "Adres aan het laden..."
        APIHelper().searchAddress(location, this@AddToiletActivity)

        //Register button clicks
        btnBack.setOnClickListener { onBackClick() }
        btnAdd.setOnClickListener { onAddClick() }
    }

    private fun setupMap(){

        mapHelper = MapHelper(packageName, cacheDir.absolutePath, findViewById(R.id.minimapview), this@AddToiletActivity)
        mapHelper.initMap(false, GeoPoint(location.latitude, location.longitude), ArrayList(), 20.0, true)

        mapHelper.getMapView()!!.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                location = p
                mapHelper.clearMarkers()
                mapHelper.addMarker(
                    null,
                    location,
                    "",
                    R.mipmap.icon_toilet_map_larger
                )
                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }))
    }

    private fun onAddClick(){

        if (address == null){
            Toast.makeText(this, "Please wait until the address is loaded", Toast.LENGTH_LONG).show()
            return
        }

        if (Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$").matches(txtEmail.text.toString())){

            updated = true

            //Create toilet
            toilet = Toilet(
                "",
                txtEmail.text.toString(),
                location.latitude,
                location.longitude,
                address!!.street,
                address!!.houseNr,
                address!!.district,
                address!!.districtCode,
                cbMenAccessible.isChecked,
                cbWomenAccessible.isChecked,
                cbWheelchairAccessible.isChecked,
                cbChangingTable.isChecked,
                ArrayList()
            )

            //Update shared preferences
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString(User.EMAIL, txtEmail.text.toString())
            editor.apply()

            Toast.makeText(this, "Toilet succesvol toegevoegd", Toast.LENGTH_LONG).show()

            //Return to main + center new toilet

            finish()

        }else{
            tvAddError.visibility = View.VISIBLE
        }
    }

    override fun finish() {
        intent = Intent()
        intent.putExtra(Toilet.TOILET, Gson().toJson(toilet))
        intent.putExtra(Toilet.LOCATION, Gson().toJson(location))
        setResult(201, intent)
        super.finish()
    }

    fun updateAddress(address: Address){
        tvAddress.text = "${address.street} ${address.houseNr}, ${address.districtCode} ${address.district}"
        this.address = address
    }

    private fun onBackClick(){
        if (updated){
            setResult(201)
        }
        super.finish()
    }
}