package com.example.heritagehub.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object LocationUtils {
    data class GeocodedAddress(
        val addressLine1: String = "",
        val addressLine2: String = "",
        val city: String = "",
        val state: String = "",
        val pincode: String = ""
    )

    suspend fun reverseGeocode(context: Context, location: Location): GeocodedAddress? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                return@withContext GeocodedAddress(
                    addressLine1 = address.getAddressLine(0) ?: "",
                    addressLine2 = address.subLocality ?: address.featureName ?: "",
                    city = address.locality ?: "",
                    state = address.adminArea ?: "",
                    pincode = address.postalCode ?: ""
                )
            }
        } catch (e: Exception) {
            // Handle geocoder errors
        }
        return@withContext null
    }
}
