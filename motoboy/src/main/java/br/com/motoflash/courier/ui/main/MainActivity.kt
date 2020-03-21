package br.com.motoflash.courier.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import br.com.motoflash.core.data.network.model.Courier
import br.com.motoflash.core.ui.util.COURIER_ID
import br.com.motoflash.core.ui.util.COURIER_ONLINE
import br.com.motoflash.courier.R
import br.com.motoflash.courier.service.location.LocationService
import br.com.motoflash.courier.ui.base.BaseActivity
import br.com.motoflash.courier.ui.base.BaseFragment
import br.com.motoflash.courier.ui.history.HistoryFragment
import br.com.motoflash.courier.ui.home.HomeFragment
import br.com.motoflash.courier.ui.profile.ProfileFragment
import br.com.motoflash.courier.ui.splash.SplashActivity
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.firebase.ui.auth.AuthUI
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.pixplicity.easyprefs.library.Prefs
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseActivity(), MainMvpView {

    private var courierCallback: OnCourierCallback? = null

    @Inject
    lateinit var presenter: MainMvpPresenter<MainMvpView>

    private var courier: Courier? = null

    private var load = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activityComponent.inject(this)
        presenter.onAttach(this)

        setUp()
    }

    @SuppressLint("CheckResult")
    override fun setUp() {
        Prefs.putString(COURIER_ID, FirebaseAuth.getInstance().uid)
        showLoading()

        RxPermissions(this)
            .request(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .subscribe { granted ->
                if(granted){

                }else{
                    finish()
                }
            }

        nav_view.add(MeowBottomNavigation.Model(1, R.drawable.ic_history))
        nav_view.add(MeowBottomNavigation.Model(2, R.drawable.ic_home))
        nav_view.add(MeowBottomNavigation.Model(3, R.drawable.ic_profile))

        presenter.doGetCourier()
    }

    override fun onGetCourier(courier: Courier) {
        this.courier = courier

        hideLoading()

        nav_view.setOnClickMenuListener{
            val fragment: BaseFragment = when(it.id){
                1 -> {
                    HistoryFragment()
                }
                2 -> {
                    HomeFragment()
                }
                3 -> {
                    ProfileFragment()
                }
                else -> {
                    ProfileFragment()
                }
            }

            val fragmentTransaction = supportFragmentManager.beginTransaction().replace(
                frame.id,
                fragment
            )
                .addToBackStack(fragment.getCustomTag())
            fragmentTransaction.commit()
        }

        if(courier.online){
            LocationService.startLocationServices(this)
            Prefs.putBoolean(COURIER_ONLINE, true)
        }
        else{
            LocationService.stopLocationServices(this)
            Prefs.putBoolean(COURIER_ONLINE, false)
        }


        if(!load){
            load = true

            Places.initialize(applicationContext, getString(R.string.google_maps_key))

            FirebaseMessaging.getInstance().subscribeToTopic(FirebaseAuth.getInstance().currentUser!!.uid)

            val fragment = HomeFragment()
            nav_view.show(2)
            val fragmentTransaction = supportFragmentManager.beginTransaction().replace(
                frame.id,
                fragment
            )
                .addToBackStack(fragment.getCustomTag())
            fragmentTransaction.commit()

        }
        courierCallback?.onGetCourier(courier)
    }

    fun setCallback(courierCallback: OnCourierCallback){
        this.courierCallback = courierCallback
    }

    fun removeCallback(){
        courierCallback = null
    }

    fun doGetCourier() : Courier? {
        return courier
    }

    override fun onLogoutCourier(message: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(FirebaseAuth.getInstance().currentUser!!.uid)
        AuthUI.getInstance().signOut(this).addOnCompleteListener {
            showToast(message)
            FirebaseAuth.getInstance().signOut()
            finish()
        }
    }

    override fun hideLoading() {
        progress.visibility = View.GONE
    }

    override fun showLoading() {
        progress.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetach()
    }

    interface OnCourierCallback {
        fun onGetCourier(courier: Courier)
    }
}
