package vn.eazy.harrylefit.secrets

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import vn.eazy.harrylefit.keeper.authentication.EncryptionServices

class MainActivity : AppCompatActivity() {
    private lateinit var secrets: SharedPreferences


    companion object {
        const val KEY = "PASSWORD"
        const val SAVED = "SAVED"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        secrets = getSharedPreferences("StorageSecrets", android.content.Context.MODE_PRIVATE)
        createKeys()
        secrets.getString(KEY, null)?.apply {
            val data = EncryptionServices(applicationContext).decrypt(this, this@MainActivity)
            tv_decrypted_data?.text = data
        }

        btn_encrypt?.setOnClickListener {
            et_data?.text?.toString()?.apply {
                if (isPasswordValid(this)) {
                    val encryptedPassword = EncryptionServices(applicationContext).encrypt(this, this@MainActivity)
                    Log.d("Secret", "Original password is: $this")
                    Log.d("Secret", "Saved password is: $encryptedPassword")
                    secrets.edit()?.putString(KEY, encryptedPassword)?.apply()
                } else {
                    et_data?.error = "Data's invalid"
                }
            }
        }

        btn_decrypt?.setOnClickListener {
            if (secrets.contains(KEY)) {
                val encryptedData = secrets.getString(KEY, null)
                val decryptedData = EncryptionServices(applicationContext).decrypt(encryptedData, this@MainActivity)
                tv_decrypted_data?.error = null
                tv_decrypted_data?.text = decryptedData
            } else {
                tv_decrypted_data?.error = "No found data"
            }
        }
    }

    /**
     * Create master, fingerprint and confirm credentials keys.
     */
    private fun createKeys() {
        val encryptionService = EncryptionServices(applicationContext)
        encryptionService.createMasterKey(this)
    }


    private fun isPasswordValid(password: String) = !TextUtils.isEmpty(password) && password.length >= 6
}
