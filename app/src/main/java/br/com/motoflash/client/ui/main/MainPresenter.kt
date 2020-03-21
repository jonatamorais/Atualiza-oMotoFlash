package br.com.motoflash.client.ui.main

import br.com.motoflash.client.ui.base.BasePresenter
import br.com.motoflash.core.data.network.model.User
import br.com.motoflash.core.ui.util.DEVICE_ID
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.pixplicity.easyprefs.library.Prefs
import java.util.*
import javax.inject.Inject


class MainPresenter<V :MainMvpView> @Inject
constructor() : BasePresenter<V>(), MainMvpPresenter<V> {
    override fun doGetUser() {
        val user = auth.currentUser!!
        firestore
            .collection("users")
            .document(user.uid)
            .addSnapshotListener{ doc, e ->
                if(e != null){
                    log("Error: ${e.message}")
                    logoutUser()
                }else{
                    if(doc != null && doc.exists()){
                        val userDoc = doc.toObject(User::class.java)!!
                        userDoc.id = doc.id

                        val device = getUserDevice(Prefs.getString(DEVICE_ID, ""))

                        if(userDoc.active == true){
                            firestore
                                .collection("users")
                                .document(user.uid)
                                .collection("devices")
                                .document(device.uniqueId!!)
                                .set(device)
                                .addOnCompleteListener {
                                    mvpView?.onGetUser(userDoc)
                                }
                        }
                        else{
                            log("userDoc.active == true")
                            logoutUser()
                        }

                    }else{
                        log("doc != null && doc.exists()")
                        logoutUser()
                    }
                }
            }
    }

    private fun logoutUser(){
        mvpView?.run {
            onLogoutUser()
        }
    }
}