package br.com.motoflash.client.ui.detail

import br.com.motoflash.client.ui.base.MvpPresenter
import br.com.motoflash.client.ui.base.MvpView
import br.com.motoflash.core.data.network.model.Quotation
import br.com.motoflash.core.data.network.model.WorkOrder
import br.com.motoflash.core.data.network.model.WorkOrderPoint
import com.google.firebase.auth.FirebaseUser

interface WorkOrderDetailMvpPresenter<V :WorkOrderDetailMvpView> : MvpPresenter<V> {
    fun doGetWorkOrder(workOrderId: String)
    fun doRunQueue(workOrderId: String)
}

interface WorkOrderDetailMvpView : MvpView {
    fun onGetWorkOrder(workOrder: WorkOrder)
    fun onRunQueueFail()
    fun onNotFoundCourier()
    fun onRunQueue()
}


