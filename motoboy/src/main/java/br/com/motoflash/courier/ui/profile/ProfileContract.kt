package br.com.motoflash.courier.ui.profile

import br.com.motoflash.core.data.network.model.Courier
import br.com.motoflash.courier.ui.base.MvpPresenter
import br.com.motoflash.courier.ui.base.MvpView


interface ProfileMvpPresenter<V :ProfileMvpView> : MvpPresenter<V> {
    fun doUpdateOnline(courierId: String, online: Boolean)
    fun doUpdateProfilePhoto(courierId: String, courierPhoto: String)
}

interface ProfileMvpView : MvpView {
    fun onUpdateOnline()
    fun onUpdateFail()
    fun onUpdateInvalidCourier()
    fun onUpdateProfilePhoto()
    fun onUpdateProfilePhotoFail()
}


