package br.com.motoflash.core.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.motoflash.core.R
import br.com.motoflash.core.data.network.model.WorkOrderPoint
import kotlinx.android.synthetic.main.list_workorderpoint.view.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by lucascordeiro on 21/10/17.
 */
class WorkOrderPointsAdapter(val callback: OnWorkOrderPointCallback, var list: MutableList<WorkOrderPoint>?) : RecyclerView.Adapter<WorkOrderPointsAdapter.ViewHolder>() {

    fun clear(){
        list?.clear()
        notifyDataSetChanged()
    }
    override fun getItemCount() = list!!.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val point = list!![holder.adapterPosition]
        val view = holder.itemView
        view.setOnClickListener {
            callback.onWorkOrderPointClick(point)
        }

        view.setOnLongClickListener {
            callback.onWorkOrderPointLongClick(point)
            true
        }

        view.txtSequence.text = point.sequence.toString()
        view.txtAddress.text = if(point.address?.address1!=null) String.format("%s, %s",point.address?.address1, point.address?.number) else "Selecione um endereço"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(
                parent.context
            ).inflate(R.layout.list_workorderpoint, parent, false)
        )
    }

    override fun getItemId(position: Int): Long {
        return list!![position].hashCode().toLong()
    }

    interface OnWorkOrderPointCallback {
        fun onWorkOrderPointClick(workOrderPoint: WorkOrderPoint)
        fun onWorkOrderPointLongClick(workOrderPoint: WorkOrderPoint)
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}