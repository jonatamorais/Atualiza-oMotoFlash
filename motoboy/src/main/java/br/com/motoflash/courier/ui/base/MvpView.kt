package br.com.motoflash.courier.ui.base

import androidx.annotation.StringRes

/**
 * Base interface that any class that wants to act as a View in the MVP (Model View Presenter)
 * pattern must implement. Generally this interface will be extended by a more specific interface
 * that then usually will be implemented by an Activity or Fragment.
 */
interface MvpView {
    fun onError(@StringRes resId: Int)
    fun onError(message: String)
    fun showMessage(message: String)
    fun showMessage(@StringRes resId: Int)
    fun showSnack(message: String)
    fun showSnack(@StringRes resId: Int)
}
