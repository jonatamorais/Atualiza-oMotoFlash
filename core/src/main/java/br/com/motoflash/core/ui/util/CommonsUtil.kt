package br.com.motoflash.core.ui.util

import android.content.Context
import android.graphics.*
import android.util.TypedValue
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import androidx.core.content.ContextCompat
import br.com.motoflash.core.R
import br.com.motoflash.core.data.network.model.WorkOrder
import com.google.android.gms.maps.model.LatLng
import org.jetbrains.anko.layoutInflater


class CommonsUtil {
    companion object{
        fun getPx(context: Context, dip: Float) : Float{
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                context.resources.displayMetrics
            )
        }

        fun isValidPassword(password: String): Boolean {

            val pattern: Pattern
            val matcher: Matcher

            val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$"

            pattern = Pattern.compile(PASSWORD_PATTERN)
            matcher = pattern.matcher(password)

            return matcher.matches()

        }




        // Return true if the card number is valid
        fun isValidCreditCard(number: Long): Boolean {
            return getSize(number) >= 13 && getSize(
                number
            ) <= 16 &&
                    (prefixMatched(number, 4) ||
                            prefixMatched(number, 5) ||
                            prefixMatched(number, 37) ||
                            prefixMatched(number, 6)) &&
                    sumOfDoubleEvenPlace(number) + sumOfOddPlace(
                        number
                    ) % 10 == 0
        }

        // Get the result from Step 2
        fun sumOfDoubleEvenPlace(number: Long): Int {
            var sum = 0
            val num = number.toString() + ""
            var i = Companion.getSize(number) - 2
            while (i >= 0) {
                sum += getDigit(Integer.parseInt(num[i] + "") * 2)
                i -= 2
            }

            return sum
        }

        // Return this number if it is a single digit, otherwise,
        // return the sum of the two digits
        fun getDigit(number: Int): Int {
            return if (number < 9) number else number / 10 + number % 10
        }

        // Return sum of odd-place digits in number
        fun sumOfOddPlace(number: Long): Int {
            var sum = 0
            val num = number.toString() + ""
            var i = getSize(number) - 1
            while (i >= 0) {
                sum += Integer.parseInt(num[i] + "")
                i -= 2
            }
            return sum
        }

        // Return true if the digit d is a prefix for number
        fun prefixMatched(number: Long, d: Int): Boolean {
            return getPrefix(
                number,
                getSize(d.toLong())
            ) == d.toLong()
        }

        // Return the number of digits in d
        fun getSize(d: Long): Int {
            val num = d.toString() + ""
            return num.length
        }

        // Return the first k number of digits from
        // number. If the number of digits in number
        // is less than k, return number.
        fun getPrefix(number: Long, k: Int): Long {
            if (getSize(number) > k) {
                val num = number.toString() + ""
                return java.lang.Long.parseLong(num.substring(0, k))
            }
            return number
        }

        private val pesoCPF = intArrayOf(11, 10, 9, 8, 7, 6, 5, 4, 3, 2)
        private val pesoCNPJ = intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)

        private fun calcularDigito(str: String, peso: IntArray): Int {
            var soma = 0
            var indice = str.length - 1
            var digito: Int
            while (indice >= 0) {
                digito = Integer.parseInt(str.substring(indice, indice + 1))
                soma += digito * peso[peso.size - str.length + indice]
                indice--
            }
            soma = 11 - soma % 11
            return if (soma > 9) 0 else soma
        }
        fun convertPxToDp(context: Context, dip: Float): Float {
            val r = context.getResources()
            val px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
            )
            return px
        }

        fun isValidCPF(_cpf: String): Boolean {
            val cpf = _cpf.unMaskOnlyNumbers()
            if (cpf.length != 11 || cpf == "00000000000" || cpf == "11111111111" ||
                cpf == "22222222222" || cpf == "33333333333" || cpf == "44444444444" || cpf == "55555555555" ||
                cpf == "66666666666" || cpf == "77777777777" || cpf == "88888888888" || cpf == "99999999999") {
                return false
            }

            val digito1 = calcularDigito(
                cpf.substring(0, 9),
                pesoCPF
            )
            val digito2 =calcularDigito(
                cpf.substring(0, 9) + digito1,
                pesoCPF
            )
            return cpf == cpf.substring(0, 9) + digito1.toString() + digito2.toString()
        }

        fun isValidCNPJ(cnpj: String): Boolean {
            if (cnpj.length != 14 || cnpj == "00000000000000" || cnpj == "11111111111111" ||
                cnpj == "22222222222222" || cnpj == "33333333333333" || cnpj == "44444444444444" || cnpj == "55555555555555" ||
                cnpj == "66666666666666" || cnpj == "77777777777777" || cnpj == "88888888888888" || cnpj == "99999999999999") {
                return false
            }

            val digito1 = calcularDigito(
                cnpj.substring(0, 12),
                pesoCNPJ
            )
            val digito2 = calcularDigito(
                cnpj.substring(0, 12) + digito1,
                pesoCNPJ
            )
            return cnpj == cnpj.substring(0, 12) + digito1.toString() + digito2.toString()
        }

        fun getAndroidVersion(sdkInt: Int) : String {
            return when(sdkInt){
                17 -> "4.2"
                18 -> "4.3"
                19 -> "4.4"
                20 -> "4.4"
                21 -> "5.0"
                22 -> "5.1"
                23 -> "6.0"
                24 -> "7.0"
                25 -> "7.1"
                26 -> "8.0"
                27 -> "8.1"
                28 -> "9.0"
                else -> sdkInt.toString()
            }
        }

        fun getColorOfWorkOrderStatus(workOrder: WorkOrder) : Int{
            when(WorkOrder.Status.valueOf(workOrder.status!!)){
                WorkOrder.Status.PENDING -> {
                    return R.color.colorYellow
                }
                WorkOrder.Status.ASSIGNED -> {
                    return R.color.colorYellow
                }
                WorkOrder.Status.EXECUTION -> {
                    return R.color.colorGreen
                }
                WorkOrder.Status.CANCELLED -> {
                    return R.color.colorRed
                }
                WorkOrder.Status.FINISHED -> {
                    return R.color.colorGray
                }
                WorkOrder.Status.ALL -> {
                    return R.color.colorGray
                }
            }
        }

        fun bearingBetweenLocations(latLng1: LatLng, latLng2: LatLng): Double {
            val PI = 3.14159
            val lat1 = latLng1.latitude * PI / 180.0
            val long1 = latLng1.longitude * PI / 180.0
            val lat2 = latLng2.latitude * PI / 180.0
            val long2 = latLng2.longitude * PI / 180.0
            val dLon = long2 - long1
            val y = Math.sin(dLon) * Math.cos(lat2)
            val x =
                Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)
            var brng = Math.atan2(y, x)
            brng = Math.toDegrees(brng)
            brng = (brng + 360.0) % 360.0
            return brng
        }

        fun distanceLatLng(lat_a: Float, lng_a: Float, lat_b: Float, lng_b: Float): Float {
            val earthRadius = 3958.75
            val latDiff = Math.toRadians((lat_b - lat_a).toDouble())
            val lngDiff = Math.toRadians((lng_b - lng_a).toDouble())
            val a =
                Math.sin(latDiff / 2) * Math.sin(latDiff / 2) + Math.cos(Math.toRadians(lat_a.toDouble())) * Math.cos(
                    Math.toRadians(lat_b.toDouble())
                ) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            val distance = earthRadius * c

            val meterConversion = 1609

            return (distance * meterConversion).toFloat()
        }

        fun splitLine(
            a: LatLng,
            b: LatLng,
            count: Int
        ): List<LatLng> {
            var count = count

            count += 1

            val d = sqrt((a.latitude - b.latitude) * (a.latitude - b.latitude) + (a.longitude - b.longitude) * (a.longitude - b.longitude)) / count
            val fi = atan2(b.longitude - a.longitude, b.latitude - a.latitude)

            val points : MutableList<LatLng> = ArrayList()

            for (i in 0..count)
                points.add(
                   LatLng(
                        a.latitude + i * d * cos(fi),
                        a.longitude + i * d * sin(fi)
                    )
                )

            return points
        }

        fun createPureTextIcon(context: Context, text: String, colorBackground: Int, textSize: Int = 20): BitmapDescriptor {

            val textPaint = Paint() // Adapt to your needs

            textPaint.textSize = textSize / context.resources.displayMetrics.scaledDensity
            textPaint.color = ContextCompat.getColor(context, R.color.colorRed)
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

//            textPaint.style = Paint.Style.STROKE

            val width =  textPaint.measureText(text).toInt() + getPx(context, 20f).toInt()
            val height =  getPx(context, 30f)

            val image = Bitmap.createBitmap(width, height.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(image)

            canvas.translate(0f, height)

            // For development only:
            // Set a background in order to see the
            // full size and positioning of the bitmap.
            // Remove that for a fully transparent icon.
            canvas.drawColor(ContextCompat.getColor(context, colorBackground))

            val xPos = canvas.width / 2.toFloat()
            val yPos = (canvas.height / 2 - (textPaint.descent() + textPaint.ascent()) / 2)

            canvas.drawText(text, getPx(context, 10f), -getPx(context, 10f), textPaint)
            return BitmapDescriptorFactory.fromBitmap(image)
        }

        fun showCustomTost(context: Context, message: String, success: Boolean) {
            val inflater = context.layoutInflater
            val layout = inflater.inflate(R.layout.custom_toast_result, null)

            val image = layout.findViewById(R.id.toastCustomImage) as ImageView
            val drawable = ContextCompat.getDrawable(context, if (false) R.drawable.ic_success else R.drawable.ic_error)!!
//            val wrappedDrawable = DrawableCompat.wrap(drawable)
//            DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(context, if (success) R.color.gray else R.color.blue))
            image.setImageDrawable(drawable)

            val cardView = layout.findViewById(R.id.toastCustomLayout) as CardView
//            cardView.setCardBackgroundColor(ContextCompat.getColor(context, if (success) R.color.gray else R.color.blue))

            val text = layout.findViewById(R.id.toastCustomText) as TextView
            text.text = message
//            text.setTextColor(ContextCompat.getColor(context, if (success) R.color.gray else R.color.blue))

            val toast = Toast(context)
            /*toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)*/
            toast.duration = if(success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
            toast.view = layout
            toast.show()
        }
    }
}