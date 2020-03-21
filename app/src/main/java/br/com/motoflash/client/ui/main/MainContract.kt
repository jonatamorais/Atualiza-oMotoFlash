package br.com.motoflash.client.ui.main

import br.com.motoflash.client.ui.base.MvpPresenter
import br.com.motoflash.client.ui.base.MvpView
import br.com.motoflash.core.data.network.model.User
import com.google.firebase.auth.FirebaseUser

interface MainMvpPresenter<V :MainMvpView> : MvpPresenter<V> {
    fun doGetUser()
}

interface MainMvpView : MvpView {
    fun onGetUser(user: User)
    fun onLogoutUser()
}


