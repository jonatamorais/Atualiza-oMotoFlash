package br.com.motoflash.courier.ui.history

import br.com.motoflash.core.data.network.model.WorkOrder
import br.com.motoflash.courier.ui.base.BasePresenter
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class HistoryPresenter<V :HistoryMvpView> @Inject
constructor() : BasePresenter<V>(), HistoryMvpPresenter<V> {
    private var removeListenerRegistration: ListenerRegistration? = null
    override fun doGetWorkOrders(courierId: String) {
    removeListenerRegistration = firestore.collection("workorders").orderBy("createdDate", Query.Direction.DESCENDING).whereEqualTo("courier.id",courierId).addSnapshotListener { querySnapshot, e ->
            if(e==null){
                val list = querySnapshot!!.documents.map { it.toObject(WorkOrder::class.java)!!.apply {
                    id = it.id
                } }
                mvpView?.onGetWorkOrders(list)
            }else{
                log("Error: ${e.message}")
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        removeListenerRegistration?.remove()
    }
}