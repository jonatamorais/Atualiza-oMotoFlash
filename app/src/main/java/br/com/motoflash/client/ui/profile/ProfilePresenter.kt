package br.com.motoflash.client.ui.profile

import android.net.Uri
import br.com.motoflash.client.ui.base.BasePresenter
import br.com.motoflash.core.data.network.model.User
import br.com.motoflash.core.ui.util.ErrosUtil
import br.com.motoflash.core.ui.util.RxUtil
import com.firebase.ui.auth.AuthUI
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import io.reactivex.rxkotlin.plusAssign
import retrofit2.HttpException
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap


class ProfilePresenter<V :ProfileMvpView> @Inject
constructor() : BasePresenter<V>(), ProfileMvpPresenter<V> {
    override fun doSingOutUser() {
        auth.signOut()
        mvpView?.run {
            onSingOutUser()
        }
    }

    override fun doUpdateProfile(
        userId: String,
        name: String,
        email: String,
        profilePhoto: String,
        mobilePhone: String,
        updatePhoto: Boolean
    ) {
        if(updatePhoto){
            val refCourier = storage.getReferenceFromUrl("gs://motoflash-a2f12.appspot.com/").child("users/$userId/profilePhoto/")
            val fileDocument = Uri.parse(profilePhoto)
            val refUploadDocument = refCourier.child(fileDocument.lastPathSegment!!)
            refUploadDocument.putFile(fileDocument).addOnSuccessListener {
                refUploadDocument.downloadUrl.addOnSuccessListener {
                    updateProfile(userId, name, email, it.toString(), mobilePhone)
                }.addOnFailureListener {
                    log("Error: ${it.message}")
                    mvpView?.onUpdateProfileFail()
                }
            }.addOnFailureListener{
                log("Error: ${it.message}")
                mvpView?.onUpdateProfileFail()
            }
        }else{
            updateProfile(userId, name, email, profilePhoto, mobilePhone)
        }
    }

    private fun updateProfile(userId: String, name: String, email: String, profilePhoto: String, mobilePhone: String){
        val body = HashMap<String,Any>()
        body["user"] = User(
            name = name,
            email = email,
            profilePhoto = profilePhoto,
            mobilePhone = mobilePhone,
            active = true
        )
        compositeDisposable +=  api
            .doUpdateUser(
                accessToken = currentTokenId,
                userId = userId,
                body = toBodyJsonObject(body)
            )
            .compose(RxUtil.applyNetworkSchedulers())
            .subscribe({
                log("success")
                mvpView?.run {
                    onUpdateProfile()
                }
            },{
                log("error: ${it.message}")
                if(it is HttpException){
                    val error = ErrosUtil.getErrorCode(it)
                    log("field: ${error.field}")
                    mvpView?.onUpdateProfileError(error)
                }else{
                    mvpView?.onUpdateProfileFail()
                }
            })
    }

    override fun doUpdatePassword(userId: String, password: String) {
        val body = HashMap<String, Any>()
        body["password"] = password
        compositeDisposable += api
            .doUpdateUserPassword(
                accessToken = currentTokenId,
                userId = userId,
                body = toBodyJsonObject(body)
            )
            .compose(RxUtil.applyNetworkSchedulers())
            .subscribe({
                mvpView?.onUpdatePassword()
            },{
                log("Error:${it.message}")
                mvpView?.onUpdatePasswordFail()
            })
    }
}