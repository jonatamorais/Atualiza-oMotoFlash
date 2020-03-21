package br.com.motoflash.courier.ui.login

import br.com.motoflash.core.data.network.model.Courier
import br.com.motoflash.core.data.network.model.ErrorCode
import br.com.motoflash.courier.ui.base.MvpPresenter
import br.com.motoflash.courier.ui.base.MvpView

interface LoginMvpPresenter<V :LoginMvpView> : MvpPresenter<V> {
    fun doVerifyCourier(email: String)
    fun doLoginCourier(email: String, password: String)
    fun doCreatedCourier(courier: Courier)
}

interface LoginMvpView : MvpView {
    fun onVerfyCourier(exists: Boolean)
    fun onLoginCourier()
    fun onLoginFailGeneric()
    fun onLoginInvalidCredentials()
    fun onCreatedCourier()
    fun onCreatedCourierFail()
    fun onCreatedCourierInvalidCredentials(errorCode: ErrorCode)
}


