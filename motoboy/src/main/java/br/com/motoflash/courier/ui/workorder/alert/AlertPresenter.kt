package br.com.motoflash.courier.ui.workorder.alert

import android.net.Uri
import br.com.motoflash.core.data.network.model.Courier
import br.com.motoflash.core.data.network.model.WorkOrder
import br.com.motoflash.core.ui.util.DEVICE_ID
import br.com.motoflash.core.ui.util.RxUtil
import br.com.motoflash.courier.ui.base.BasePresenter
import com.pixplicity.easyprefs.library.Prefs
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class AlertPresenter<V : AlertMvpView> @Inject
constructor() : BasePresenter<V>(), AlertMvpPresenter<V> {
    override fun doGetWorkOrder(workOrderId: String) {
        firestore
            .collection("workorders")
            .document(workOrderId)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if(firebaseFirestoreException != null){
                    log("Error: ${firebaseFirestoreException.message}")
                    mvpView?.onGetWorkOrderFail()
                }else{
                    if(documentSnapshot != null && documentSnapshot.exists()){
                        val workOrder = documentSnapshot.toObject(WorkOrder::class.java)!!
                        workOrder.id = documentSnapshot.id
                        mvpView?.onGetWorkOrder(workOrder)
                    }else{
                        mvpView?.onGetWorkOrderFail()
                    }
                }
            }
    }

    override fun doAssignWorkOrder(courierId: String, workOrderId: String) {
        compositeDisposable += api
            .doAssingCourier(
                accessToken = currentTokenId,
                courierId = courierId,
                workOrderId = workOrderId
            )
            .compose(RxUtil.applyNetworkSchedulers())
            .subscribe({
                mvpView?.onAssignWorkOrder()
            },{
                mvpView?.onAssignWorkOrderFail()
            })
    }

    override fun doDenieWorkOrder(courierId: String, workOrderId: String) {
        compositeDisposable += api
            .doDeniedCourier(
                accessToken = currentTokenId,
                courierId = courierId,
                workOrderId = workOrderId
            )
            .compose(RxUtil.applyNetworkSchedulers())
            .subscribe({
                mvpView?.onDeniedWorkOrder()
            },{
                log("Error: ${it.message}")
                mvpView?.onDeniedWorkOrderFail()
            })
    }
}