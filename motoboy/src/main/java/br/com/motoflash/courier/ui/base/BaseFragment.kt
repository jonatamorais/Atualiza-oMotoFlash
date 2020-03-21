package br.com.motoflash.courier.ui.base


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import br.com.motoflash.courier.BaseApp
import br.com.motoflash.courier.R
import br.com.motoflash.courier.di.component.DaggerFragmentComponent
import br.com.motoflash.courier.di.component.FragmentComponent
import br.com.motoflash.courier.di.module.FragmentModule
import br.com.motoflash.core.ui.util.showSnack
import com.google.firebase.analytics.FirebaseAnalytics

abstract class BaseFragment : Fragment(), MvpView {

    val firebaseAnalytics: FirebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(context!!)
    }

    private var viewContainer: View? = null
    val fragmentComponent: FragmentComponent by lazy {
        DaggerFragmentComponent.builder()
            .fragmentModule(FragmentModule(this))
            .applicationComponent((activity?.application as BaseApp).applicationComponent)
            .build()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        log("onCreateView")
        OPEN = true
        val view = super.onCreateView(inflater, container, savedInstanceState)
        this.viewContainer = view
        return view
    }

    override fun onError(message: String) {
        logError(message)
        showMessage(message)
    }

    override fun onError(@StringRes resId: Int) {
        onError(getString(resId))
    }

    override fun showMessage(message: String) {
        Toast.makeText(context!!, message, Toast.LENGTH_SHORT).show()
    }

    override fun showMessage(@StringRes resId: Int) {
        showMessage(getString(resId))
    }
    override fun showSnack(resId: Int) {
        showSnack(getString(resId))
    }

    override fun showSnack(message: String) {
        if(viewContainer!=null)
        message.showSnack(viewContainer!!.findViewById(R.id.container), backgroundColor = R.color.colorRed)
    }

    override fun onStart() {
        super.onStart()
        log("OnStart")
    }

    override fun onResume() {
        super.onResume()
        log("OnResume")
    }

    override fun onPause() {
        super.onPause()
        log("OnPause")
    }

    override fun onStop() {
        super.onStop()
        log("OnStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        log("onDestroy")
        OPEN = false
    }

    fun log(message: String){
        Log.d(TAG, message)
    }

    fun logError(message: String){
        Log.e(TAG, message)
    }

    fun logEvent(event: String, bundle: Bundle? = null) {
        firebaseAnalytics.logEvent(event, bundle)
    }

    fun getCustomTag() = TAG

    protected abstract fun setUp()

    val TAG: String
        get() = this::class.java.simpleName

    companion object{
        var OPEN = false
    }
}