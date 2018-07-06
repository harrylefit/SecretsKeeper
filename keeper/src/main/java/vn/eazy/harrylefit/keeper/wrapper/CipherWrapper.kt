package vn.eazy.harrylefit.keeper.wrapper

import android.util.Base64
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

class CipherWrapper(val transformation: String) {
    companion object {
        /**
         * For default created asymmetric keys
         */
        var TRANSFORMATION_ASYMMETRIC = "RSA/ECB/PKCS1Padding"

        /**
         * For default created symmetric keys
         */
        var TRANSFORMATION_SYMMETRIC = "AES/CBC/PKCS7Padding"

        val IV_SEPARATOR = "]"
    }

    val cipher: Cipher = Cipher.getInstance(transformation)

    fun encrypt(data: String, key: Key?, useInitializationVector: Boolean = false): String {
        cipher.init(Cipher.ENCRYPT_MODE, key)
        var result = ""
        if (useInitializationVector) {
            val iv = cipher.iv
            val ivString = Base64.encodeToString(iv, Base64.DEFAULT)
            result = ivString + IV_SEPARATOR
        }
        val bytes = cipher.doFinal(data.toByteArray())
        result += Base64.encodeToString(bytes, Base64.DEFAULT)
        return result
    }

    fun decrypt(data: String, key: Key?, useInitializationVector: Boolean = false): String {
        var encodedString: String
        if (useInitializationVector) {
            val split = data.split(IV_SEPARATOR.toRegex())
            if (split.size != 2) {
                throw IllegalArgumentException("Passed data is incorrect. There was no IV specified with it.")
            }

            val ivString = split[0]
            encodedString = split[1]
            val ivSpec = IvParameterSpec(Base64.decode(ivString, Base64.DEFAULT))
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
        } else {
            encodedString = data
            cipher.init(Cipher.DECRYPT_MODE, key)
        }
        val encryptedData = Base64.decode(encodedString, Base64.DEFAULT)
        val encodedData = cipher.doFinal(encryptedData)
        return String(encodedData)
    }
}