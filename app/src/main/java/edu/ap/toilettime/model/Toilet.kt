package edu.ap.toilettime.model

class Toilet (var id: String? = null, var lat: Double, var long: Double, var street: String?, var houseNr: String?, var district: String?, var districtCode: String?, var menAccessible: Boolean, var womenAccessible: Boolean, var wheelchairAccessible: Boolean,
              var changingTable: Boolean, var reporterEmails: List<User> = ArrayList()) : java.io.Serializable{

    companion object {
        var ID = "id"
        var STREET = "street"
        var HOUSE_NR = "house_nr"
        var DISTRICT_CODE = "district_code"
        var DISTRICT = "district"
        var MEN_ACCESSIBLE = "men_accessible"
        var WOMEN_ACCESSIBLE = "women_accessible"
        var WHEELCHAIR_ACCESSIBLE = "wheelchair_accessible"
        var CHANGING_TABLE = "changing_accessible"
        var REPORTER_EMAILS = "reporter_emails"
        var LAT = "latitude"
        var LONG = "longitude"
    }
}