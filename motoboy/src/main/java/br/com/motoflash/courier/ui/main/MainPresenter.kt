package br.com.motoflash.courier.ui.main

import android.util.Log
import br.com.motoflash.core.data.network.model.Courier
import br.com.motoflash.core.ui.util.DEVICE_ID
import br.com.motoflash.courier.ui.base.BasePresenter
import com.crashlytics.android.Crashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import com.google.gson.Gson
import com.pixplicity.easyprefs.library.Prefs
import javax.inject.Inject

class MainPresenter<V :MainMvpView> @Inject
constructor() : BasePresenter<V>(), MainMvpPresenter<V> {

    private var removeListener: ListenerRegistration? = null

    override fun doGetCourier() {
        val user = auth.currentUser!!


        removeListener = firestore
            .collection("couriers")
            .document(user.uid)
            .addSnapshotListener{ doc, e ->
                if(e != null){
                    Crashlytics.logException(e)
                    log("Error: ${e.message}")
                    logoutUser("Erro ao recuperar informações do usuário")
                }else{
                    if(doc != null && doc.exists()){
                        if (doc.metadata.hasPendingWrites()){
                            log("local")
                            return@addSnapshotListener
                        }else{
                            log("server")
                        }


                        val userDoc = doc.toObject(Courier::class.java)!!
                        userDoc.id = doc.id

                        Log.d("ProfileFragment", "update snap")

                        val device = getUserDevice(Prefs.getString(DEVICE_ID, ""))

                        if(userDoc.active == true){
                            firestore
                                .collection("couriers")
                                .document(user.uid)
                                .collection("devices")
                                .document(device.uniqueId!!)
                                .set(device)
                                .addOnCompleteListener {
                                    mvpView?.onGetCourier(userDoc)
                                }
                        }
                        else{
//                            log("userDoc.active: ${Gson().toJson(userDoc)}")
//                            logoutUser("Usuário bloqueado")
                            firestore
                                .collection("couriers")
                                .document(user.uid)
                                .get(Source.SERVER)
                                .addOnSuccessListener {
                                    val courier = it.toObject(Courier::class.java)!!
                                    if(courier.active == true){
                                        firestore
                                            .collection("couriers")
                                            .document(courier.id!!)
                                            .collection("devices")
                                            .document(device.uniqueId!!)
                                            .set(device)
                                            .addOnCompleteListener {
                                                mvpView?.onGetCourier(courier)
                                            }
                                    }else{
                                        log("userDoc.active: ${Gson().toJson(userDoc)}")
                            logoutUser("Usuário bloqueado")
                                    }
                                }
                                .addOnFailureListener {
                                    Crashlytics.logException(it)
                                    log("get2 fail: ${it.message}")
                                    logoutUser("Usuário bloqueado")
                                }
                        }

                    }else{
                        log("doc != null && doc.exists()")
                        logoutUser("Usuário não encontrado")
                    }
                }
            }
    }

    private fun logoutUser(message: String){
        mvpView?.run {
            onLogoutCourier(message)
        }
    }

    override fun onDetach() {
        removeListener?.remove()
        super.onDetach()
    }
}