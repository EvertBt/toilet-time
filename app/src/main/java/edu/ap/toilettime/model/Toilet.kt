package edu.ap.toilettime.model

class Toilet (var id: String, var street: String, var district: String, var menAccessible: Boolean, var womenAccessible: Boolean, var wheelchairAccessible: Boolean,
              var changingTable: Boolean, var reporterEmails: List<User>) {

    companion object {
        var ID = "id"
        var STREET = "street"
        var DISTRICT = "district"
        var MEN_ACCESSIBLE = "men_accessible"
        var WOMEN_ACCESSIBLE = "women_accessible"
        var WHEELCHAIR_ACCESSIBLE = "wheelchair_accessible"
        var CHANGING_TABLE = "changing_accessible"
        var REPORTER_EMAILS = "reporter_emails"
    }
}