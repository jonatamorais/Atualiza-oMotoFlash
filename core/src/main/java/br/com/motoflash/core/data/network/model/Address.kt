package br.com.motoflash.core.data.network.model

data class Address(
    var address1: String? = null,
    var address2: String? = null,
    var city: String? = null,
    var location: Location? = null,
    var neighborhood: String? = null,
    var number: String = "Não definido",
    var zipCode: String? = null,
    var state: String? = null
){
    companion object{
        const val NUMBER = "street_number"
        const val STREET = "route"
        const val NEIGHBORHOOD = "sublocality_level_1"
        const val CITY = "administrative_area_level_2"
        const val STATE = "administrative_area_level_1"
        const val POSTAL_CODE = "postal_code"
    }
}