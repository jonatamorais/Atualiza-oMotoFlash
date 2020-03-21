package br.com.motoflash.core.data.network.model

import com.google.firebase.firestore.GeoPoint

data class Location(
    var geohash: String? = null,
    var geopoint: GeoPoint? = null
)