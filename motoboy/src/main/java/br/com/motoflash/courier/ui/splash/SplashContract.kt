package br.com.motoflash.courier.ui.splash

import br.com.motoflash.courier.ui.base.MvpPresenter
import br.com.motoflash.courier.ui.base.MvpView


interface SplashMvpPresenter<V :SplashMvpView> : MvpPresenter<V> {
    fun doVerifyUser()
}

interface SplashMvpView : MvpView {
    fun onVerfyUser(logged: Boolean)
}


