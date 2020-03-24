package br.com.motoflash.courier.service.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import br.com.motoflash.core.ui.util.COURIER_ONLINE
import br.com.motoflash.core.ui.util.CURRENT_WORK_ORDER
import br.com.motoflash.core.ui.util.LAST_LOCATION
import br.com.motoflash.core.ui.util.LAST_LOCATION_TIME
import br.com.motoflash.courier.BuildConfig
import br.com.motoflash.courier.R
import br.com.motoflash.courier.ui.splash.SplashActivity
import com.fonfon.kgeohash.GeoHash
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
import com.pixplicity.easyprefs.library.Prefs


class LocationService : Service() {

    private var mNotificationManager: NotificationManager? = null
    private var callback: LocationCallback? = null
    private var client: FusedLocationProviderClient? = null
    private var mLastLocation: Location? = null

    private var currentUserOnline = Prefs.getBoolean(COURIER_ONLINE, false)

    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser!!
    private val firestore = FirebaseFirestore.getInstance()
    private var workOrderRef: DocumentReference? = null
    private val courierRef = firestore.collection("couriers").document(user.uid)

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        log("onStartCommand")
        OPEN = true

        initializeLocationManager()

        return START_NOT_STICKY
    }

    private fun newShowNotification(start: Boolean) {
        log("newShowNotification")
        val open = "open"

        registerReceiver(openReceiver, IntentFilter(open))
        val broadcastIntent = PendingIntent.getBroadcast(
            this, 0, Intent(open), PendingIntent.FLAG_UPDATE_CURRENT
        )


        val CHANNEL_ID = "03"// The id of the channel.

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentIntent(broadcastIntent)
            .setSmallIcon(R.drawable.motoflash_logo_blue)

        if (start) {
            builder.setOngoing(true)
            builder.setContentText("Você está online")
        } else
            builder.setContentText("Serviço de localização parou")

        builder.color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            resources.getColor(R.color.colorBlue, theme)
        else
            resources.getColor(R.color.colorAccent)

        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Motoflash para Entregadores"// The user-visible name of the channel.
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            builder.setChannelId(CHANNEL_ID)
            mNotificationManager!!.createNotificationChannel(mChannel)
        }


//        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
        startForeground(1, builder.build())
    }

    private var openReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            log("received open broadcast")

            val openActivity = Intent(context, SplashActivity::class.java)
            openActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(openActivity)

            /*// Stop the service when the notification is tapped
            unregisterReceiver(this)
            if (client != null && callback != null) {
                client.removeLocationUpdates(callback)
            }
            stopSelf()*/
        }
    }

    private fun initializeLocationManager() {
        log("initializeLocationManager")

        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {
        log("requestLocationUpdates")

        if (!currentUserOnline) {
            log("offline")
            newShowNotification(false)
            stopSelf()
        } else {
            log("online")
            newShowNotification(true)
        }

        val request = LocationRequest()

        /*if (BuildConfig.DEBUG) {
            UPDATE_INTERVAL_IN_MILLISECONDS = 5000
        }*/

        request.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        request.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY


        client = LocationServices.getFusedLocationProviderClient(this)

        if (callback != null) {
            client!!.removeLocationUpdates(callback)
        }

        callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                log("onLocationResult")

                val location = locationResult!!.lastLocation

                if (location != null) {

                    log("location != null")

                    val updateLocation = br.com.motoflash.core.data.network.model.Location(
                        geohash = GeoHash(location.latitude, location.longitude).toString(),
                        geopoint = GeoPoint(location.latitude, location.longitude)
                    )

                    log("updateLocation: ${Gson().toJson(updateLocation)} lat: ${location.latitude} lng: ${location.longitude}")

                    courierRef.update("location", updateLocation)

                    if (mLastLocation == null) {
                        log("mLastLocation null")
                        mLastLocation = location

                        log("pref lastlocation")
                        Prefs.putString(
                            LAST_LOCATION,
                            Gson().toJson(updateLocation)
                        )

                        log("pref lastlocationtime")
                        Prefs.putLong(LAST_LOCATION_TIME, System.currentTimeMillis())

                        log("pref currentworkorder")
                        if (Prefs.getString(CURRENT_WORK_ORDER, "").isNotEmpty()) {

                            val hashLocationUpdateWO: HashMap<String, Any> = HashMap()
                            hashLocationUpdateWO["courier.location"] = updateLocation

                            workOrderRef = firestore.collection("workorders")
                                .document(Prefs.getString(CURRENT_WORK_ORDER, ""))
                            workOrderRef!!.update(hashLocationUpdateWO).addOnSuccessListener {
                                log("Update WO Location")
                            }.addOnFailureListener {
                                log("Fail Update WO Location: ${it.message ?: "null"}")
                            }
                        }
                    } else {
                        val distance = location.distanceTo(mLastLocation).toDouble()
                        log("Distance: $distance")
                        if (isBetterLocation(
                                location,
                                mLastLocation
                            ) && location.latitude != 0.0 && location.longitude != 0.0 && distance >= LOCATION_DISTANCE
                        ) {

                            if (mLastLocation!!.latitude != 0.0 && mLastLocation!!.longitude != 0.0) {
                                if (/*location.accuracy <= 20 && location.speed >= 3*/true) {
                                    log("LocationUpdate distanceElapsed: $distance lat:${location.latitude} lng: ${location.longitude}")
                                    mLastLocation = location
                                } else {
                                    log("Location Update Ignored")
                                }

                            } else {
                                mLastLocation = location
                            }

                            Prefs.putString(
                                LAST_LOCATION,
                                Gson().toJson(updateLocation)
                            )
                            Prefs.putLong(LAST_LOCATION_TIME, System.currentTimeMillis())

                            log(
                                "Update WO ${Prefs.getString(
                                    CURRENT_WORK_ORDER,
                                    ""
                                ).isNotEmpty()} id: ${Prefs.getString(CURRENT_WORK_ORDER, "")}"
                            )
                            if (Prefs.getString(CURRENT_WORK_ORDER, "").isNotEmpty()) {

                                val hashLocationUpdateWO: HashMap<String, Any> = HashMap()
                                hashLocationUpdateWO["courier.location"] = updateLocation

                                workOrderRef = firestore.collection("workorders")
                                    .document(Prefs.getString(CURRENT_WORK_ORDER, ""))
                                workOrderRef!!.update(hashLocationUpdateWO).addOnSuccessListener {
                                    log("Update WO Location")
                                }.addOnFailureListener {
                                    log("Fail Update WO Location: ${it.message ?: "null"}")
                                }
                            }

                        } else {
                            log("Location Update Ignored")
                        }
                    }
                }
            }
        }

        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permission == PackageManager.PERMISSION_GRANTED && client != null) {
            // Request location updates and when an update is
            client!!.requestLocationUpdates(request, callback, null)
        }
    }

    private fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
        if (!BuildConfig.DEBUG)
            if (location.isFromMockProvider) {
                return false
            }

        if (currentBestLocation == null) {
            // A new location is always better than no location
            log("currentBestLocation")
            return true
        }

        if (!location.hasAccuracy()) {
            // Mock Location
            log("!location.hasAccuracy()")
            return false
        }

        // Check whether the new location fix is newer or older
        val timeDelta = location.time - currentBestLocation.time
        log("timeDelta: $timeDelta")
        val isSignificantlyNewer = timeDelta > UPDATE_INTERVAL_IN_MILLISECONDS
        log("isSignificantlyNewer: $isSignificantlyNewer")
        val isSignificantlyOlder = timeDelta < -UPDATE_INTERVAL_IN_MILLISECONDS
        log("isSignificantlyOlder: $isSignificantlyOlder")
        val isNewer = timeDelta > 0
        log("isNewer: $isNewer")

        // If it's been more than delta minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false
        }

        // Check whether the new location fix is more or less accurate
        val accuracyDelta = (location.accuracy - currentBestLocation.accuracy).toInt()
        log("accuracyDelta: $accuracyDelta")
        val isLessAccurate = accuracyDelta > 0
        log("isLessAccurate: $isLessAccurate")
        val isMoreAccurate = accuracyDelta <= 0
        log("isMoreAccurate: $isMoreAccurate")
        val isSignificantlyLessAccurate = accuracyDelta > 200
        log("isSignificantlyLessAccurate: $isSignificantlyLessAccurate")

        // Check if the old and new location are from the same provider
        val isFromSameProvider = isSameProvider(
            location.provider,
            currentBestLocation.provider
        )

        log("isFromSameProvider: $isFromSameProvider")

        log("isMock: ${isMockSettingsON(this)} ${location.isFromMockProvider()}")


        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true
        } else if (isNewer && !isLessAccurate) {
            return true
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true
        }
        return false
    }

    /** Checks whether two providers are the same  */
    private fun isSameProvider(provider1: String?, provider2: String?): Boolean {
        return if (provider1 == null) {
            provider2 == null
        } else provider1 == provider2
    }

    override fun stopService(name: Intent?): Boolean {
        log("stopService")
        OPEN = false
        return super.stopService(name)
    }

    override fun onDestroy() {
        log("onDestroy")
        newShowNotification(false)
        super.onDestroy()
        OPEN = false
        removeLocationListeners()
    }

    private fun removeLocationListeners() {
        if (client != null && callback != null) {
            client!!.removeLocationUpdates(callback)
        }
    }

    private fun isMockSettingsON(context: Context): Boolean {
        // returns true if mock location enabled, false if not enabled.
        return !Settings.Secure.getString(
            context.getContentResolver(),
            Settings.Secure.ALLOW_MOCK_LOCATION
        ).equals("0")
    }


    private fun log(message: String) {
        Log.d("LOCATION_SERVICE", message)
    }


    companion object {
        var OPEN = false

        // LOCATION SETTINGS
        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        private var UPDATE_INTERVAL_IN_MILLISECONDS = 4L * 1000

        // Sets the min distance for active location updates
        private var LOCATION_DISTANCE = 3f

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        private var FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5L * 1000

        fun startLocationServices(applicationContext: Context) {
            Log.d("LOCATION_SERVICE", "startLocationServices")
            if (!OPEN) {
                val intentService = Intent(applicationContext, LocationService::class.java)
                applicationContext.startService(intentService)
            }
        }

        fun stopLocationServices(applicationContext: Context) {
            val intentService = Intent(applicationContext, LocationService::class.java)
            applicationContext.stopService(intentService)
        }
    }
}
