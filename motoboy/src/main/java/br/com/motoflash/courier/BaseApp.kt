package br.com.motoflash.courier

import android.app.Application
import android.content.ContextWrapper
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import br.com.motoflash.core.data.network.model.Session
import br.com.motoflash.core.data.network.model.User
import br.com.motoflash.core.data.network.model.UserDevice
import br.com.motoflash.core.ui.util.DEVICE_ID
import br.com.motoflash.core.ui.util.DEVICE_MESSAGE_ID
import br.com.motoflash.core.ui.util.HAS_USER
import br.com.motoflash.core.ui.util.USER_ID
import br.com.motoflash.courier.di.component.ApplicationComponent
import br.com.motoflash.courier.di.component.DaggerApplicationComponent
import br.com.motoflash.courier.di.module.ApplicationModule
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.pixplicity.easyprefs.library.Prefs

class BaseApp: Application() {


    lateinit var applicationComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        FirebaseApp.initializeApp(this)

        val deviceAppUID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()

        Prefs.putString(DEVICE_ID, deviceAppUID)
        Prefs.putBoolean(HAS_USER, false)

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            Prefs.putString(DEVICE_MESSAGE_ID, it.token)

            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
            FirebaseApp.initializeApp(this)

            val auth = FirebaseAuth.getInstance()

            val userDevice = UserDevice()
            userDevice.manufacturer = Build.MANUFACTURER
            userDevice.brand = Build.BRAND
            userDevice.deviceToken = it.token
            userDevice.sysVersion = Build.VERSION.SDK_INT.toString()
            userDevice.appVersion = BuildConfig.VERSION_CODE.toString()
            userDevice.model = Build.MODEL
            userDevice.uniqueId = deviceAppUID
            userDevice.userId = Prefs.getString(USER_ID,"")

            FirebaseFirestore.getInstance().collection("sessions").add(
                Session(
                    user = if(auth.currentUser!=null) User(email = auth.currentUser?.email, name = auth.currentUser?.displayName) else null,
                    device = userDevice,
                    timestamp = Timestamp.now()
                )
            )
        }

        applicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this)).build()
        applicationComponent.inject(this)
    }
}
