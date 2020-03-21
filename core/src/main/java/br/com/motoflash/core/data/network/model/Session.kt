package br.com.motoflash.core.data.network.model

import com.google.firebase.Timestamp

data class Session(
    var device: UserDevice,
    var timestamp: Timestamp,
    var user: User?
)