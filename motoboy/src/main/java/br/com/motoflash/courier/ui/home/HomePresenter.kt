package br.com.motoflash.courier.ui.home

import br.com.motoflash.core.data.network.model.WorkOrder
import br.com.motoflash.core.data.network.model.WorkOrderPoint
import br.com.motoflash.core.ui.util.RxUtil
import br.com.motoflash.courier.ui.base.BasePresenter
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import io.reactivex.rxkotlin.plusAssign
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class HomePresenter<V :HomeMvpView> @Inject
constructor() : BasePresenter<V>(), HomeMvpPresenter<V> {
    private var removeListenerRegistrationAssign: ListenerRegistration? = null
    private var removeListenerRegistrationExecution: ListenerRegistration? = null
    private var cancell = false

    override fun doSetRunningFalse(courierId: String) {
        firestore
            .collection("couriers")
            .document(courierId)
            .update("running",false)
    }

    override fun doGetCurrentWorkOrder(courierId: String) {
        mvpView?.onGetEmptyWorkOrder()

        removeListenerRegistrationAssign = firestore
            .collection("workorders")
            .whereEqualTo("courier.id", courierId)
            .whereEqualTo("status", "ASSIGNED")
            .orderBy("createdDate", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, exception ->
                if(exception != null){
                    log("Error: ${exception.message}")
                }else{
                    if(querySnapshot != null && querySnapshot.documents.size >= 1){
                        mvpView?.onGetCurrentWorkOrder(querySnapshot.documents[0].toObject(WorkOrder::class.java)!!)
                    }
                }
            }

        removeListenerRegistrationExecution = firestore
            .collection("workorders")
            .whereEqualTo("courier.id", courierId)
            .whereEqualTo("status", "EXECUTION")
            .orderBy("createdDate", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, exception ->
                if(exception != null){
                    log("Error: ${exception.message}")
                }else{
                    if(querySnapshot != null && querySnapshot.documents.size >= 1){
                        mvpView?.onGetCurrentWorkOrder(querySnapshot.documents[0].toObject(WorkOrder::class.java)!!)
                    }
                }
            }
    }

    override fun doStartWorkOrder(courierId: String, workOrderId: String, workOrder: WorkOrder) {
        val newPoints = workOrder.points!!


        val workOrderHash = HashMap<String,Any>()
        workOrderHash["status"] = WorkOrder.Status.EXECUTION
        firestore
            .collection("workorders")
            .document(workOrderId)
            .update(workOrderHash)
            .addOnSuccessListener {
                mvpView?.onStartWorkOrder()
            }
            .addOnFailureListener {
                log("Error: ${it.message}")
                mvpView?.onStartWorkOrderFail()
            }
    }

    override fun doStartPoint(
        courierId: String,
        workOrderId: String,
        workOrderPointId: String,
        workOrder: WorkOrder
    ) {
        val newPoints: MutableList<WorkOrderPoint> = ArrayList()

        workOrder.points!!.forEach {point ->
            if(point.id == workOrderPointId){
                newPoints.add(point.apply {
                    status = WorkOrderPoint.Status.STARTED.name
                })
            }else{
                newPoints.add(point)
            }
        }

        val workOrderHash = HashMap<String,Any>()
        workOrderHash["points"] = newPoints


        firestore
            .collection("workorders")
            .document(workOrderId)
            .update(workOrderHash)
            .addOnSuccessListener {
                mvpView?.onStartPoint()
            }
            .addOnFailureListener {
                log("Error: ${it.message}")
                mvpView?.onStartPointFail()
            }
    }

    override fun doFinishPoint(
        courierId: String,
        workOrderId: String,
        workOrderPointId: String,
        workOrder: WorkOrder
    ) {
        val newPoints: MutableList<WorkOrderPoint> = ArrayList()

        workOrder.points!!.forEach {point ->
            if(point.id == workOrderPointId){
                newPoints.add(point.apply {
                    status = WorkOrderPoint.Status.CHECKED_OUT.name
                })
            }else{
                newPoints.add(point)
            }
        }
        val lastPoint = newPoints.none { it.status != WorkOrderPoint.Status.CHECKED_OUT.name }

        val workOrderHash = HashMap<String,Any>()
        workOrderHash["points"] = newPoints
        if(lastPoint){
            workOrderHash["status"] = WorkOrder.Status.FINISHED
            firestore
                .collection("couriers")
                .document(courierId)
                .update("running",false)
        }

        firestore
            .collection("workorders")
            .document(workOrderId)
            .update(workOrderHash)
            .addOnSuccessListener {
                mvpView?.onFinishPoint(lastPoint)
            }
            .addOnFailureListener {
                log("Error: ${it.message}")
                mvpView?.onFinishPointFail()
            }
    }

    override fun onDetach() {
        super.onDetach()
        removeListenerRegistrationAssign?.remove()
        removeListenerRegistrationExecution?.remove()
    }

    override fun doCancellWorkOrder(courierId: String, workOrderId: String) {
        compositeDisposable += api
            .doDeniedCourier(
                accessToken = currentTokenId,
                courierId = courierId,
                workOrderId = workOrderId
            )
            .compose(RxUtil.applyNetworkSchedulers())
            .subscribe({
                mvpView?.onCancellWorkOrder()
            },{
                log("Error: ${it.message}")
                mvpView?.onCancellWorkOrderFail()
            })
    }

}