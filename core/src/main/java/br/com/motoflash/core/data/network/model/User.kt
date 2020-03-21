package br.com.motoflash.core.data.network.model

import com.google.firebase.Timestamp


data class User(
    var id: String? = null,
    var name: String? = null,
    var companyId: String? = null,
    var password: String? = null,
    var createdDate: Timestamp? = null,
    var email: String? = null,
    var mobilePhone: String? = null,
    var profilePhoto: String? = null,
    var active: Boolean? = null
)