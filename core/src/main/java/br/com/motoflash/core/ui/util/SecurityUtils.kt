package br.com.motoflash.core.ui.util

import android.util.Base64
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

object SecurityUtils {
    fun SHA256(input: String): String? {
        try {
            val mDigest = MessageDigest.getInstance("SHA256")
            val result = mDigest.digest(input.toByteArray())
            val sb = StringBuilder()
            for (aResult in result) {
                sb.append(Integer.toString((aResult.toInt() and 0xff) + 0x100, 16).substring(1))
            }
            return sb.toString().toUpperCase()
        } catch (e: NoSuchAlgorithmException) {
        }

        return null
    }

    fun rsaEncrypt(original: String, publicKey: String): String {
        var publicKeyEdited = publicKey
        publicKeyEdited = publicKeyEdited.replace("\\n", "").replace("\\r", "")
        try {
            val pubKeyPEM = publicKeyEdited.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
            val keyBytes = Base64.decode(pubKeyPEM.toByteArray(charset("UTF-8")), Base64.DEFAULT)
            val spec = X509EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            val key = keyFactory.generatePublic(spec)
            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encryptedBytes = cipher.doFinal(original.toByteArray())

            return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            System.err.println(e.message)
        }

        return ""
    }
}