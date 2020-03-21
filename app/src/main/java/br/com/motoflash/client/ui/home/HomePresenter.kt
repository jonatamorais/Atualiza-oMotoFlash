package br.com.motoflash.client.ui.home

import br.com.motoflash.client.ui.base.BasePresenter
import br.com.motoflash.core.data.network.model.Quotation
import br.com.motoflash.core.data.network.model.WorkOrder
import br.com.motoflash.core.data.network.model.WorkOrderPoint
import br.com.motoflash.core.ui.util.ErrosUtil
import br.com.motoflash.core.ui.util.RxUtil
import br.com.motoflash.core.ui.util.toJson
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import io.reactivex.rxkotlin.plusAssign
import retrofit2.HttpException
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap


class HomePresenter<V :HomeMvpView> @Inject
constructor() : BasePresenter<V>(), HomeMvpPresenter<V> {

    private var cancell = false
    override fun doGetQuotation(points: List<WorkOrderPoint>, companyId: String) {
        val body = HashMap<String, Any>()
        body["companyId"] = companyId
        body["points"] = points
        compositeDisposable += api
            .doCreateQuotation(
                accessToken = currentTokenId,
                body = toBodyJsonObject(body)
            )
            .compose(RxUtil.applyNetworkSchedulers())
            .subscribe({
                mvpView?.onGetQuotation(it["quotation"]!!)
            },{
                log("Error: ${it.message}")
                mvpView?.onGetQuotationFail()
            })
    }

    override fun doCreateWorkOrder(
        userId: String,
        points: List<WorkOrderPoint>,
        quotation: Quotation,
        motorcycle: Boolean
    ) {
        val body = HashMap<String, Any>()
        body["workOrder"] = WorkOrder(
            quotation = quotation,
            points = points,
            motorcycle = motorcycle,
            userId = userId
        )

        compositeDisposable += api
            .doCreateWorkOrder(
                accessToken = currentTokenId,
                body = toBodyJsonObject(body)
            )
            .compose(RxUtil.applyNetworkSchedulers())
//            .flatMap {
//                mvpView?.onSearchCourier(it["workOrder"]!!.id!!)
//                api.doRunQueue(
//                    accessToken = currentTokenId,
//                    workOrderId = it["workOrder"]!!.id!!
//                ).compose(RxUtil.applyNetworkSchedulers())
//            }
            .subscribe({
                if(!cancell)
                mvpView?.onCreateWorkOrder()
            },{
                if(!cancell){
                log("Error: ${it.message}")
                if(it is HttpException){
                    val error = ErrosUtil.getErrorCode(it)

                    log("error: $error")
                    if(error.type=="NOT_EXIST"){
                        mvpView?.onNotFoundCourier()
                    }
                }else{
                    mvpView?.onCreateWorkOrderFail()
                }
                }
            })
    }

    override fun doCancellWorkOrder(workOrderId: String) {
        compositeDisposable += api
            .doCancellWorkOrder(
                accessToken = currentTokenId,
                workOrderId = workOrderId
            )
            .compose(RxUtil.applyNetworkSchedulers())
            .subscribe({
                cancell = true
                mvpView?.onCancellWorkOrder()
            },{
                log("Error: ${it.message}")
                mvpView?.onCancellWorkOrderFail()
            })
    }
}