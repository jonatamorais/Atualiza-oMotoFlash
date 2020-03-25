package br.com.motoflash.courier.ui.home

import br.com.motoflash.core.data.network.model.WorkOrder
import br.com.motoflash.courier.ui.base.MvpPresenter
import br.com.motoflash.courier.ui.base.MvpView


interface HomeMvpPresenter<V :HomeMvpView> : MvpPresenter<V> {
    fun doGetCurrentWorkOrder(courierId: String)
    fun doSetRunningFalse(courierId: String)
    fun doStartWorkOrder(courierId: String, workOrderId: String, workOrder: WorkOrder)
    fun doStartPoint(courierId: String, workOrderId: String, workOrderPointId: String, workOrder: WorkOrder)
    fun doFinishPoint(courierId: String, workOrderId: String, workOrderPointId: String, workOrder: WorkOrder)
    fun doCancellWorkOrder(courierId: String, workOrderId: String)
}

interface HomeMvpView : MvpView {
    fun onGetCurrentWorkOrder(workOrder: WorkOrder)
    fun onGetEmptyWorkOrder()
    fun onStartWorkOrder()
    fun onStartWorkOrderFail()
    fun onStartPoint()
    fun onStartPointFail()
    fun onFinishPoint(lastPoint: Boolean)
    fun onFinishPointFail()
    fun onCancellWorkOrder()
    fun onCancellWorkOrderFail()
}


