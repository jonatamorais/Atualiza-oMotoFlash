package br.com.motoflash.core.ui.util

import android.content.Context
import android.text.Html
import androidx.appcompat.app.AlertDialog

class AlertUtil {
    companion object{
        fun showAlertNeutral(context: Context, title: String, message: String){
            val dialog = AlertDialog.Builder(context)
            dialog.setTitle(Html.fromHtml("<font color=#F27173>$title</font>"))
            dialog.setMessage(message)
            dialog.setNeutralButton("Ok", null)
            dialog.show()
        }

        fun showAlertNeutralWithCallback(context: Context, title: String, message: String, callback: AlertDialogListener){
            val dialog = AlertDialog.Builder(context)
            dialog.setTitle(Html.fromHtml("<font color=#F27173>$title</font>"))
            dialog.setMessage(message)
            dialog.setOnDismissListener {
                callback.onDismiss()
            }
            dialog.setNeutralButton("Ok", null)
            dialog.show()
        }
    }

    interface AlertDialogListener {
        fun onDismiss(){

        }
    }
}