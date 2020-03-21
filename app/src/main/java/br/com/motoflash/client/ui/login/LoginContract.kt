package br.com.motoflash.client.ui.login

import br.com.motoflash.client.ui.base.MvpPresenter
import br.com.motoflash.client.ui.base.MvpView
import br.com.motoflash.core.data.network.model.ErrorCode
import com.google.firebase.auth.FirebaseUser

interface LoginMvpPresenter<V :LoginMvpView> : MvpPresenter<V> {
    fun doVerifyUser(email: String)
    fun doLoginUser(email: String, password: String)
    fun doCreatedUser(email: String, password: String, name: String, mobilePhone: String)
}

interface LoginMvpView : MvpView {
    fun onVerfyUser(exists: Boolean)
    fun onLoginUser()
    fun onLoginFailGeneric()
    fun onLoginInvalidCredentials()
    fun onCreatedUser()
    fun onCreatedUserFail()
    fun onCreatedUserInvalidCredentials(errorCode: ErrorCode)
}


