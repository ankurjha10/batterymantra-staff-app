package com.battery.mantra.util

/**
 * Checks whether the city selected by the customer on the website mismatches
 * with the provided shipping address.
 *
 * @param selectedCity The recorded city from website selection.
 * @param shippingAddress The delivery/shipping address entered by customer.
 * @return True if there is an apparent mismatch, false otherwise.
 */
fun checkCityMismatch(selectedCity: String?, shippingAddress: String?): Boolean {
    if (selectedCity.isNullOrBlank() || shippingAddress.isNullOrBlank()) return false
    val sel = selectedCity.trim().lowercase()
    val addr = shippingAddress.lowercase()
    // Direct match
    if (addr.contains(sel)) return false
    // Common Indian city synonyms and variations
    if ((sel.contains("gurgaon") || sel.contains("gurugram")) && 
        (addr.contains("gurgaon") || addr.contains("gurugram"))) return false
    if ((sel.contains("delhi") || sel.contains("ncr")) && addr.contains("delhi")) return false
    if ((sel.contains("bangalore") || sel.contains("bengaluru")) && 
        (addr.contains("bangalore") || addr.contains("bengaluru"))) return false
    return true
}
