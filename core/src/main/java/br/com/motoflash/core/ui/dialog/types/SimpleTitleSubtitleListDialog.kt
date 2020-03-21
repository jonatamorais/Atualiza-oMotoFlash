package br.com.motoflash.core.ui.dialog.types

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.motoflash.core.R
import br.com.motoflash.core.ui.dialog.adapter.SimpleTitleSubtitleAdapter
import br.com.motoflash.core.ui.util.showSnack
import kotlinx.android.synthetic.main.dialog_list_text.view.*

class SimpleTitleSubtitleListDialog(val title: String, val label: String, val callback: DialogListener, val list: List<SimpleTitleSubtitleAdapter.SimpleTitle>, val dark: Boolean) : DialogFragment() {

    var container: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity?.layoutInflater?.inflate(R.layout.dialog_list_text, null)!!

        if(dark){
            view.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorGray))
        }else{
            view.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorWhite))
        }

        view.txtTitle.text = title
        view.txtLabel.text = label

        view.recyclerView.layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)

        val adapter = SimpleTitleSubtitleAdapter(object : SimpleTitleSubtitleAdapter.ClickCallback{
            override fun onClick(position: Int, simpleText: SimpleTitleSubtitleAdapter.SimpleTitle) {
                callback.onTextSelect(simpleText.id, this@SimpleTitleSubtitleListDialog)
            }

        }, list.toMutableList())

        view.recyclerView.adapter = adapter

        container = view.container

        val alert = AlertDialog.Builder(activity)
        alert.setView(view)

        return alert.create()
    }

    fun showError(message: String){
        message.showSnack(container!!, backgroundColor = R.color.colorRed)
    }

    interface DialogListener{
        fun onTextSelect(id: String, dialogFragment: DialogFragment)
    }
}