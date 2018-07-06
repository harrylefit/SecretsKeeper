package vn.eazy.harrylefit.keeper.wrapper

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.*
import java.util.*
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.security.auth.x500.X500Principal

class KeyStoreWapper(private val context: Context, defaultKeyStoreName: String) {
    private val keyStore: KeyStore = createAndroidKeyStore()
    private val defaultKeyStoreFile = File(context.filesDir, defaultKeyStoreName)
    private val defaultKeyStore = createDefaultKeyStore()

    fun getAndroidKeyStoreSymmetricKey(alias: String): SecretKey? = keyStore.getKey(alias, null) as SecretKey?

    fun getDefaultKeyStoreSymmetricKey(alias: String, keyPassword: String): SecretKey? {
        return try {
            defaultKeyStore.getKey(alias, keyPassword.toCharArray()) as SecretKey
        } catch (e: UnrecoverableKeyException) {
            null
        }
    }

    fun getAndroidKeyStoreAsymmetricKey(alias: String): KeyPair? {
        val privateKey = keyStore.getKey(alias, null) as PrivateKey?
        val publicKey = keyStore.getCertificate(alias)?.publicKey
        return if (privateKey != null && publicKey != null) {
            KeyPair(publicKey, privateKey)
        } else {
            null
        }
    }

    fun removeAndroidKeyStore(alias: String) = keyStore.deleteEntry(alias)

    fun createDefaultKeyStoreSymmetricKey(alias: String, password: String) {
        val key = generateDefaultSymmetricKey()
        val keyEntry = KeyStore.SecretKeyEntry(key)
        defaultKeyStore.setEntry(alias, keyEntry, KeyStore.PasswordProtection(password.toCharArray()))
        defaultKeyStore.store(FileOutputStream(defaultKeyStoreFile), password.toCharArray())
    }


    private fun generateDefaultSymmetricKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        return keyGenerator.generateKey()
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun createAndroidKeyStoreAsymmetricKey(alias: String): KeyPair {
        val generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initGeneratorWithKeyGenParameterSpec(generator, alias)
        } else {
            initGeneratorWithKeyPairGeneratorSpec(generator, alias)
        }
        return generator.generateKeyPair()
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun createAndroidKeyStoreSymmetricKey(
            alias: String,
            userAuthenticationRequired: Boolean = false,
            invalidatedByBiometricEnrollment: Boolean = true,
            userAuthenticationValidityDurationSeconds: Int = -1,
            userAuthenticationValidWhileOnBody: Boolean = true): SecretKey {

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val builder = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(userAuthenticationRequired)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationValidityDurationSeconds(userAuthenticationValidityDurationSeconds)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment)
            builder.setUserAuthenticationValidWhileOnBody(userAuthenticationValidWhileOnBody)
        }
        keyGenerator.init(builder.build())
        return keyGenerator.generateKey()
    }
    

    @TargetApi(Build.VERSION_CODES.M)
    private fun initGeneratorWithKeyGenParameterSpec(generator: KeyPairGenerator, alias: String) {
        val builder = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
        generator.initialize(builder.build())
    }


    private fun initGeneratorWithKeyPairGeneratorSpec(generator: KeyPairGenerator, alias: String) {
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        endDate.add(Calendar.YEAR, 20)

        val builder = KeyPairGeneratorSpec.Builder(context)
                .setAlias(alias)
                .setSerialNumber(BigInteger.ONE)
                .setSubject(X500Principal("CN=$alias CA Certificate"))
                .setStartDate(startDate.time)
                .setEndDate(endDate.time)

        generator.initialize(builder.build())
    }

    private fun createAndroidKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        return keyStore
    }

    private fun createDefaultKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())

        if (!defaultKeyStoreFile.exists()) {
            keyStore.load(null)
        } else {
            keyStore.load(FileInputStream(defaultKeyStoreFile), null)
        }
        return keyStore
    }

}