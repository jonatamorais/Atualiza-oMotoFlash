package br.com.motoflash.core.data.network.model

data class WorkOrderPoint(
    var address: Address? = null,
    var id: String? = null,
    var sequence: Long? = null,
    var status: String? = null
){
    enum class Status {
        PENDING, STARTED, CHECKED_IN, CHECKED_OUT, CANCELED
    }
}