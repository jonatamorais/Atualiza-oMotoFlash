package br.com.motoflash.core.data.network.model

import com.google.firebase.Timestamp

data class Payment(
    var id: String? = null,
    var courierAmount: Double? = null,
    var courierId: String? = null,
    var createdDate: Timestamp? = null,
    var status: String? = null,
    var workOrderId: String? = null
){
    enum class Status {
        PENDING, PAY, CANCELED
    }
}