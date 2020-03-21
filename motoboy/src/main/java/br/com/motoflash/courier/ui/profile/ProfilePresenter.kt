package br.com.motoflash.courier.ui.profile

import android.net.Uri
import br.com.motoflash.core.data.network.model.Courier
import br.com.motoflash.core.ui.util.DEVICE_ID
import br.com.motoflash.core.ui.util.ErrosUtil
import br.com.motoflash.core.ui.util.RxUtil
import br.com.motoflash.core.ui.util.TOKEN_ACCESS
import br.com.motoflash.courier.ui.base.BasePresenter
import com.crashlytics.android.Crashlytics
import com.pixplicity.easyprefs.library.Prefs
import io.reactivex.rxkotlin.plusAssign
import retrofit2.HttpException
import javax.inject.Inject

class ProfilePresenter<V : ProfileMvpView> @Inject
constructor() : BasePresenter<V>(), ProfileMvpPresenter<V> {
    override fun doUpdateOnline(courierId: String, online: Boolean) {

        auth.currentUser?.let {
            it.getIdToken(true).addOnSuccessListener {result ->
                result.token?.let{

                    currentTokenId = result.token!!
                    Prefs.putString(TOKEN_ACCESS, currentTokenId)
                    log("token: $currentTokenId")

                    val body = HashMap<String, Any>()

                    body["online"] = online

                    compositeDisposable += api
                        .doCourierOnline(
                            accessToken = currentTokenId,
                            courierId = courierId,
                            body = toBodyJsonObject(body)
                        )
                        .compose(RxUtil.applyNetworkSchedulers())
                        .subscribe({
                            log("subscribe")
                            mvpView?.onUpdateOnline()
                        },{
                            log("Error: ${it.message}")
                            if(it is HttpException){
                                val error = ErrosUtil.getErrorCode(it)
                                log("field: ${error.field}")
                                mvpView?.run {
                                    onUpdateInvalidCourier()
                                }
                            }else{
                                mvpView?.onUpdateFail()
                            }
                        })
                }
            }.addOnFailureListener {
                Crashlytics.logException(it)
                log("Error: ${it.message}")
                mvpView?.onUpdateFail()
            }
        }

        val body = HashMap<String, Any>()


    }

    override fun doUpdateProfilePhoto(courierId: String, courierPhoto: String) {
        val refCourier = storage.getReferenceFromUrl("gs://motoflash-a2f12.appspot.com/").child("couriers/$courierId/profilePhoto")
        val fileDocument = Uri.parse(courierPhoto)
        val refUploadDocument = refCourier.child(fileDocument.lastPathSegment!!)
        refUploadDocument.putFile(fileDocument).addOnSuccessListener {
            refUploadDocument.downloadUrl.addOnSuccessListener {
                firestore
                    .collection("couriers")
                    .document(courierId)
                    .update("profilePhoto", it.toString())
                    .addOnSuccessListener {
                        mvpView?.run {
                            onUpdateProfilePhoto()
                        }
                    }
                    .addOnFailureListener {
                        mvpView?.run {
                            onUpdateProfilePhotoFail()
                        }
                    }

            }.addOnFailureListener {
                mvpView?.run {
                    onUpdateProfilePhotoFail()
                }
            }
        }.addOnFailureListener{
            mvpView?.run {
                onUpdateProfilePhotoFail()
            }
        }
    }
}