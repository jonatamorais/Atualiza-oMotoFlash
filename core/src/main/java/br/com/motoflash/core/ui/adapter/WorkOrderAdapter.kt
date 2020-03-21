package br.com.motoflash.core.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import br.com.motoflash.core.R
import br.com.motoflash.core.data.network.model.Payment
import br.com.motoflash.core.data.network.model.WorkOrder
import br.com.motoflash.core.data.network.model.WorkOrderPoint
import br.com.motoflash.core.ui.util.Mask
import br.com.motoflash.core.ui.util.getColor
import br.com.motoflash.core.ui.util.getLabel
import br.com.motoflash.core.ui.util.toLabel
import kotlinx.android.synthetic.main.list_workorder.view.*
import kotlinx.android.synthetic.main.list_workorderpoint.view.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by lucascordeiro on 21/10/17.
 */
class WorkOrderAdapter(val callback: OnWorkOrderCallback, var list: MutableList<WorkOrder>?, val motoboy: Boolean = false) : RecyclerView.Adapter<WorkOrderAdapter.ViewHolder>() {

    fun clear(){
        list?.clear()
        notifyDataSetChanged()
    }
    override fun getItemCount() = list!!.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workOrder = list!![holder.adapterPosition]
        val view = holder.itemView
        view.setOnClickListener {
            callback.onWorkOrderClick(workOrder)
        }

        view.setOnLongClickListener {
            callback.onWorkOrderLongClick(workOrder)
            true
        }

        var status = WorkOrder.Status.PENDING

        try{
            status =  WorkOrder.Status.valueOf(workOrder.status!!)
        }catch (e: Exception){

        }

        view.txtStatus.backgroundTintList = ContextCompat.getColorStateList(view.context, status.getColor(view.context))
        view.txtStatus.text = status.toLabel()

        view.txtPrice.text =  String.format("Total: R$ %.2f", workOrder.quotation?.price?.toFloat())

        val addressFirst = workOrder.points!![0].address!!
        val addressSecond = workOrder.points!![workOrder.points!!.size - 1].address!!
        val sequenceSecond = workOrder.points!![workOrder.points!!.size - 1].sequence

        view.txtSequenceFirst.text = "1"
        view.txtAddressFirst.text = String.format("%s, %s",addressFirst.address1, addressFirst.number)

        view.txtSequenceSecond.text = sequenceSecond.toString()
        view.txtAddressSecond.text = String.format("%s, %s",addressSecond.address1, addressSecond.number)

        view.txtDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(workOrder.createdDate!!.seconds * 1000))

        if(motoboy){
            view.containerMotoboy.visibility = View.VISIBLE
            view.containerInfoClient.visibility = View.VISIBLE

            view.txtStatusPaymentCourier.backgroundTintList = ContextCompat.getColorStateList(view.context, Payment.Status.valueOf(workOrder.payment?.status!!).getColor(view.context))
            view.txtStatusPaymentCourier.text = Payment.Status.valueOf(workOrder.payment?.status!!).toLabel()

            view.txtPricePaymentCourier.text =  String.format("Total: R$ %.2f", workOrder.payment?.courierAmount?.toFloat())

            view.txtClientName.text = workOrder.user?.name
            if(workOrder.user?.mobilePhone != null){
                view.txtClientMobilePhone.visibility = View.VISIBLE
                view.txtClientMobilePhone.text = Mask.mask(Mask.MOBILE_PHONE_MASK, workOrder.user?.mobilePhone?.substring(3)?:"")
            }else{
                view.txtClientMobilePhone.visibility = View.GONE
            }
        }else{
            view.containerMotoboy.visibility = View.GONE
            view.containerInfoClient.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(
                parent.context
            ).inflate(R.layout.list_workorder, parent, false)
        )
    }

    override fun getItemId(position: Int): Long {
        return list!![position].hashCode().toLong()
    }

    interface OnWorkOrderCallback {
        fun onWorkOrderClick(workOrder: WorkOrder)
        fun onWorkOrderLongClick(workOrder: WorkOrder)
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}