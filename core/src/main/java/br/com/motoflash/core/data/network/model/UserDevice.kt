package br.com.motoflash.core.data.network.model


data class UserDevice(

    var id: String? = null,
    var userId: String? = null,
    var platform: String = Platforms.CLIENT_ANDROID.name,
    var deviceToken: String? = null,
    var appVersion: String? = null,
    var snsEndpoint: String? = null,
    var manufacturer: String? = null,
    var brand: String? = null,
    var product: String? = null,
    var model: String? = null,
    var sysVersion: String? = null,
    var uniqueId: String? = null,
    var createdDate: Long? = null,
    var inactiveDate: Long? = null

) {

    enum class Platforms {
        CLIENT_ANDROID,
        CLIENT_IOS,
        CLIENT_WEB
    }
}