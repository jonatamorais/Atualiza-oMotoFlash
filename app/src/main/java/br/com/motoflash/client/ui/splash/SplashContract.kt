package br.com.motoflash.client.ui.splash

import br.com.motoflash.client.ui.base.MvpPresenter
import br.com.motoflash.client.ui.base.MvpView
import com.google.firebase.auth.FirebaseUser

interface SplashMvpPresenter<V :SplashMvpView> : MvpPresenter<V> {
    fun doVerifyUser()
}

interface SplashMvpView : MvpView {
    fun onVerfyUser(logged: Boolean)
}


