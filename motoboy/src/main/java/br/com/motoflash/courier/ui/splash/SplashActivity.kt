package br.com.motoflash.courier.ui.splash

import android.content.ContextWrapper
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.view.ViewCompat
import br.com.motoflash.core.ui.util.*
import br.com.motoflash.courier.R
import br.com.motoflash.courier.ui.base.BaseActivity
import br.com.motoflash.courier.ui.login.LoginActivity
import br.com.motoflash.courier.ui.main.MainActivity
import com.google.firebase.iid.FirebaseInstanceId
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_splash.*
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

class SplashActivity : BaseActivity(), SplashMvpView {

    @Inject
    lateinit var presenter: SplashMvpPresenter<SplashMvpView>

    private val deviceAppUID by lazy {
        Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        activityComponent.inject(this)
        presenter.onAttach(this)

        setUp()
    }

    override fun setUp() {
        animate()

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
        }

        Timer("splash").schedule(1000L){
            runOnUiThread {
                presenter.doVerifyUser()
            }
        }

        for( i in 0 until container.childCount){
            log("item id: ${container.getChildAt(i).id}")
        }
    }

    override fun onVerfyUser(logged: Boolean) {
        log("Logado: $logged")
        if(logged){
            startActivity(Intent(this, MainActivity::class.java))
        }else{
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun animate() {

        ViewCompat.animate(imgLogo)
            .translationY(-CommonsUtil.convertPxToDp(this, 70f))
            .setStartDelay(250L)
            .setDuration(600L).setInterpolator(
                DecelerateInterpolator(1.5f)
            ).start()

        container.showItemsWithMove()


    }

    override fun hideLoading() {

    }

    override fun showLoading() {

    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetach()
    }
}