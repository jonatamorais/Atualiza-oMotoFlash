package br.com.motoflash.core.ui.util

const val CRESCENTE = "Crescente"
const val DECRESCENTE = "Decrescente"

const val NAME = "name"
const val COMICS = "comics"
const val DATE = "date"

const val COLETA = "COLLECT"
const val ENTREGA = "DELIVERY"
const val OUTROS = "OTHER"

//SharedPreferences
const val TOKEN_ACCESS = "TOKEN_ACCESS"
const val EXPIRE_DATE = "EXPIRE_DATE"
const val REFRESH_TOKEN = "REFRESH_TOKEN"
const val DEVICE_ID = "DEVICE_ID"
const val DEVICE_MESSAGE_ID = "DEVICE_MESSAGE_ID"
const val SHORT_HASH = "SHORT_HASH"
const val HAS_USER = "HAS_USER"
const val USER_ID = "USER_ID"
const val LAST_LOCATION = "LAST_LOCATION"
const val LAST_LOCATION_TIME = "LAST_LOCATION_TIME"
const val CURRENT_WORK_ORDER = "CURRENT_WORK_ORDER"
const val COURIER_ONLINE = "COURIER_ONLINE"
const val COURIER_ID = "COURIER_ID"
const val DEVICE_COURIER_ID = "DEVICE_COURIER_ID"
const val CURRENT_WORK_ORDER_POINT_SIGNATURE = "CURRENT_WORK_ORDER_POINT_SIGNATURE"

//Firebase Events
const val EVENT_OPEN = "open"
const val EVENT_CLOSE = "close"
const val EVENT_REQUEST_START = "request_start"
const val EVENT_REQUEST_FINISH = "request_finish"
const val EVENT_REQUEST_PAY = "request_pay"


const val NAME_LABEL = "nome"
const val COMICS_LABEL = "comics"
const val DATE_LABEL = "data"

const val PUSH_VERIFY_ACCOUNT = "account"
const val PUSH_TITLE = "title"
const val PUSH_MESSAGE = "message"

enum class OsStatus {
    OPEN, ASSIGN, PENDING, FINISH
}

enum class PointStatus {
    PENDING, START, INPROGRESS, FINISH
}

enum class PointType{
    COLETA, ENTREGA
}