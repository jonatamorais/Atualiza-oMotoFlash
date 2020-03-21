package br.com.motoflash.client.ui.base

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import br.com.motoflash.client.BuildConfig
import br.com.motoflash.core.data.network.api.ApiServiceInterface
import br.com.motoflash.core.data.network.model.UserDevice
import br.com.motoflash.core.ui.util.*
import io.reactivex.disposables.CompositeDisposable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.Gson
import com.pixplicity.easyprefs.library.Prefs
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import kotlin.collections.HashMap


/**
 * Base class that implements the Presenter interface and provides a base implementation for
 * onAttach() and onDetach(). It also handles keeping a reference to the mvpView that
 * can be accessed from the children classes by calling getMvpView().
 */
open class BasePresenter<V : MvpView>  :
    MvpPresenter<V> {

    val auth = FirebaseAuth.getInstance()
    val storage = FirebaseStorage.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val database = FirebaseDatabase.getInstance()

    var currentTokenId = ""

    val api: ApiServiceInterface = ApiServiceInterface.create()
    val compositeDisposable = CompositeDisposable()

    var mvpView: V? = null
        private set

    init {
        auth.addAuthStateListener {
            auth.currentUser?.let {
                it.getIdToken(true).addOnSuccessListener {result ->
                    result.token?.let{
                        currentTokenId = result.token!!
                        log("token: $currentTokenId")
                    }
                }
            }
        }
    }

    override fun onAttach(mvpView: V) {
        this.mvpView = mvpView
    }

    override fun onDetach() {
        mvpView = null
    }

    protected fun log(message: String) {
        Log.d(TAG, message)
    }

    protected fun getUserDevice(uniqueId: String) : UserDevice {
        val userDevice = UserDevice()
        userDevice.manufacturer = Build.MANUFACTURER
        userDevice.brand = Build.BRAND
        userDevice.deviceToken = Prefs.getString(DEVICE_MESSAGE_ID, "")
        userDevice.sysVersion = Build.VERSION.SDK_INT.toString()
        userDevice.appVersion = BuildConfig.VERSION_CODE.toString()
        userDevice.model = Build.MODEL
        userDevice.uniqueId = uniqueId
        userDevice.userId = Prefs.getString(USER_ID,"")
        return userDevice
    }


    protected fun toBodyJsonObject(body: HashMap<String, Any>): JsonObject{
        val jsonObject = JsonObject()

        for(key in body.keys){
            jsonObject.add(key, JsonParser().parse(body[key]!!.toJson()))
        }

        return jsonObject
    }

    override val TAG: String
        get() = this::class.java.simpleName

    val isViewAttached: Boolean
        get() = mvpView != null

}
