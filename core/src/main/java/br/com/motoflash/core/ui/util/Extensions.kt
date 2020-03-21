package br.com.motoflash.core.ui.util

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat
import androidx.core.view.ViewPropertyAnimatorListener
import br.com.motoflash.core.R
import br.com.motoflash.core.data.network.model.Payment
import br.com.motoflash.core.data.network.model.WorkOrder
import br.com.motoflash.core.data.network.model.WorkOrderPoint
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson

fun TextView.toBold() {
    this.text = "<b>${this.text}</b>".toHtml()
}

fun Any.toJson() : String{
    return Gson().toJson(this)
}

fun String.toHtml() : Spanned{
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
       Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT)
    } else {
      Html.fromHtml(this)
    }
}

fun String.showSnack(v: View, duration: Int = 1, backgroundColor: Int = R.color.colorDark) {
    val snack = Snackbar.make(v, this,if(duration == 1)  Snackbar.LENGTH_LONG else  Snackbar.LENGTH_SHORT)
    val view = snack.view
    view.setBackgroundColor(ContextCompat.getColor(v.context, backgroundColor))
    val tv = view.findViewById(R.id.snackbar_text) as TextView
    tv.setTextColor(Color.WHITE)
    snack.show()
}

fun View.hideScale(){
    ViewCompat.animate(this)
        .scaleY(0f).scaleX(0f)
        .setDuration(500)
        .setListener(object : ViewPropertyAnimatorListener {
            override fun onAnimationEnd(view: View?) {
                view?.scaleY = 0f
                view?.scaleX = 0f
                this@hideScale.visibility = View.GONE
            }

            override fun onAnimationCancel(view: View?) {
            }

            override fun onAnimationStart(view: View?) {
            }

        })
        .setInterpolator(DecelerateInterpolator()).start()
}

fun View.showScale(){
    visibility = View.VISIBLE

    ViewCompat.animate(this)
        .scaleY(1f).scaleX(1f)
        .setDuration(500)
        .setListener(object : ViewPropertyAnimatorListener {
            override fun onAnimationEnd(view: View?) {
                visibility = View.VISIBLE
                view?.scaleY = 1f
                view?.scaleX = 1f
            }

            override fun onAnimationCancel(view: View?) {
            }

            override fun onAnimationStart(view: View?) {
            }

        })
        .setInterpolator(DecelerateInterpolator())
        .setInterpolator(DecelerateInterpolator()).start()
}

fun View.hideAlpha(delay: Long = 500L, startDelay: Long? = null, translate: Boolean = false){
        val animation = ViewCompat.animate(this)
            .alpha(0f)
            .setDuration(delay)
            .setListener(object : ViewPropertyAnimatorListener {
                override fun onAnimationEnd(view: View?) {
                    visibility = View.GONE
                    view?.alpha = 0f
                }

                override fun onAnimationCancel(view: View?) {
                }

                override fun onAnimationStart(view: View?) {
                }

            })
            .setInterpolator(DecelerateInterpolator())

        if (startDelay != null)
            animation.startDelay = startDelay

        if(translate)
            animation.translationY(-50f)

        animation.start()

}

fun View.showAlpha(delay: Long = 500L, startDelay: Long? = null, translate: Boolean = false){
        visibility = View.VISIBLE
        val animation = ViewCompat.animate(this)
            .alpha(1f)
            .setDuration(delay)
            .setListener(object : ViewPropertyAnimatorListener {
                override fun onAnimationEnd(view: View?) {
                    visibility = View.VISIBLE
                    view?.alpha = 1f
                }

                override fun onAnimationCancel(view: View?) {
                }

                override fun onAnimationStart(view: View?) {
                }

            })
            .setInterpolator(DecelerateInterpolator())
        if (startDelay != null)
            animation.startDelay = startDelay

        if(translate){
            animation.translationY(50f)
        }
        animation.start()
}

fun ViewGroup.showItemsWithMove(delay: Long = 300L){
    for (i in 0 until this.childCount) {

        val v = this.getChildAt(i)

        if (v !is ImageView && v !is LinearLayout) {
            val viewAnimator: ViewPropertyAnimatorCompat

            if (v !is Button) {
                viewAnimator = ViewCompat.animate(v)
                    .alpha(1f)
                    .setStartDelay(delay * i)
                    .setDuration(1000)
            } else {
                viewAnimator = ViewCompat.animate(v)
                    .scaleY(1f).scaleX(1f)
                    .setStartDelay(delay * i)
                    .setDuration(500)
            }

            viewAnimator.setInterpolator(DecelerateInterpolator()).start()
        }
    }
}

fun ViewGroup.showItems(delay: Long = 300L){
    for (i in 0 until this.childCount) {

        val v = this.getChildAt(i)

        if (v !is ImageView && v !is ProgressBar) {
            val viewAnimator: ViewPropertyAnimatorCompat

            if (v is Button) {
                viewAnimator = ViewCompat.animate(v)
                    .alpha(1f)
                    .setStartDelay(delay * i )
                    .setDuration(500)

                viewAnimator.setListener(object : ViewPropertyAnimatorListener{
                    override fun onAnimationCancel(view: View?) {}
                    override fun onAnimationStart(view: View?) {}
                    override fun onAnimationEnd(view: View?) {
                        view?.alpha = 1f
                    }
                })
            } else {
                viewAnimator = ViewCompat.animate(v)
                    .scaleY(1f).scaleX(1f)
                    .setStartDelay(delay * i)
                    .setDuration(1000)

                viewAnimator.setListener(object : ViewPropertyAnimatorListener{
                    override fun onAnimationCancel(view: View?) {}
                    override fun onAnimationStart(view: View?) {}
                    override fun onAnimationEnd(view: View?) {
                        view?.scaleY = 1f
                        view?.scaleX = 1f
                    }
                })
            }

            viewAnimator.setInterpolator(DecelerateInterpolator()).start()
        }
    }
}

fun ViewGroup.hideItems(delay: Long = 300L){
    val viewMain = this
    for (i in 0 until this.childCount) {

        val v = this.getChildAt(i)

        if (v !is ImageView && v !is ProgressBar) {
            val viewAnimator: ViewPropertyAnimatorCompat

            if (v is Button) {
                viewAnimator = ViewCompat.animate(v)
                    .alpha(0f)
                    .setStartDelay(delay * i)
                    .setDuration(500)
                viewAnimator.setListener(object : ViewPropertyAnimatorListener{
                    override fun onAnimationCancel(view: View?) {}
                    override fun onAnimationStart(view: View?) {}
                    override fun onAnimationEnd(view: View?) {
                        view?.alpha = 0f
                    }
                })
            } else {
                viewAnimator = ViewCompat.animate(v)
                    .scaleY(0f).scaleX(0f)
                    .setStartDelay(delay * i)
                    .setDuration(1000)

                viewAnimator.setListener(object : ViewPropertyAnimatorListener{
                    override fun onAnimationCancel(view: View?) {}
                    override fun onAnimationStart(view: View?) {}
                    override fun onAnimationEnd(view: View?) {
                        view?.scaleY = 0f
                        view?.scaleX = 0f
                    }
                })
            }

            if(i == childCount-1){
                viewAnimator.setListener(object : ViewPropertyAnimatorListener{
                    override fun onAnimationCancel(view: View?) {}
                    override fun onAnimationStart(view: View?) {}
                    override fun onAnimationEnd(view: View?) {
                        viewMain.visibility = View.GONE
                    }
                })
            }

            viewAnimator.setInterpolator(DecelerateInterpolator()).start()
        }
    }
}

fun View.animateHeight(height: Int, duration: Long = 300L){
    val anim = ValueAnimator.ofInt(this.measuredHeight, height)
    anim.addUpdateListener { valueAnimator ->
        val `val` = valueAnimator.animatedValue as Int
        val layoutParams = this.layoutParams
        layoutParams.height = `val`
        this.layoutParams = layoutParams
    }
    anim.duration = duration
    anim.start()
}

fun View.animateScale(height: Int, width: Int, duration: Long = 300L){
    val animH = ValueAnimator.ofInt(this.measuredHeight, height)
    animH.addUpdateListener { valueAnimator ->
        val `val` = valueAnimator.animatedValue as Int
        val layoutParams = this.layoutParams
        layoutParams.height = `val`
        this.layoutParams = layoutParams
    }
    animH.duration = duration
    animH.start()

    val animW = ValueAnimator.ofInt(this.measuredWidth, width)
    animW.addUpdateListener { valueAnimator ->
        val `val` = valueAnimator.animatedValue as Int
        val layoutParams = this.layoutParams
        layoutParams.width = `val`
        this.layoutParams = layoutParams
    }
    animW.duration = duration
    animW.start()
}

fun String.unMaskOnlyNumbers() : String {
    return this.replace("[^0-9]".toRegex(), "")
}

fun String.toBold() : Spanned {
    return "<b>${this}</b>".toHtml()
}

fun String.toHtmlColor(color: String) : Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml("<font color=$color>${this}</font>", Html.FROM_HTML_MODE_COMPACT)
    } else {
        Html.fromHtml(this)
    }
}

fun WorkOrder.Status.toLabel() : String {
    when(this){
        WorkOrder.Status.PENDING -> {
          return  "Pendente"
        }
        WorkOrder.Status.ASSIGNED -> {
            return  "Atribuído"
        }
        WorkOrder.Status.EXECUTION -> {
            return  "Em execução"
        }
        WorkOrder.Status.CANCELLED -> {
            return  "Cancelado"
        }
        WorkOrder.Status.FINISHED -> {
            return  "Finalizado"
        }
        WorkOrder.Status.ALL -> {
            return  "Todos"
        }
    }
}

fun WorkOrder.Status.getLabel(context: Context) : String {
    when(this){
        WorkOrder.Status.PENDING -> {
            return  context.getString(R.string.labelPending)
        }
        WorkOrder.Status.ASSIGNED -> {
            return  context.getString(R.string.labelAssigned)
        }
        WorkOrder.Status.EXECUTION -> {
            return  context.getString(R.string.labelExecution)
        }
        WorkOrder.Status.CANCELLED -> {
            return  context.getString(R.string.labelCanceled)
        }
        WorkOrder.Status.FINISHED -> {
            return  context.getString(R.string.labelFinished)
        }
        WorkOrder.Status.ALL -> {
            return "Todos"
        }
    }
}

fun WorkOrder.Status.getText(context: Context) : String {
    when(this){
        WorkOrder.Status.PENDING -> {
            return  context.getString(R.string.textPending)
        }
        WorkOrder.Status.ASSIGNED -> {
            return  context.getString(R.string.textAssigned)
        }
        WorkOrder.Status.EXECUTION -> {
            return  context.getString(R.string.textExecution)
        }
        WorkOrder.Status.CANCELLED -> {
            return  context.getString(R.string.textCanceled)
        }
        WorkOrder.Status.FINISHED -> {
            return  context.getString(R.string.textFinished)
        }
        WorkOrder.Status.ALL -> {
            return "All"
        }
    }
}

fun WorkOrder.Status.getColor(context: Context) : Int {
    when(this){
        WorkOrder.Status.PENDING -> {
            return  R.color.colorOrange
        }
        WorkOrder.Status.ASSIGNED -> {
            return  R.color.colorYellow
        }
        WorkOrder.Status.EXECUTION -> {
            return  R.color.colorGreen
        }
        WorkOrder.Status.CANCELLED -> {
            return  R.color.colorRed
        }
        WorkOrder.Status.FINISHED -> {
            return  R.color.colorGrayLightSuper
        }
        WorkOrder.Status.ALL -> {
            return  R.color.colorYellow
        }
    }
}

fun Payment.Status.getColor(context: Context) : Int {
    when(this){
        Payment.Status.PENDING -> {
            return  R.color.colorYellow
        }
        Payment.Status.PAY -> {
            return  R.color.colorGreen
        }
        Payment.Status.CANCELED -> {
            return  R.color.colorRed
        }
    }
}

fun Payment.Status.toLabel() : String {
    when(this){
        Payment.Status.PENDING -> {
            return  "Pendente"
        }
        Payment.Status.PAY -> {
            return  "Pago"
        }
        Payment.Status.CANCELED -> {
            return  "Cancelado"
        }
    }
}

fun WorkOrderPoint.Status.toLabel() : String {
    when(this){
        WorkOrderPoint.Status.PENDING -> {
            return  "Pendente"
        }
        WorkOrderPoint.Status.STARTED -> {
            return  "Iniciado"
        }
        WorkOrderPoint.Status.CHECKED_IN -> {
            return  "Checked In"
        }
        WorkOrderPoint.Status.CHECKED_OUT -> {
            return  "Checked Out"
        }
        WorkOrderPoint.Status.CANCELED -> {
            return  "Cancelado"
        }
    }
 }

fun WorkOrder.Priority.toLabel() : String {
    when(this){
        WorkOrder.Priority.NORMAL -> {
            return "Normal"
        }
        WorkOrder.Priority.URGENT -> {
            return "Urgente"
        }
    }
}