package br.com.motoflash.client.ui.home

import br.com.motoflash.client.ui.base.MvpPresenter
import br.com.motoflash.client.ui.base.MvpView
import br.com.motoflash.core.data.network.model.Quotation
import br.com.motoflash.core.data.network.model.WorkOrder
import br.com.motoflash.core.data.network.model.WorkOrderPoint
import com.google.firebase.auth.FirebaseUser

interface HomeMvpPresenter<V :HomeMvpView> : MvpPresenter<V> {
    fun doGetQuotation(points: List<WorkOrderPoint>, companyId: String)
    fun doCreateWorkOrder(userId: String, points: List<WorkOrderPoint>, quotation: Quotation, motorcycle: Boolean)
    fun doCancellWorkOrder(workOrderId: String)
}

interface HomeMvpView : MvpView {
    fun onGetQuotation(quotation: Quotation)
    fun onGetQuotationFail()
    fun onCreateWorkOrder()
    fun onSearchCourier(workOrderId: String)
    fun onCancellWorkOrder()
    fun onCancellWorkOrderFail()
    fun onNotFoundCourier()
    fun onCreateWorkOrderFail()
}


