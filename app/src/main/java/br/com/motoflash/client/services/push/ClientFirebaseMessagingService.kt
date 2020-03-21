package br.com.motoflash.client.services.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import br.com.motoflash.client.R
import br.com.motoflash.client.ui.splash.SplashActivity
import br.com.motoflash.core.ui.util.PUSH_MESSAGE
import br.com.motoflash.core.ui.util.PUSH_TITLE
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson

class ClientFirebaseMessagingService : FirebaseMessagingService() {
    // [START receive_message]
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "New Message ${Gson().toJson(message)}")
        notifyUser(message.data[PUSH_TITLE], message.data[PUSH_MESSAGE], 1)
    }

    fun notifyUser(title: String?, message: String?, pushId: Int) {

        val CHANNEL_ID = "motoflash_clientes"// The id of the channel.

        val startIntent = Intent(this, SplashActivity::class.java)

        val startPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(startIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }


        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
                    .setBigContentTitle(title))
            .setContentIntent(startPendingIntent)
            .setLights(Color.argb(100, 255, 48, 55), 5000, 5000)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setSmallIcon(R.drawable.motoflash_purple)
            .setVibrate(longArrayOf(100, 100))

        builder.color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            resources.getColor(R.color.colorAccent, theme)
        else
            resources.getColor(R.color.colorAccent)

        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Motoflash"// The user-visible name of the channel.
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            builder.setChannelId(CHANNEL_ID)
            mNotificationManager.createNotificationChannel(mChannel)
        }


        mNotificationManager.notify(pushId, builder.build())
    }

    companion object{
        const val TAG = "PUSH"
    }
}