package br.com.motoflash.core.ui.dialog.types

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import br.com.motoflash.core.R
import kotlinx.android.synthetic.main.dialog_input_text.view.*

class InputTextDialog(val title: String, val label: String, val callback: DialogListener, val dark: Boolean) : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity?.layoutInflater?.inflate(R.layout.dialog_input_text, null)!!

        if(dark){
            view.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorGray))
        }else{
            view.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorWhite))
        }

        val edt = view.edtInput
        val button = view.btnSend

        view.txtTitle.text = title
        view.txtLabel.text = label

        val alert = AlertDialog.Builder(activity)
        alert.setView(view)
        button.setOnClickListener {
            callback.onButtonClick(edt.text.toString(), this)
        }

        edt.requestFocus()

        return alert.create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.edtInput.requestFocus()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    interface DialogListener{
        fun onButtonClick(text: String, dialogFragment: DialogFragment)
    }
}