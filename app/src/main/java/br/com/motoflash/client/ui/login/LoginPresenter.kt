package br.com.motoflash.client.ui.login

import br.com.motoflash.client.ui.base.BasePresenter
import br.com.motoflash.core.data.network.model.ErrorCode
import br.com.motoflash.core.data.network.model.User
import br.com.motoflash.core.ui.util.ErrosUtil
import br.com.motoflash.core.ui.util.RxUtil
import com.google.firebase.FirebaseError
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import io.reactivex.observers.DisposableObserver
import io.reactivex.rxkotlin.plusAssign
import retrofit2.HttpException
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import android.icu.lang.UCharacter.GraphemeClusterBreak.V
import retrofit2.converter.gson.GsonConverterFactory
import android.icu.lang.UCharacter.GraphemeClusterBreak.V
import br.com.motoflash.core.BuildConfig
import br.com.motoflash.core.ui.util.DEVICE_ID
import com.pixplicity.easyprefs.library.Prefs
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory


class LoginPresenter<V :LoginMvpView> @Inject
constructor() : BasePresenter<V>(), LoginMvpPresenter<V> {
    override fun doCreatedUser(email: String, password: String, name: String, mobilePhone: String) {
        log("doCreatedUser")
        val body = HashMap<String, Any>()
        body["user"] = User(
            email = email,
            password = password,
            name = name,
            mobilePhone = mobilePhone
        )
        body["device"] = getUserDevice(Prefs.getString(DEVICE_ID, ""))

        compositeDisposable += api.doCreateUser(
            toBodyJsonObject(body)
        )
            .compose(RxUtil.applyNetworkSchedulers())
            .subscribe({
                log("subscribe: ${Gson().toJson(it)}")
                auth.signInWithCustomToken(it["userToken"].toString()).addOnSuccessListener {
                    mvpView?.run {
                        onCreatedUser()
                    }
                }.addOnFailureListener {
                    log("Error:${it.message}")
                    mvpView?.run {
                        onCreatedUserFail()
                    }
                }
            },{
                if(it is HttpException){
                    val error = ErrosUtil.getErrorCode(it)
                    log("field: ${error.field}")
                    mvpView?.run {
                        onCreatedUserInvalidCredentials(error)
                    }
                }else{
                    mvpView?.run {
                        onCreatedUserFail()
                    }
                }
            })
    }

    override fun doLoginUser(email: String, password: String) {
        log("doLoginUser")
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            mvpView?.run {
                onLoginUser()
            }
        }.addOnFailureListener {
            if (it is FirebaseAuthInvalidCredentialsException){
                mvpView?.run {
                    onLoginInvalidCredentials()
                }
            }
            else{
                mvpView?.run {
                    onLoginFailGeneric()
                }
            }
        }
    }

    override fun doVerifyUser(email: String) {
        log("doVerifyUser")
        auth.fetchSignInMethodsForEmail(email)
            .addOnSuccessListener {
                log("addOnSuccessListener:${it.signInMethods!!.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)}")
                mvpView?.run {
                    onVerfyUser(it.signInMethods!!.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD))
                }
            }
            .addOnFailureListener {
                log("addOnFailureListener:${it.message}")
                mvpView?.run {
                    onVerfyUser(false)
                }
            }
    }
}