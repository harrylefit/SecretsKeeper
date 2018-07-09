package vn.eazy.harrylefit.keeper.authentication

import android.content.Context
import vn.eazy.harrylefit.keeper.wrapper.CipherWrapper
import vn.eazy.harrylefit.keeper.wrapper.KeyStoreWapper
import vn.eazy.harrylefit.keeper.wrapper.SystemServices

class EncryptionServices(context: Context) {
    companion object {
        val DEFAULT_KEY_STORE_NAME = "default_keystore"
        val MASTER_KEY = "MASTER_KEY"

    }

    private val keyStoreWrapper = KeyStoreWapper(context, DEFAULT_KEY_STORE_NAME)

    fun createMasterKey(password: String? = null) {
        if (SystemServices.hasMarshmallow()) {
            createAndroidSymmetricKey()
        } else {
            createDefaultSymmetricKey(password ?: "")
        }
    }

    fun createMasterKey(context: Context) {
        if (isExistMasterKey() == true)
            return
        if (SystemServices.hasMarshmallow()) {
            createAndroidSymmetricKey()
        } else {
            createDefaultSymmetricKey(SystemServices.getSecureId(context))
        }
    }

    fun isExistMasterKey(): Boolean? {
        return keyStoreWrapper.isExistKeyStore(MASTER_KEY)
    }

    fun removeMasterKey() {
        keyStoreWrapper.removeAndroidKeyStore(MASTER_KEY)
    }

    fun encrypt(data: String, keyPassword: String? = null): String {
        return if (SystemServices.hasMarshmallow()) {
            encryptWithAndroidSymmetricKey(data)
        } else {
            encryptWithDefaultSymmetricKey(data, keyPassword ?: "")
        }
    }

    fun encrypt(data: String, context: Context): String? {
        return try {
            if (isExistMasterKey() == true) {
                if (SystemServices.hasMarshmallow()) {
                    encryptWithAndroidSymmetricKey(data)
                } else {
                    encryptWithDefaultSymmetricKey(data, SystemServices.getSecureId(context))
                }
            } else {
                null
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun decrypt(data: String, context: Context): String? {
        return try {
            if (isExistMasterKey() == true) {
                if (SystemServices.hasMarshmallow()) {
                    decryptWithAndroidSymmetricKey(data)
                } else {
                    decryptWithDefaultSymmetricKey(data, SystemServices.getSecureId(context))
                }
            } else {
                null
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun decrypt(data: String, keyPassword: String? = null): String {
        return if (SystemServices.hasMarshmallow()) {
            decryptWithAndroidSymmetricKey(data)
        } else {
            decryptWithDefaultSymmetricKey(data, keyPassword ?: "")
        }
    }

    private fun encryptWithDefaultSymmetricKey(data: String, keyPassword: String): String {
        val masterKey = keyStoreWrapper.getDefaultKeyStoreSymmetricKey(MASTER_KEY, keyPassword)
        return CipherWrapper(CipherWrapper.TRANSFORMATION_SYMMETRIC).encrypt(data, masterKey, true)
    }

    private fun decryptWithDefaultSymmetricKey(data: String, keyPassword: String): String {
        val masterKey = keyStoreWrapper.getDefaultKeyStoreSymmetricKey(MASTER_KEY, keyPassword)
        return masterKey?.let { CipherWrapper(CipherWrapper.TRANSFORMATION_SYMMETRIC).decrypt(data, masterKey, true) }
                ?: ""
    }

    private fun encryptWithAndroidSymmetricKey(data: String): String {
        val masterKey = keyStoreWrapper.getAndroidKeyStoreSymmetricKey(MASTER_KEY)
        return CipherWrapper(CipherWrapper.TRANSFORMATION_SYMMETRIC).encrypt(data, masterKey, true)
    }

    private fun decryptWithAndroidSymmetricKey(data: String): String {
        val masterKey = keyStoreWrapper.getAndroidKeyStoreSymmetricKey(MASTER_KEY)
        return CipherWrapper(CipherWrapper.TRANSFORMATION_SYMMETRIC).decrypt(data, masterKey, true)
    }

    private fun createDefaultSymmetricKey(password: String) {
        keyStoreWrapper.createDefaultKeyStoreSymmetricKey(MASTER_KEY, password)
    }

    private fun createAndroidSymmetricKey() {
        keyStoreWrapper.createAndroidKeyStoreSymmetricKey(MASTER_KEY)

    }
}