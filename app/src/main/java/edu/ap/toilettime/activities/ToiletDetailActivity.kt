package edu.ap.toilettime.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import edu.ap.toilettime.R
import edu.ap.toilettime.database.DatabaseHelper
import edu.ap.toilettime.maps.MapHelper
import edu.ap.toilettime.model.Toilet
import edu.ap.toilettime.model.User
import org.osmdroid.util.GeoPoint

class ToiletDetailActivity : AppCompatActivity() {
    lateinit var toilet: Toilet
    lateinit var cbMenAccessible: CheckBox
    lateinit var cbWomenAccessible: CheckBox
    lateinit var cbWheelchairAccessible: CheckBox
    lateinit var cbChangingTable: CheckBox
    lateinit var tvAddress: TextView
    lateinit var tvReportCount: TextView
    lateinit var tvReportError: TextView
    lateinit var txtEmail: EditText
    lateinit var btnBack: Button
    lateinit var btnReport: Button
    lateinit var mapHelper: MapHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toilet_detail)

        //Init views
        cbMenAccessible = findViewById(R.id.cbDetailMenAccessible)
        cbWomenAccessible = findViewById(R.id.cbDetailWomenAccessible)
        cbWheelchairAccessible = findViewById(R.id.cbDetailWheelchairAccessible)
        cbChangingTable = findViewById(R.id.cbDetailChangingTable)
        tvAddress = findViewById(R.id.tvDetailAddress)
        tvReportCount = findViewById(R.id.tvDetailReportCount)
        tvReportError = findViewById(R.id.tvDetailReportError)
        txtEmail = findViewById(R.id.txtDetailReportEmail)
        btnBack = findViewById(R.id.btnBackToiletDetail)
        btnReport = findViewById(R.id.btnDetailReport)

        //Setup OSM
        mapHelper = MapHelper(packageName, cacheDir.absolutePath, findViewById(R.id.minimapview), null, this@ToiletDetailActivity)

        //Retrieve clicked toilet from intent
        toilet = Gson().fromJson(intent.extras?.get(Toilet.TOILET).toString(), Toilet::class.java)

        //Register button clicks
        btnBack.setOnClickListener { super.finish() }
        btnReport.setOnClickListener { onReportClick() }

        //Init detail view
        drawDetails()
    }

    private fun drawDetails(){
        cbMenAccessible.isChecked = toilet.menAccessible
        cbWomenAccessible.isChecked = toilet.womenAccessible
        cbWheelchairAccessible.isChecked = toilet.wheelchairAccessible
        cbChangingTable.isChecked = toilet.changingTable

        tvAddress.text = "${toilet.street} ${toilet.houseNr}, ${toilet.districtCode} ${toilet.district}"
        tvReportCount.text = "Aantal rapporteringen: ${toilet.reporterEmails.size}"

        mapHelper.initMap(false, GeoPoint(toilet.latitude, toilet.longitude), ArrayList(listOf(toilet)), 20.0)
    }

    private fun onReportClick(){

        //Force close keyboard
        txtEmail.clearFocus()
        val view: View? = findViewById(android.R.id.content)
        if (view != null) {
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        if (Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$").matches(txtEmail.text.toString())){

            tvReportError.visibility = View.VISIBLE
            tvReportError.text = "Toilet is succesvol gerapporteerd"
            tvReportError.setTextColor(getColor(R.color.success))
            tvReportCount.text = "Aantal rapporteringen: ${toilet.reporterEmails.size + 1}"

            toilet.reporterEmails.add(User(txtEmail.text.toString()))
            DatabaseHelper(null, null).updateToilet(toilet)
        }else{
            tvReportError.visibility = View.VISIBLE
        }
    }
}