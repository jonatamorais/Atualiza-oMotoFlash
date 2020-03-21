package br.com.motoflash.core.ui.dialog.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.motoflash.core.R
import kotlinx.android.synthetic.main.list_simple_text.view.*

class SimpleTextAdapter(val callback: ClickCallback, var list: MutableList<SimpleText>) : RecyclerView.Adapter<SimpleTextAdapter.ViewHolder>() {

    fun clear(){
        list.clear()
        notifyDataSetChanged()
    }
    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val simpleText = list[holder.adapterPosition]
        val view = holder.itemView
        view.txtText.text = simpleText.text

        view.setOnClickListener {
            callback.onClick(
                holder.adapterPosition,
                simpleText)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_simple_text, parent, false))
    }

    override fun getItemId(position: Int): Long {
        return list[position].hashCode().toLong()
    }

    interface ClickCallback {
        fun onClick(position: Int, simpleText: SimpleText)
    }
    data class SimpleText(var id:String, var text: String)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}