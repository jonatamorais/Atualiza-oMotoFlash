package br.com.motoflash.core.data.network.model

data class ErrorCode (
    var field: String? = null,
    var type: String? = null,
    var code: Long? = null,
    override var message: String? = null
)  : Throwable(message!!)