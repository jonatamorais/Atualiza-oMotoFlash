package br.com.motoflash.courier.ui.splash

import br.com.motoflash.courier.ui.base.BasePresenter
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.util.*
import javax.inject.Inject


class SplashPresenter<V :SplashMvpView> @Inject
constructor() : BasePresenter<V>(), SplashMvpPresenter<V> {
    override fun doVerifyUser() {
        mvpView?.run {
            onVerfyUser(auth.currentUser!=null)
        }
    }
}