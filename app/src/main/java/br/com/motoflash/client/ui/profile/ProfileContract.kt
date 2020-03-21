package br.com.motoflash.client.ui.profile

import br.com.motoflash.client.ui.base.MvpPresenter
import br.com.motoflash.client.ui.base.MvpView
import br.com.motoflash.core.data.network.model.ErrorCode
import br.com.motoflash.core.data.network.model.User
import com.google.firebase.auth.FirebaseUser

interface ProfileMvpPresenter<V :ProfileMvpView> : MvpPresenter<V> {
    fun doSingOutUser()
    fun doUpdateProfile(userId: String, name: String, email: String, profilePhoto: String, mobilePhone: String, updatePhoto: Boolean)
    fun doUpdatePassword(userId: String, password: String)
}

interface ProfileMvpView : MvpView {
    fun onSingOutUser()
    fun onSingOutFail()
    fun onUpdateProfile()
    fun onUpdateProfileFail()
    fun onUpdatePassword()
    fun onUpdatePasswordFail()
    fun onUpdateProfileError(errorCode: ErrorCode)
}


