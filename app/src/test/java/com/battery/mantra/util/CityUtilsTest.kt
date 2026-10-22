package com.battery.mantra.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CityUtilsTest {

    @Test
    fun checkCityMismatch_nullOrBlank_returnsFalse() {
        assertFalse(checkCityMismatch(null, "Sector 14, Gurgaon, Haryana"))
        assertFalse(checkCityMismatch("Gurgaon", null))
        assertFalse(checkCityMismatch("", "Sector 14, Gurgaon"))
        assertFalse(checkCityMismatch("Delhi", ""))
        assertFalse(checkCityMismatch(null, null))
    }

    @Test
    fun checkCityMismatch_directMatch_returnsFalse() {
        assertFalse(checkCityMismatch("Noida", "Flat 402, Sector 62, Noida, UP 201301"))
        assertFalse(checkCityMismatch("Mumbai", "Andheri West, Mumbai, Maharashtra"))
    }

    @Test
    fun checkCityMismatch_gurgaonSynonyms_returnsFalse() {
        assertFalse(checkCityMismatch("Gurgaon", "DLF Phase 3, Gurugram, Haryana"))
        assertFalse(checkCityMismatch("Gurugram", "Sector 29, Gurgaon, Haryana"))
    }

    @Test
    fun checkCityMismatch_delhiNcrSynonyms_returnsFalse() {
        assertFalse(checkCityMismatch("Delhi", "Connaught Place, New Delhi"))
        assertFalse(checkCityMismatch("Delhi NCR", "Rohini, Delhi 110085"))
        assertFalse(checkCityMismatch("NCR", "Saket, Delhi"))
    }

    @Test
    fun checkCityMismatch_bangaloreSynonyms_returnsFalse() {
        assertFalse(checkCityMismatch("Bangalore", "Koramangala, Bengaluru, Karnataka"))
        assertFalse(checkCityMismatch("Bengaluru", "Indiranagar, Bangalore, Karnataka"))
    }

    @Test
    fun checkCityMismatch_actualMismatch_returnsTrue() {
        assertTrue(checkCityMismatch("Gurgaon", "Sector 62, Noida, Uttar Pradesh"))
        assertTrue(checkCityMismatch("Mumbai", "Koramangala, Bengaluru, Karnataka"))
        assertTrue(checkCityMismatch("Delhi", "MG Road, Pune, Maharashtra"))
    }
}
