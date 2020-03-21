package br.com.motoflash.courier.ui.workorder.alert

import br.com.motoflash.core.data.network.model.Courier
import br.com.motoflash.core.data.network.model.WorkOrder
import br.com.motoflash.courier.ui.base.MvpPresenter
import br.com.motoflash.courier.ui.base.MvpView


interface AlertMvpPresenter<V :AlertMvpView> : MvpPresenter<V> {
    fun doGetWorkOrder(workOrderId: String)
    fun doDenieWorkOrder(courierId: String, workOrderId: String)
    fun doAssignWorkOrder(courierId: String, workOrderId: String)
}

interface AlertMvpView : MvpView {
    fun onGetWorkOrder(workOrder: WorkOrder)
    fun onGetWorkOrderFail()
    fun onAssignWorkOrder()
    fun onAssignWorkOrderFail()
    fun onDeniedWorkOrder()
    fun onDeniedWorkOrderFail()
}


