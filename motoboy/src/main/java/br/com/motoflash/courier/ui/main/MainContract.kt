package br.com.motoflash.courier.ui.main

import br.com.motoflash.core.data.network.model.Courier
import br.com.motoflash.courier.ui.base.MvpPresenter
import br.com.motoflash.courier.ui.base.MvpView


interface MainMvpPresenter<V :MainMvpView> : MvpPresenter<V> {
    fun doGetCourier()
}

interface MainMvpView : MvpView {
    fun onGetCourier(courier: Courier)
    fun onLogoutCourier(message: String)
}


