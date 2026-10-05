package com.example.data.geo

import kotlin.math.*

data class GeoPoint(val lat: Double, val lng: Double)

object ChefchaouenGeoFence {

    // Chefchaouen City Center (Outa El Hammam / Kasbah)
    val CHEFCHAOUEN_CENTER = GeoPoint(35.1688, -5.2636)

    // Pre-configured municipal service boundary polygon for Chefchaouen
    // Surrounds the Medina, Outa El Hammam, Bab Souk, Ras El Maa, Bab El Ain, Sidi Abdelhamid, Hay El Andalous
    val DEFAULT_SERVICE_POLYGON = listOf(
        GeoPoint(35.1820, -5.2680), // North / Jebel El Kelaa slope
        GeoPoint(35.1760, -5.2530), // East / Ras El Maa & Spanish Mosque ridge
        GeoPoint(35.1670, -5.2520), // South-East / Bab El Ansar
        GeoPoint(35.1580, -5.2610), // South / Hay El Andalous & Gare Routière
        GeoPoint(35.1600, -5.2750), // South-West / Bab El Ain & Avenue Hassan II
        GeoPoint(35.1690, -5.2820), // West / Al Ayoun & N2 entrance from Tetouan
        GeoPoint(35.1780, -5.2760)  // North-West / Bab Souk hillside
    )

    // Well-known Chefchaouen landmarks for map & pin selection
    val LANDMARKS = listOf(
        Pair("ساحة وطاء الحمام (Outa El Hammam)", GeoPoint(35.1688, -5.2636)),
        Pair("رأس الماء (Ras El Maa)", GeoPoint(35.1712, -5.2575)),
        Pair("باب العين (Bab El Ain)", GeoPoint(35.1675, -5.2678)),
        Pair("باب السوق (Bab Souk)", GeoPoint(35.1715, -5.2662)),
        Pair("حي الأندلس (Hay El Andalous)", GeoPoint(35.1630, -5.2645)),
        Pair("حي العيون (Hay Al Ayoun)", GeoPoint(35.1700, -5.2720)),
        Pair("شارع الحسن الثاني (Ave Hassan II)", GeoPoint(35.1655, -5.2680)),
        Pair("سيدي عبد الحميد (Sidi Abdelhamid)", GeoPoint(35.1650, -5.2600))
    )

    /**
     * Ray-Casting algorithm to check if a coordinate falls inside the service boundary polygon.
     */
    fun isInsidePolygon(point: GeoPoint, polygon: List<GeoPoint>): Boolean {
        if (polygon.size < 3) return false
        var inside = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            val pi = polygon[i]
            val pj = polygon[j]

            val intersect = ((pi.lat > point.lat) != (pj.lat > point.lat)) &&
                    (point.lng < (pj.lng - pi.lng) * (point.lat - pi.lat) / (pj.lat - pi.lat) + pi.lng)
            if (intersect) {
                inside = !inside
            }
            j = i
        }
        return inside
    }

    /**
     * Haversine formula to compute great-circle distance in kilometers.
     */
    fun distanceKm(p1: GeoPoint, p2: GeoPoint): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(p2.lat - p1.lat)
        val dLng = Math.toRadians(p2.lng - p1.lng)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(p1.lat)) * cos(Math.toRadians(p2.lat)) *
                sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Format distance for Moroccan display (e.g. "450 m" or "1.8 km")
     */
    fun formatDistance(km: Double): String {
        return if (km < 1.0) {
            "${(km * 1000).roundToInt()} m"
        } else {
            String.format(java.util.Locale.US, "%.1f km", km)
        }
    }

    /**
     * Estimated arrival time in minutes based on moped / on-foot terrain in Chefchaouen Medina
     */
    fun estimateEtaMinutes(km: Double): Int {
        // Average speed in hilly Medina is ~15 km/h + 5 mins baseline
        val mins = (km / 15.0 * 60.0).roundToInt() + 6
        return max(5, mins)
    }

    /**
     * Calculate delivery fee in MAD based on distance (base 12 MAD in Chefchaouen Medina)
     */
    fun calculateDeliveryFeeMad(km: Double): Double {
        val baseFee = 12.0
        val extra = if (km > 1.0) (km - 1.0) * 4.0 else 0.0
        return (baseFee + extra).roundToInt().toDouble()
    }
}
