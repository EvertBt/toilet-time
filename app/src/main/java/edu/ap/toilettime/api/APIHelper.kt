package edu.ap.toilettime.api

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.ap.toilettime.activities.AddToiletActivity
import edu.ap.toilettime.maps.MapHelper
import edu.ap.toilettime.model.Toilet
import kotlinx.coroutines.runBlocking
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.io.IOException
import java.net.URL

class APIHelper {

    private val apiTag = "API"

    private fun getURLContentAsString(url: URL) : String{

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val call = client.newCall(request)
        val response = call.execute()
        return response.body!!.string()
    }

    fun getToilets() : List<Toilet>{

        val url = URL("https://geodata.antwerpen.be/arcgissql/rest/services/P_Portal/portal_publiek1/MapServer/8/query?where=1%3D1&outFields=*&outSR=4326&f=json")
        val result : String?
        val myHandler = Handler(Looper.getMainLooper())

        myHandler.post{
            Log.i(apiTag, "Loading toilets from API")
        }

        //Run blocking
        result = getURLContentAsString(url)
        val toilets: ArrayList<Toilet> = ArrayList()

        val jObject = JSONObject(result)
        val allToiletsJsonArray = jObject.getJSONArray("features")

        for (i in 0 until allToiletsJsonArray.length()){
            val toiletObject = allToiletsJsonArray.getJSONObject(i).getJSONObject("attributes")

            val toilet = Toilet(
                id = toiletObject.getString("ID"),
                addedBy = "Stad Antwerpen",
                latitude = getLat(allToiletsJsonArray.getJSONObject(i).getJSONObject("geometry")),
                longitude = getLong(allToiletsJsonArray.getJSONObject(i).getJSONObject("geometry")),
                street = toiletObject.getString("STRAAT"),
                houseNr =  toiletObject.getString("HUISNUMMER"),
                district = toiletObject.getString("DISTRICT"),
                districtCode = toiletObject.getString("POSTCODE"),
                menAccessible = toiletObject.getString("DOELGROEP").contains("man") || toiletObject.getString("TYPE").equals("urinoir"),
                womenAccessible = toiletObject.getString("DOELGROEP").contains("vrouw"),
                wheelchairAccessible = toiletObject.getString("INTEGRAAL_TOEGANKELIJK").equals("ja"),
                changingTable = toiletObject.getString("LUIERTAFEL").equals("ja"),
                reporterEmails = ArrayList()
            )

            if (toilet.street != "null" && toilet.houseNr != "null" && toilet.district != "null") toilets.add(toilet)
        }

        return toilets
        //
    }

    private fun getLat(obj: JSONObject) : Double{

        return try {
            val latString = obj.getJSONObject("y").getString("value")
            latString.toDouble()

        }catch (e: JSONException){
            obj.getDouble("y")
        }
    }

    private fun getLong(obj: JSONObject) : Double{

        return try {
            val latString = obj.getJSONObject("x").getString("value")
            latString.toDouble()

        }catch (e: JSONException){
            obj.getDouble("x")
        }
    }

    fun searchLocation(address: String, ctx: Context, mapHelper: MapHelper){

        //Get address location async from API
        Thread{
            val client = OkHttpClient()
            val request = Request.Builder().url(URL("https://nominatim.openstreetmap.org/search.php?q=$address&format=jsonv2")).build()
            val myHandler = Handler(Looper.getMainLooper())

            client.newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    myHandler.post {
                        Toast.makeText(ctx,"There was a problem trying to get address data, please try again.", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {

                        try{
                            val jObject : JSONObject = JSONArray(response.body!!.string()).getJSONObject(0)
                            val lat : Double = jObject.getString("lat").toDouble()
                            val lon : Double = jObject.getString("lon").toDouble()
                            val name : String = jObject.getString("display_name")

                            val geoPoint = GeoPoint(lat, lon)
                            myHandler.post {
                                //mapHelper.addMarker(null, geoPoint, name, android.R.drawable.btn_plus) //TODO add icon here
                                mapHelper.setCenter(geoPoint, name)
                            }
                        }catch (_: Exception){
                            myHandler.post {
                                Toast.makeText(ctx, "Address does not exist", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            })
        }.start()
    }

    fun searchAddress(location: GeoPoint, ctx: Context, activity: AppCompatActivity){

        //Get address location async from API
        Thread{
            val client = OkHttpClient()
            val request = Request.Builder().url(URL("https://nominatim.openstreetmap.org/reverse.php?lat=${location.latitude}&lon=${location.longitude}&zoom=18&format=jsonv2")).build()
            val myHandler = Handler(Looper.getMainLooper())

            client.newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    myHandler.post {
                        Toast.makeText(ctx,"There was a problem trying to get address data, please try again.", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    try{
                        val jObject : JSONObject = JSONArray(response.body!!.string()).getJSONObject(0).getJSONObject("address")
                        val street : String = jObject.getString("road")
                        val houseNr : String = jObject.getString("house_number")
                        val city : String = jObject.getString("city")
                        val districtCode : String = jObject.getString("postcode")

                        if (activity is AddToiletActivity){
                            activity.updateAddress("${street} ${houseNr}, ${districtCode} ${city}")
                        }

                    }catch (_: Exception){
                        myHandler.post {
                            Toast.makeText(ctx, "Address does not exist", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            })
        }.start()
    }
}
