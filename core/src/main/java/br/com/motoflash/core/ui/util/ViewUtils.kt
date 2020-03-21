package br.com.motoflash.core.ui.util

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.PorterDuff
import androidx.core.content.ContextCompat
import br.com.motoflash.core.R

/**
 * Created by lucascordeiro on 24/10/17.
 */
class ViewUtils {
    companion object {
        fun dpToPx(dp: Float): Int {
            val density = Resources.getSystem().displayMetrics.density
            return Math.round(dp * density)
        }

        fun pxToDp(px: Float): Int {
            val density = Resources.getSystem().displayMetrics.density
            return Math.round(px / density)
        }

        fun getConfirmDialog(mContext: Context, title: String, msg: String, positiveBtnCaption: String, negativeBtnCaption: String, isCancelable: Boolean, target: AlertInterface, icon: Int) {
            val builder = AlertDialog.Builder(mContext)

            var iconAlert = ContextCompat.getDrawable(mContext, icon)
            iconAlert = iconAlert!!.mutate()
            if (icon == R.drawable.ic_check_circle) {
                iconAlert!!.setColorFilter(ContextCompat.getColor(mContext, R.color.colorGreen), PorterDuff.Mode.MULTIPLY)
            } else {
                iconAlert!!.setColorFilter(ContextCompat.getColor(mContext, R.color.colorRed), PorterDuff.Mode.MULTIPLY)
            }

            builder.setTitle(title).setMessage(msg).setIcon(iconAlert).setCancelable(false).setPositiveButton(positiveBtnCaption) { dialog, id -> target.onPositiveClick(dialog, id) }.setNegativeButton(negativeBtnCaption) { dialog, id -> target.onNegativeClick(dialog, id) }

            val alert = builder.create()
            alert.setCancelable(isCancelable)
            alert.show()
            if (isCancelable) {
                alert.setOnCancelListener { dialog -> target.onNegativeClick(dialog, 0) }
            }
        }
    }
}