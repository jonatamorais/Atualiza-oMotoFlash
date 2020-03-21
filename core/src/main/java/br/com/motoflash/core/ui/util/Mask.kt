package br.com.motoflash.core.ui.util

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView

/**
 * Created by lucascordeiro on 05/12/17.
 */

object Mask {
    var CPF_MASK = "###.###.###-##"
    var CNPJ_MASK = "##.###.###/####-##"
    var CARD_16_DIGITS_MASK = "#### #### #### ####"
    var CARD_15_DIGITS_MASK = "#### ###### #####"
    var CARD_20_DIGITS_MASK = "####################"
    var CEP_MASK = "#####-###"
    var BIRTH_DATE = "##/##/####"
    var MOBILE_PHONE_MASK = "(##) #####-####"
    var EXPIRY_DATE_MASK = "##/##"

    fun unmask(s: String): String {
        return if(s.isEmpty()) "" else s.replace("[.]".toRegex(), "").replace("[-]".toRegex(), "")
                .replace("[/]".toRegex(), "").replace("[(]".toRegex(), "")
                .replace("[)]".toRegex(), "").replace(" ".toRegex(), "")
                .replace(",".toRegex(), "").replace("R".toRegex(), "").replace("[$]".toRegex(), "").trim()
    }

    fun isASign(c: Char): Boolean {
        return c == '.' || c == '-' || c == '/' || c == '(' || c == ')' || c == ',' || c == ' '
    }

    fun mask(mask: String, text: String): String {
        var i = 0
        var mascara = ""
        for (m in mask.toCharArray()) {
            if (m != '#') {
                mascara += m
                continue
            }
            try {
                mascara += text[i]
            } catch (e: Exception) {
                break
            }

            i++
        }

        return mascara
    }

    fun insert(mask: String, ediTxt: EditText, copyText: TextView? = null): TextWatcher {
        return object : TextWatcher {
            internal var isUpdating: Boolean = false
            internal var old = ""

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val str = unmask(s.toString())
                var mascara = ""
                if (isUpdating) {
                    old = str
                    isUpdating = false
                    return
                }

                var index = 0
                for (i in 0 until mask.length) {
                    val m = mask[i]
                    if (m != '#') {
                        if (index == str.length && str.length < old.length) {
                            continue
                        }
                        mascara += m
                        continue
                    }

                    try {
                        mascara += str[index]
                    } catch (e: Exception) {
                        break
                    }

                    index++
                }

                if (mascara.length > 0) {
                    var last_char = mascara[mascara.length - 1]
                    var hadSign = false
                    while (isASign(last_char) && str.length == old.length) {
                        mascara = mascara.substring(0, mascara.length - 1)
                        last_char = mascara[mascara.length - 1]
                        hadSign = true
                    }

                    if (mascara.length > 0 && hadSign) {
                        mascara = mascara.substring(0, mascara.length - 1)
                    }
                }

                isUpdating = true
                ediTxt.setText(mascara)
                ediTxt.setSelection(mascara.length)

                if(copyText!=null){
                    copyText.setText(mascara)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {}
        }
    }

    fun creditCardTextWatcher(): TextWatcher {

        return object : TextWatcher {
            private val space = ' '

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // noop
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // noop
            }

            override fun afterTextChanged(s: Editable) {
                // Remove spacing char
                if (s.length > 0 && s.length % 5 == 0) {
                    val c = s[s.length - 1]
                    if (space == c) {
                        s.delete(s.length - 1, s.length)
                    }
                }
                // Insert char where needed.
                if (s.length > 0 && s.length % 5 == 0) {
                    val c = s[s.length - 1]
                    // Only if its a digit where there should be a space we insert a space
                    if (Character.isDigit(c) && TextUtils.split(
                            s.toString(),
                            space.toString()
                        ).size <= 3
                    ) {
                        s.insert(s.length - 1, space.toString())
                    }
                }
            }
        }
    }
}