package br.com.motoflash.client.ui.detail

import br.com.motoflash.client.ui.base.BasePresenter
import br.com.motoflash.client.ui.home.HomeMvpPresenter
import br.com.motoflash.client.ui.home.HomeMvpView
import br.com.motoflash.core.data.network.model.Quotation
import br.com.motoflash.core.data.network.model.WorkOrder
import br.com.motoflash.core.data.network.model.WorkOrderPoint
import br.com.motoflash.core.ui.util.ErrosUtil
import br.com.motoflash.core.ui.util.RxUtil
import com.google.firebase.firestore.ListenerRegistration
import io.reactivex.rxkotlin.plusAssign
import retrofit2.HttpException
import javax.inject.Inject
import kotlin.collections.HashMap


class WorkOrderDetailPresenter<V : WorkOrderDetailMvpView> @Inject
constructor() : BasePresenter<V>(), WorkOrderDetailMvpPresenter<V> {

    private var removeListenerRegistration: ListenerRegistration? = null

    override fun doRunQueue(workOrderId: String) {
        compositeDisposable +=  api.doRunQueue(
            accessToken = currentTokenId,
            workOrderId = workOrderId
        )
            .compose(RxUtil.applyNetworkSchedulers())
            .subscribe({
                mvpView?.onRunQueue()
            },{
                log("Error: ${it.message}")
                if(it is HttpException){
                    val error = ErrosUtil.getErrorCode(it)

                    log("error: $error")
                    if(error.type=="NOT_EXIST"){
                        mvpView?.onNotFoundCourier()
                    }
                }else{
                    mvpView?.onRunQueueFail()
                }
            })
    }

    override fun doGetWorkOrder(workOrderId: String) {
        removeListenerRegistration = firestore.collection("workorders").document(workOrderId).addSnapshotListener { snapshot, exception ->
            if(exception==null && snapshot!=null){
                mvpView?.onGetWorkOrder(snapshot.toObject(WorkOrder::class.java)!!)
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        removeListenerRegistration?.remove()
    }
}