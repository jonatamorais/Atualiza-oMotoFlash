package br.com.motoflash.core.ui.dialog.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.motoflash.core.R
import kotlinx.android.synthetic.main.dialog_list_text.view.txtTitle
import kotlinx.android.synthetic.main.list_simple_text_subtitle.view.*

class SimpleTitleSubtitleAdapter(val callback: ClickCallback, var list: MutableList<SimpleTitle>) : RecyclerView.Adapter<SimpleTitleSubtitleAdapter.ViewHolder>() {

    fun clear(){
        list.clear()
        notifyDataSetChanged()
    }
    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val simpleText = list[holder.adapterPosition]
        val view = holder.itemView
        view.txtTitle.text = simpleText.title
        view.txtSubtitle.text = simpleText.subTitle

        view.setOnClickListener {
            callback.onClick(
                holder.adapterPosition,
                simpleText)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_simple_text_subtitle,
                parent,
                false
            )
        )
    }

    override fun getItemId(position: Int): Long {
        return list[position].hashCode().toLong()
    }

    interface ClickCallback {
        fun onClick(position: Int, simpleTitle: SimpleTitle)
    }
    data class SimpleTitle(var id:String, var title: String, var subTitle: String)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}