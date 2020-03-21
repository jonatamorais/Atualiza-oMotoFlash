package br.com.motoflash.courier.ui.history

import br.com.motoflash.core.data.network.model.WorkOrder
import br.com.motoflash.courier.ui.base.MvpPresenter
import br.com.motoflash.courier.ui.base.MvpView


interface HistoryMvpPresenter<V :HistoryMvpView> : MvpPresenter<V> {
    fun doGetWorkOrders(courierId: String)
}

interface HistoryMvpView : MvpView {
    fun onGetWorkOrders(workOrders: List<WorkOrder>)
}


