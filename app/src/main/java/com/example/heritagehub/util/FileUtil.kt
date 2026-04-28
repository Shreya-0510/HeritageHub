package com.example.heritagehub.util

import android.net.Uri
import java.io.File

object FileUtil {
    /**
     * Temporary guard for project demo. 
     * Checks if a local URI path actually exists on the current device.
     */
    fun isLocalFileValid(uriString: String?): Boolean {
        if (uriString.isNullOrBlank()) return false
        return try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file") {
                val path = uri.path
                if (path != null) {
                    File(path).exists()
                } else false
            } else {
                // Assume http, https, or other content providers are valid for demo purposes
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Filters a list of URIs, keeping only those that exist locally (or are non-file schemes).
     */
    fun filterValidUris(uris: List<String>): List<String> {
        return uris.filter { isLocalFileValid(it) }
    }
}
