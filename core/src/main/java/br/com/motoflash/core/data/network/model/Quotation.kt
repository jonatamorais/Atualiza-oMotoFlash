package br.com.motoflash.core.data.network.model

import com.google.firebase.Timestamp

data class Quotation(
    var id: String? = null,
    var companyId: String? = null,
    var createdDate: Timestamp? = null,
    var distance: Long? = null,
    var duration: Long? = null,
    var price: Double? = null
)