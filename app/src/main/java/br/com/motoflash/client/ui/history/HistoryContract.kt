package br.com.motoflash.client.ui.history

import br.com.motoflash.client.ui.base.MvpPresenter
import br.com.motoflash.client.ui.base.MvpView
import br.com.motoflash.core.data.network.model.WorkOrder
import com.google.firebase.auth.FirebaseUser

interface HistoryMvpPresenter<V :HistoryMvpView> : MvpPresenter<V> {
    fun doGetWorkOrders(userId: String)
}

interface HistoryMvpView : MvpView {
    fun onGetWorkOrders(workOrders: List<WorkOrder>)
}


