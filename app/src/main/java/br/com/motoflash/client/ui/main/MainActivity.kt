package br.com.motoflash.client.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import br.com.motoflash.client.R
import br.com.motoflash.client.ui.base.BaseActivity
import br.com.motoflash.client.ui.base.BaseFragment
import br.com.motoflash.client.ui.history.HistoryFragment
import br.com.motoflash.client.ui.home.HomeFragment
import br.com.motoflash.client.ui.profile.ProfileFragment
import br.com.motoflash.client.ui.splash.SplashActivity
import br.com.motoflash.core.data.network.model.User
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.firebase.ui.auth.AuthUI
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseActivity(),  MainMvpView{

    private var user: User? = null

    private var load = false

    @Inject
    lateinit var presenter: MainMvpPresenter<MainMvpView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activityComponent.inject(this)
        presenter.onAttach(this)

         setUp()
    }

    override fun setUp() {
        showLoading()

        presenter.doGetUser()
    }

    fun doOpenFragment(id: Int){
        nav_view.show(id)
        val fragment: BaseFragment = when(id){
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

    override fun onGetUser(user: User) {
        this.user = user
        hideLoading()

        if(!load){
            load = true

            nav_view.add(MeowBottomNavigation.Model(1, R.drawable.ic_history))
            nav_view.add(MeowBottomNavigation.Model(2, R.drawable.ic_home))
            nav_view.add(MeowBottomNavigation.Model(3, R.drawable.ic_profile))

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
    }

    override fun onLogoutUser() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(FirebaseAuth.getInstance().currentUser!!.uid)
        AuthUI.getInstance().signOut(this).addOnCompleteListener {
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }
    }

    fun getCurrentUser() : User? {
        return user
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
}
