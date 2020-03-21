package br.com.motoflash.courier.ui.login

import android.net.Uri
import br.com.motoflash.core.data.network.model.Courier
import br.com.motoflash.core.data.network.model.User
import br.com.motoflash.core.ui.util.ErrosUtil
import br.com.motoflash.core.ui.util.RxUtil
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.gson.Gson
import retrofit2.HttpException
import javax.inject.Inject
import kotlin.collections.HashMap
import br.com.motoflash.core.ui.util.DEVICE_ID
import br.com.motoflash.courier.ui.base.BasePresenter
import com.pixplicity.easyprefs.library.Prefs
import io.reactivex.rxkotlin.plusAssign


class LoginPresenter<V :LoginMvpView> @Inject
constructor() : BasePresenter<V>(), LoginMvpPresenter<V> {
    override fun doCreatedCourier(courier: Courier) {
        courier.active = true

        val refCourier = storage.getReferenceFromUrl("gs://motoflash-a2f12.appspot.com/").child("couriers/register")
        val fileDocument = Uri.parse(if(courier.cnh) courier.cnhDoc!! else courier.rgDoc)
        val refUploadDocument = refCourier.child(fileDocument.lastPathSegment!!)
        refUploadDocument.putFile(fileDocument).addOnSuccessListener {
            refUploadDocument.downloadUrl.addOnSuccessListener {
                if(courier.cnh)
                    courier.cnhDoc = it.toString()
                else
                    courier.rgDoc = it.toString()

                val body = HashMap<String, Any>()
                body["courier"] = courier
                body["device"] = getUserDevice(Prefs.getString(DEVICE_ID, ""))

                compositeDisposable += api.doCreateCourier(
                    toBodyJsonObject(body)
                )
                    .compose(RxUtil.applyNetworkSchedulers())
                    .subscribe({
                        log("subscribe: ${Gson().toJson(it)}")
                        auth.signInWithCustomToken(it["courierToken"].toString()).addOnSuccessListener {
                            mvpView?.run {
                                onCreatedCourier()
                            }
                        }.addOnFailureListener {
                            log("Error:${it.message}")
                            mvpView?.run {
                                onCreatedCourierFail()
                            }
                        }
                    },{
                        if(it is HttpException){
                            val error = ErrosUtil.getErrorCode(it)
                            log("field: ${error.field}")
                            mvpView?.run {
                                onCreatedCourierInvalidCredentials(error)
                            }
                        }else{
                            mvpView?.run {
                                onCreatedCourierFail()
                            }
                        }
                    })
            }.addOnFailureListener {

            }
        }.addOnFailureListener{
            log("Error:${it.message}")
            mvpView?.run {
                onCreatedCourierFail()
            }
        }
    }


    override fun doLoginCourier(email: String, password: String) {
        log("doLoginUser")
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            mvpView?.run {
                onLoginCourier()
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

    override fun doVerifyCourier(email: String) {
        log("doVerifyUser")
        auth.fetchSignInMethodsForEmail(email)
            .addOnSuccessListener {
                log("addOnSuccessListener:${it.signInMethods!!.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)}")
                mvpView?.run {
                    onVerfyCourier(it.signInMethods!!.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD))
                }
            }
            .addOnFailureListener {
                log("addOnFailureListener:${it.message}")
                mvpView?.run {
                    onVerfyCourier(false)
                }
            }
    }
}