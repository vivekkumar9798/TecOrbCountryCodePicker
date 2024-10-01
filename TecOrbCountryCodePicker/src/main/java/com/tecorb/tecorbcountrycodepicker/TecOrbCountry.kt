package com.tecorb.tecorbcountrycodepicker

import java.util.Locale

class TecOrbCountry(val iso: String, val phoneCode: String, val name: String) {
    fun isEligibleForQuery(query: String): Boolean {
        var query = query
        query = query.lowercase(Locale.getDefault())
        return (name.lowercase(Locale.getDefault()).contains(query)
                || iso.lowercase(Locale.getDefault()).contains(query)
                || phoneCode.lowercase(Locale.getDefault()).contains(query))
    }
}
